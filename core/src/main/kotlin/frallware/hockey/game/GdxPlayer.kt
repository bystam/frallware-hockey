package frallware.hockey.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
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
        const val RADIUS = 0.6f
        const val MAX_VELOCITY = 25f

        const val MAX_ACCELERATION = 15f
        const val MAX_GLIDE = 5f
        const val MAX_SHOT_FORCE = 10f
        const val MAX_PASS_FORCE = 5f

        const val STICK_AREA_COLLISION_GROUP: Int = 1 shl 3

        const val RENDER_STICK_AREA = true

        val stickTip: Vector2 = Vector2(0f, -1.8f * RADIUS)
        val stickArea = listOf(
            Vector2.Zero,
            stickTip,
            stickTip + Vector2(0.3f, 0.1f),
            Vector2(RADIUS, 0f).rotateRad(-0.3f),
        )
    }

    val body: Body

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

        // Create circular shape for the ball
        val circleShape = CircleShape().apply {
            radius = RADIUS
        }

        body.createFixture(circleShape, 1f).apply {
            restitution = 0.2f // Bounce
            friction = 0.6f
            userData = this@GdxPlayer
        }

        val stickAreaShape = PolygonShape()
        stickAreaShape.set(stickArea.toTypedArray())

        body.createFixture(stickAreaShape, 0f).apply {
            friction = 0.0f
            userData = this@GdxPlayer
            filterData = filterData.apply {
                categoryBits = STICK_AREA_COLLISION_GROUP.toShort()
                maskBits = GdxPuck.COLLISION_GROUP.inv().toShort()
            }
        }

        circleShape.dispose()
        stickAreaShape.dispose()
    }

    fun reset() {
        body.setTransform(startingPoint, startingAngle)
        body.linearVelocity = Vector2.Zero
        body.angularVelocity = 0f
        dropPuck()
    }

    fun takePuck(puck: GdxPuck) {
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

            // https://stackoverflow.com/a/3461533
            val a = Vector2.Zero
            val b = facingDirection
            val c = destinationDirection
            val cross = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
            if (cross > 0.1) { // isLeft
                body.angularVelocity = 3.06f
                body.linearVelocity = body.linearVelocity.rotateRad(0.03f)
            } else if (cross < -0.1) { // isRight
                body.angularVelocity = -3.06f
                body.linearVelocity = body.linearVelocity.rotateRad(-0.03f)
            } else {
                body.angularVelocity = 0f
            }

            body.applyForceToCenter(Vector2(move.moveSpeed, 0f).rotateRad(body.angle), true)
        }

        move.glideTowards?.let { destination ->
            // negative should be glide to the goalies right
            val position = body.position
            val glideSpeed = Vector2(destination.x - position.x, destination.y - position.y).nor().scl(move.moveSpeed)
            body.linearVelocity = glideSpeed
        }

        move.shotDestination?.let { destination ->
            val position = body.worldCenter
            val destinationDirection = Vector2(destination.x - position.x, destination.y - position.y).nor()
            val angleRandomness = Random.nextDouble(-0.8, 0.8) * (move.shotForce / MAX_SHOT_FORCE)
            destinationDirection.rotateRad(angleRandomness.toFloat())

            puck?.body?.applyLinearImpulse(destinationDirection.scl(move.shotForce), Vector2.Zero, true)
            dropPuck()
        }

        // Clamp velocity to max speed
        val velocity = body.linearVelocity
        val speed = velocity.len()
        if (speed > MAX_VELOCITY) {
            velocity.scl(MAX_VELOCITY / speed)
            body.linearVelocity = velocity
        }

        puck?.let { puck ->
            val puckOffset = (stickTip + Vector2(0.1f, 0.3f)).rotateRad(body.angle)
            val worldPos = body.position.cpy().add(puckOffset)
            puck.body.setTransform(worldPos, body.angle)
            puck.body.linearVelocity = body.linearVelocity
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
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
        shapeRenderer.circle(
            body.position.x,
            body.position.y,
            RADIUS,
            20
        )

        val headOffset = Vector2(0.2f, 0f).rotateRad(body.angle)

        shapeRenderer.color = Color.GREEN
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
            this.moveSpeed = speed.coerceAtMost(MAX_ACCELERATION)
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
