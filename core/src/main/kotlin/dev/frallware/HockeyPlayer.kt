package dev.frallware

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
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
            position.set(HockeyRink.WIDTH / 2, HockeyRink.HEIGHT / 2) // Start at center of screen
//            linearDamping = 2.0f // Adds friction/slowdown when no force applied
        }

        body = world.createBody(bodyDef)

        // Create circular shape for the ball
        val circleShape = CircleShape().apply {
            radius = RADIUS
        }

        val fixtureDef = FixtureDef().apply {
            shape = circleShape
            density = 1.0f
            restitution = 0.2f // Bounce
            friction = 0.6f // Low friction to slide along walls
        }

        body.createFixture(fixtureDef)
        circleShape.dispose()
    }

    fun update() {
        val force = Vector2(0f, 0f)

        // Check arrow key inputs and apply forces
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            force.x -= ACCELERATION_FORCE
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            force.x += ACCELERATION_FORCE
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            force.y += ACCELERATION_FORCE
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            force.y -= ACCELERATION_FORCE
        }

        // Apply force to the body center
        body.applyForceToCenter(force, true)

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

        shapeRenderer.end()
    }
}
