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
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.World

class HockeyPlayer(world: World) {
    companion object {
        const val RADIUS = 3f
        const val ACCELERATION_FORCE = 400f
        const val MAX_VELOCITY = 20f
    }

    val body: Body

    init {
        // Create a dynamic body for the ball
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(Constants.WORLD_WIDTH / 2, Constants.WORLD_HEIGHT / 2) // Start at center of screen
            angle = MathUtils.HALF_PI
            linearDamping = 0.5f // Adds friction/slowdown when no force applied
        }

        body = world.createBody(bodyDef)
        body.isFixedRotation = true

        // Create circular shape for the ball
        val circleShape = CircleShape().apply {
            radius = RADIUS
        }

        val fixtureDef = FixtureDef().apply {
            shape = circleShape
            density = 1.0f
            restitution = 0.2f // Bounce
            friction = 0.6f
        }

        body.createFixture(fixtureDef)
        circleShape.dispose()
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
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            body.applyForceToCenter(forward.scl(-ACCELERATION_FORCE), true)
        }

        // Clamp velocity to max speed
        val velocity = body.linearVelocity
        val speed = velocity.len()
        if (speed > MAX_VELOCITY) {
            velocity.scl(MAX_VELOCITY / speed)
            body.linearVelocity = velocity
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Draw blue ball
        shapeRenderer.color = Color.BLUE
        shapeRenderer.circle(
            body.position.x,
            body.position.y,
            RADIUS,
            20
        )

        val headOffset = Vector2(1f, 0f).rotateRad(body.angle)

        shapeRenderer.color = Color.GREEN
        shapeRenderer.circle(
            body.position.x + headOffset.x,
            body.position.y + headOffset.y,
            1f,
            20,
        )

        shapeRenderer.end()
    }
}
