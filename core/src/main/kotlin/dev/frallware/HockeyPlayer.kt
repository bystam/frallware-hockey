package dev.frallware

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.World

class HockeyPlayer(world: World) {
    companion object {
        const val RADIUS = 3f
    }

    val body: Body

    init {
        // Create a dynamic body for the ball
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(HockeyRink.WIDTH / 2, HockeyRink.HEIGHT / 2) // Start at center of screen
        }

        body = world.createBody(bodyDef)

        // Create circular shape for the ball
        val circleShape = CircleShape().apply {
            radius = RADIUS
        }

        val fixtureDef = FixtureDef().apply {
            shape = circleShape
            density = 1.0f
            restitution = 0.9f // High bounciness
            friction = 0.3f
        }

        body.createFixture(fixtureDef)
        circleShape.dispose()
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
