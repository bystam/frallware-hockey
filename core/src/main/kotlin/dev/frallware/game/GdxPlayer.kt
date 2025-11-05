package dev.frallware.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import dev.frallware.api.Player
import dev.frallware.api.PlayerOperations
import dev.frallware.api.PlayerStrategy
import dev.frallware.api.Point
import kotlin.random.Random

class GdxPlayer(
    world: World,
    val isGoalie: Boolean,
    val strategy: PlayerStrategy,
    val color: Color,
    val startingPoint: Vector2,
    val startingAngle: Float,
    val stateMaker: (player: GdxPlayer) -> StateImpl
) {
    companion object {
        const val RADIUS = 0.6f
        const val MAX_VELOCITY = 20f

        const val MAX_ACCELERATION = 10f
        const val MAX_SHOT_FORCE = 10f
        const val MAX_PASS_FORCE = 5f

        const val RENDER_STICK_AREA = false

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
        puck.body.fixtureList.forEach { it.isSensor = true }
    }

    fun dropPuck() {
        if (puck?.holder == this) {
            puck?.holder = null
        }
        puck = null
    }

    fun update() {
        val move = Move()
        strategy.step(state, move)
        val angle = body.angle

        move.moveDestination?.let { destination ->
            val position = body.worldCenter
            val facingDirection = Vector2(1f, 0f).rotateRad(angle)
            val destinationDirection = Vector2(destination.x - position.x, destination.y - position.y).nor()

            // https://stackoverflow.com/a/3461533
            val a = Vector2.Zero
            val b = facingDirection
            val c = destinationDirection
            val cross = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
            if (cross > 0.01) { // isLeft
                body.angularVelocity = 3.06f
                body.linearVelocity = body.linearVelocity.rotateRad(0.03f)
            } else if (cross < -0.01) { // isRight
                body.angularVelocity = -3.06f
                body.linearVelocity = body.linearVelocity.rotateRad(-0.03f)
            } else {
                body.angularVelocity = 0f
            }

            body.applyForceToCenter(Vector2(move.moveSpeed, 0f).rotateRad(body.angle), true)
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

    inner class Move : PlayerOperations {
        var moveDestination: Point? = null
            private set
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

        override fun skate(destination: Point, speed: Float): Move {
            this.moveDestination = destination
            this.moveSpeed = speed.coerceAtMost(MAX_ACCELERATION)
            return this
        }

        override fun pass(player: Player, force: Float): Move {
            this.passDestination = player.position
            this.passForce = force.coerceAtMost(MAX_PASS_FORCE)
            return this
        }

        override fun shoot(destination: Point, force: Float): Move {
            this.shotDestination = destination
            this.shotForce = force.coerceAtMost(MAX_SHOT_FORCE)
            return this
        }
    }
}
