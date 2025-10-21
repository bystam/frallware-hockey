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

class HockeyPlayer(world: World) {
    companion object {
        const val RADIUS = 1f
        const val ACCELERATION_FORCE = 40f
        const val SHOT_FORCE = 20f
        const val MAX_VELOCITY = 20f

        val startingPoint = Vector2(Constants.WORLD_WIDTH / 2, Constants.WORLD_HEIGHT / 2 - 5f)
    }

    val body: Body

    private var puck: Puck? = null

    init {
        // Create a dynamic body for the ball
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(startingPoint)
            angle = MathUtils.HALF_PI
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
        body.setTransform(startingPoint, MathUtils.HALF_PI)
        body.linearVelocity = Vector2.Zero
        puck?.drop()
        puck = null
    }

    fun takePuck(puck: Puck) {
        this.puck = puck
        puck.body.fixtureList.forEach { it.isSensor = true }
    }

    fun update() {
        val angle = body.angle
        val forward = Vector2(MathUtils.cos(angle), MathUtils.sin(angle))

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            body.setTransform(body.position, angle + 0.05f)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            body.setTransform(body.position, angle + -0.05f)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            body.applyForceToCenter(forward.scl(ACCELERATION_FORCE), true)
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            body.applyForceToCenter(forward.scl(-ACCELERATION_FORCE), true)
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
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
        shapeRenderer.color = Color.BLUE
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
