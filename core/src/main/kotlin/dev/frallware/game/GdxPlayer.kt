package dev.frallware.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.World
import dev.frallware.api.Player
import dev.frallware.api.PlayerOperations
import dev.frallware.api.PlayerStrategy
import dev.frallware.api.Point

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
        const val RADIUS = 1f
        const val SHOT_FORCE = 20f
        const val MAX_VELOCITY = 20f
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
        circleShape.dispose()
    }

    fun reset() {
        body.setTransform(startingPoint, startingAngle)
        body.linearVelocity = Vector2.Zero
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

            val angleToTarget = MathUtils.atan2(destinationDirection.y, destinationDirection.x)
            val angleDiff = angleToTarget - angle
            val angleDiff2 = ((angleDiff + MathUtils.PI) % (2 * MathUtils.PI) - MathUtils.PI)
            if (angleDiff2 > 0.0001f) {
                body.setTransform(body.position, angle + 0.03f)
            }
            if (angleDiff2 < -0.0001f) {
                body.setTransform(body.position, angle - 0.03f)
            }

            body.applyForceToCenter(facingDirection.scl(move.moveSpeed), true)
        }

        move.shotDestination?.let { destination ->
            val position = body.worldCenter
            val destinationDirection = Vector2(destination.x - position.x, destination.y - position.y).nor()
            puck?.shoot(destinationDirection, move.shotForce)
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
            val puckOffset = Vector2(0.8f * RADIUS, -0.8f * RADIUS).rotateRad(body.angle)
            val worldPos = body.position.cpy().add(puckOffset)
            puck.body.setTransform(worldPos, body.angle)
            puck.body.linearVelocity = body.linearVelocity
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = color
        shapeRenderer.circle(
            body.position.x,
            body.position.y,
            RADIUS,
            20
        )

        val headOffset = Vector2(0.4f, 0f).rotateRad(body.angle)

        shapeRenderer.color = Color.GREEN
        shapeRenderer.circle(
            body.position.x + headOffset.x,
            body.position.y + headOffset.y,
            0.4f,
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
            this.moveSpeed = speed
            return this
        }

        override fun pass(player: Player, force: Float): Move {
            this.passDestination = player.position
            this.passForce = force
            return this
        }

        override fun shoot(destination: Point, force: Float): Move {
            this.shotDestination = destination
            this.shotForce = force
            return this
        }
    }
}
