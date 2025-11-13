package frallware.hockey.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import frallware.hockey.api.GoalieOperations
import frallware.hockey.api.GoalieStrategy
import frallware.hockey.api.Player
import frallware.hockey.api.PlayerStrategy
import frallware.hockey.api.Point
import frallware.hockey.api.SkaterOperations
import frallware.hockey.api.SkaterStrategy
import kotlin.random.Random

class GdxPlayer(
    world: World,
    val strategy: PlayerStrategy,
    val color: Color,
    val startingPoint: Vector2,
    val startingAngle: Float,
    val stateMaker: (player: GdxPlayer) -> StateImpl
) {
    companion object {
        const val MAX_VELOCITY = 25f

        const val MAX_GLIDE = 5f
        const val MAX_SHOT_FORCE = 10f
        const val MAX_PASS_FORCE = 5f

        const val PLAYER_COLLISION_GROUP: Int = 1 shl 3

        const val RENDER_STICK_AREA = false
    }

    val body: Body
    val bodyRect = when (strategy) {
        is GoalieStrategy -> RoundedRect.create(1.2f, 1.6f, 0.4f, 3)
        is SkaterStrategy -> RoundedRect.create(1.0f, 1.2f, 0.4f, 3)
    }
    val bodyPolygons: List<Triple<Vector2, Vector2, Vector2>> = bodyRect.polygonTriangles()
    val stickTip: Vector2 = Vector2(0f, -0.9f * bodyRect.height)
    val stickArea = listOf(
        Vector2.Zero,
        stickTip,
        stickTip + Vector2(0.3f, 0.1f),
        bodyRect.bottomRightPoints.last(),
    )


    var puck: GdxPuck? = null
        private set

    private val state: StateImpl by lazy(LazyThreadSafetyMode.NONE) { stateMaker(this) }

    init {
        // Create a dynamic body for the ball
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(startingPoint)
            angle = startingAngle
            linearDamping = 0.5f // Adds friction/slowdown when no force applied
        }

        body = world.createBody(bodyDef)
        body.isFixedRotation = true


        val bodyShape = PolygonShape()

        for (polygon in bodyPolygons) {
            // Generally speaking, the player should not collide with the puck...
            // (the puck should be able to slide through the legs of the player)
            // Thus, the filterData

            bodyShape.set(arrayOf(polygon.first, polygon.second, polygon.third))
            body.createFixture(bodyShape, 1f).apply {
                restitution = 0.2f // Bounce
                friction = 0.6f
                userData = this@GdxPlayer
                filterData = filterData.apply {
                    categoryBits = PLAYER_COLLISION_GROUP.toShort()
                    maskBits = GdxPuck.COLLISION_GROUP.inv().toShort()
                }
            }

            // but we have a sensor-fixture that lets us detect collision with the puck to pick it up
            body.createFixture(bodyShape, 0f).apply {
                isSensor = true
                userData = this@GdxPlayer
            }
        }

        // this is used to make sure the place where we drag the puck can't overlap with things like
        // the rink or the goal cage
        val stickAreaShape = PolygonShape()
        stickAreaShape.set(stickArea.toTypedArray())
        body.createFixture(stickAreaShape, 0f).apply {
            friction = 0.0f
            userData = this@GdxPlayer
            filterData = filterData.apply {
                categoryBits = PLAYER_COLLISION_GROUP.toShort()
                maskBits = GdxPuck.COLLISION_GROUP.inv().toShort()
            }
        }
        // we should also be able to pick up the puck in this area
        body.createFixture(stickAreaShape, 0f).apply {
            isSensor = true
            userData = this@GdxPlayer
        }

        bodyShape.dispose()
        stickAreaShape.dispose()
    }

    fun reset() {
        body.setTransform(startingPoint, startingAngle)
        body.linearVelocity = Vector2.Zero
        body.angularVelocity = 0f
        dropPuck()
    }

    fun tryTakePuck(puck: GdxPuck) {
        val failureChance = when (strategy) {
            is GoalieStrategy -> 0.1
            is SkaterStrategy -> 0.3
        }
        val puckSpeed = puck.body.linearVelocity.len()
        if (puckSpeed > 50f && Random.boolean(failureChance)) {
            // failed to take it
            return
        }

        puck.holder?.dropPuck()
        puck.holder = this
        this.puck = puck
    }

    fun dropPuck() {
        if (puck?.holder == this) {
            puck?.holder = null
        }
        puck = null
    }

    fun update() {
        val move = StepOperations()
        when (strategy) {
            is GoalieStrategy -> strategy.step(state, move)
            is SkaterStrategy -> strategy.step(state, move)
        }
        val angle = body.angle

        move.turnTowards?.let { destination ->
            val position = body.position
            val facingDirection = Vector2(1f, 0f).rotateRad(angle)
            val destinationDirection = Vector2(destination.x - position.x, destination.y - position.y).nor()
            val currentSpeed = body.linearVelocity.len()
            val turnSpeed = if (currentSpeed < MAX_VELOCITY / 3) 6f else 3f

            // https://stackoverflow.com/a/3461533
            val a = Vector2.Zero
            val b = facingDirection
            val c = destinationDirection
            val cross = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
            if (cross > 0.1) { // isLeft
                body.angularVelocity = turnSpeed
                body.linearVelocity = body.linearVelocity.rotateRad(0.025f)
            } else if (cross < -0.1) { // isRight
                body.angularVelocity = -turnSpeed
                body.linearVelocity = body.linearVelocity.rotateRad(-0.025f)
            } else {
                body.angularVelocity = 0f
            }

            val speedDiscrepancy = move.moveSpeed - currentSpeed
            val acceleration = when {
                speedDiscrepancy > 10 -> 15f
                speedDiscrepancy > 3 -> 8f
                speedDiscrepancy > 0.5 -> 3f
                speedDiscrepancy < -10 -> -10f
                speedDiscrepancy < -5 -> -5f
                speedDiscrepancy < -1 -> -2f
                else -> 0f
            }
            body.applyForceToCenter(Vector2(acceleration, 0f).rotateRad(body.angle), true)
        }

        move.glideTowards?.let { destination ->
            // negative should be glide to the goalies right
            val position = body.position
            val distance = Vector2(destination.x - position.x, destination.y - position.y)
            val speed = when {
                distance.len() > 0.5 -> move.moveSpeed
                distance.len() > 0.1 -> 0.5f
                else -> 0f
            }
            body.linearVelocity = distance.nor().scl(speed)
        }

        move.shotDestination?.let { destination ->
            val position = body.position
            val destinationDirection = Vector2(destination.x - position.x, destination.y - position.y).nor()
            val angleRandomness = Random.nextDouble(-0.6, 0.6) * (move.shotForce / MAX_SHOT_FORCE)
            destinationDirection.rotateRad(angleRandomness.toFloat())

            puck?.body?.applyLinearImpulse(destinationDirection.scl(move.shotForce), Vector2.Zero, true)
            dropPuck()
        }

        move.passDestination?.let { destination ->
            val position = body.position
            val destinationDirection = Vector2(destination.x - position.x, destination.y - position.y).nor()
            val angleRandomness = Random.nextDouble(-0.05, 0.05) * (move.passForce / MAX_PASS_FORCE)
            destinationDirection.rotateRad(angleRandomness.toFloat())

            puck?.body?.applyLinearImpulse(destinationDirection.scl(move.passForce), Vector2.Zero, true)
            dropPuck()
        }

        puck?.let { puck ->
            val puckOffset = (stickTip + Vector2(0.1f, 0.3f)).rotateRad(body.angle)
            val worldPos = body.position.cpy().add(puckOffset)
            puck.body.setTransform(worldPos, body.angle)
            puck.body.linearVelocity = body.linearVelocity
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        val center = body.position
        if (RENDER_STICK_AREA) {
            val rotatedArea = stickArea.map { it.cpy().rotateRad(body.angle) }
            shapeRenderer.color = Color.GRAY
            for ((from, to) in rotatedArea.windowed(2)) {
                shapeRenderer.rectLine(body.position + from, body.position + to, 0.1f)
            }
            shapeRenderer.rectLine(body.position + rotatedArea.last(), body.position + rotatedArea.first(), 0.1f)
        }

        shapeRenderer.color = Color.BLACK
        val relativeStickTip = stickTip.cpy().rotateRad(body.angle) + body.position
        shapeRenderer.rectLine(relativeStickTip, body.position, 0.1f)

        shapeRenderer.color = color
        for (polygon in bodyPolygons) {
            val a = polygon.first.cpy().rotateRad(body.angle)
            val b = polygon.second.cpy().rotateRad(body.angle)
            val c = polygon.third.cpy().rotateRad(body.angle)
            shapeRenderer.triangle(
                a.x + center.x, a.y + center.y,
                b.x + center.x, b.y + center.y,
                c.x + center.x, c.y + center.y,
            )
        }

        val headOffset = Vector2(0.2f, 0f).rotateRad(body.angle)
        shapeRenderer.color = Color.LIGHT_GRAY
        shapeRenderer.circle(
            body.position.x + headOffset.x,
            body.position.y + headOffset.y,
            0.25f,
            20,
        )
    }

    inner class StepOperations : SkaterOperations, GoalieOperations {
        var turnTowards: Point? = null
            private set
        var glideTowards: Point? = null
        var moveSpeed: Float = 0f
            private set

        var passDestination: Point? = null
            private set
        var passForce: Float = 0f
            private set
        var shotDestination: Point? = null
            private set
        var shotForce: Float = 0f
            private set

        override fun skate(destination: Point, speed: Float): StepOperations {
            this.turnTowards = destination
            this.moveSpeed = speed.coerceAtMost(MAX_VELOCITY)
            this.glideTowards = null // mutually exclusive
            return this
        }

        override fun glide(destination: Point, speed: Float): StepOperations {
            this.glideTowards = destination
            this.moveSpeed = speed.coerceAtMost(MAX_GLIDE)
            this.turnTowards = null // mutually exclusive
            return this
        }

        override fun face(point: Point): StepOperations {
            this.turnTowards = point
            return this
        }

        override fun pass(player: Player, force: Float): StepOperations {
            this.passDestination = player.position
            this.passForce = force.coerceAtMost(MAX_PASS_FORCE)
            return this
        }

        override fun shoot(destination: Point, force: Float): StepOperations {
            this.shotDestination = destination
            this.shotForce = force.coerceAtMost(MAX_SHOT_FORCE)
            return this
        }
    }
}
