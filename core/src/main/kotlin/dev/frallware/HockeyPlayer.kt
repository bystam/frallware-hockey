package dev.frallware

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.World

class HockeyPlayer(world: World, val side: Side) {
    companion object {
        const val RADIUS = 1f
        const val ACCELERATION_FORCE = 40f
        const val SHOT_FORCE = 20f
        const val MAX_VELOCITY = 20f
    }

    private val startingPoint: Vector2
    private val startingAngle: Float
    private val leftKey: Int
    private val rightKey: Int
    private val forwardKey: Int
    private val backwardsKey: Int
    private val shootKey: Int

    val body: Body

    private var puck: Puck? = null

    init {
        when (side) {
            Side.Left -> {
                startingPoint = Vector2(Constants.WORLD_WIDTH / 2 - 5f, Constants.WORLD_HEIGHT / 2)
                startingAngle = 0f
                leftKey = Input.Keys.LEFT
                rightKey = Input.Keys.RIGHT
                forwardKey = Input.Keys.UP
                backwardsKey = Input.Keys.DOWN
                shootKey = Input.Keys.SPACE
            }

            Side.Right -> {
                startingPoint = Vector2(Constants.WORLD_WIDTH / 2 + 5f, Constants.WORLD_HEIGHT / 2)
                startingAngle = MathUtils.PI
                leftKey = Input.Keys.A
                rightKey = Input.Keys.D
                forwardKey = Input.Keys.W
                backwardsKey = Input.Keys.S
                shootKey = Input.Keys.SHIFT_LEFT
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
            userData = this@HockeyPlayer
        }
        circleShape.dispose()
    }

    fun reset() {
        body.setTransform(startingPoint, startingAngle)
        body.linearVelocity = Vector2.Zero
        dropPuck()
    }

    fun takePuck(puck: Puck) {
        this.puck = puck
        puck.body.fixtureList.forEach { it.isSensor = true }
    }

    fun dropPuck() {
        puck = null
    }

    fun update() {
        val angle = body.angle
        val forward = Vector2(MathUtils.cos(angle), MathUtils.sin(angle))

        if (Gdx.input.isKeyPressed(leftKey)) {
            body.setTransform(body.position, angle + 0.05f)
        }
        if (Gdx.input.isKeyPressed(rightKey)) {
            body.setTransform(body.position, angle + -0.05f)
        }
        if (Gdx.input.isKeyPressed(forwardKey)) {
            body.applyForceToCenter(forward.scl(ACCELERATION_FORCE), true)
        } else if (Gdx.input.isKeyPressed(backwardsKey)) {
            body.applyForceToCenter(forward.scl(-ACCELERATION_FORCE), true)
        }
        if (Gdx.input.isKeyPressed(shootKey)) {
            puck?.shoot(body.angle)
            puck = null
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
}
