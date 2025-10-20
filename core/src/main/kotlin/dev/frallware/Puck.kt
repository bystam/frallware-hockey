package dev.frallware

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.physics.box2d.World

class Puck(world: World) {

    companion object {
        const val RADIUS = 0.4f
    }

    val body: Body

    init {
        // Create a dynamic body for the ball
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(Constants.WORLD_WIDTH / 2, Constants.WORLD_HEIGHT / 2 + 5f)
            linearDamping = 0.5f // Adds friction/slowdown when no force applied
        }

        body = world.createBody(bodyDef)
        body.userData = this

        // Create circular shape for the ball
        val circleShape = CircleShape().apply {
            radius = RADIUS
        }

        val fixtureDef = FixtureDef().apply {
            shape = circleShape
            density = 1.0f
            restitution = 1.0f // Bounce
            friction = 0.0f
        }

        body.createFixture(fixtureDef)
        circleShape.dispose()
    }

    fun render(shapeRenderer: ShapeRenderer) = shapeRenderer.batch(ShapeRenderer.ShapeType.Filled) {
        shapeRenderer.color = Color.BLACK
        shapeRenderer.circle(
            body.position.x,
            body.position.y,
            RADIUS,
            20
        )
    }
}
