package dev.frallware.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.World
import dev.frallware.Constants
import dev.frallware.api.Player
import dev.frallware.api.PlayerOperations
import dev.frallware.api.PlayerStrategy
import dev.frallware.api.Point

class GdxPlayer(
    world: World,
    val side: Side,
    val strategy: PlayerStrategy,
    val stateMaker: (player: GdxPlayer) -> StateImpl
) {
    companion object {
        const val RADIUS = 1f
        const val SHOT_FORCE = 20f
        const val MAX_VELOCITY = 20f
    }

    private val startingPoint: Vector2
    private val startingAngle: Float

    val body: Body

    var puck: GdxPuck? = null
        private set

    private val state: StateImpl by lazy(LazyThreadSafetyMode.NONE) { stateMaker(this) }

    init {
        when (side) {
            Side.Left -> {
                startingPoint = Vector2(Constants.WORLD_WIDTH / 2 - 5f, Constants.WORLD_HEIGHT / 2)
                startingAngle = 0f
            }

            Side.Right -> {
                startingPoint = Vector2(Constants.WORLD_WIDTH / 2 + 5f, Constants.WORLD_HEIGHT / 2)
                startingAngle = MathUtils.PI
            }
        }

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

        body.setTransform(body.position, angle + move.rotation)

        move.moveDestination?.let {
            body.applyForceToCenter(Vector2(it.x, it.y).scl(move.moveSpeed), true)
        }

        move.shotDestination?.let {
            puck?.shoot(Vector2(it.x, it.y), move.shotForce)
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
        shapeRenderer.color = when (side) {
            Side.Left -> Color.BLUE
            Side.Right -> Color.MAROON
        }
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

        var rotation: Float = 0f
            private set

        override fun move(destination: Point, speed: Float): Move {
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

        override fun turn(angle: Float): Move {
            this.rotation += angle
            return this
        }
    }
}
