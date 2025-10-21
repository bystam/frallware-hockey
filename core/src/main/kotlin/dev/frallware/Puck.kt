package dev.frallware

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.CircleShape
import com.badlogic.gdx.physics.box2d.World
import dev.frallware.HockeyPlayer.Companion.SHOT_FORCE

class Puck(world: World) {

    companion object {
        const val RADIUS = 0.4f

        val startingPoint = Vector2(Constants.WORLD_WIDTH / 2, Constants.WORLD_HEIGHT / 2 + 5f)
    }

    val body: Body

    init {
        // Create a dynamic body for the ball
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.DynamicBody
            position.set(startingPoint)
            linearDamping = 0.5f // Adds friction/slowdown when no force applied
        }

        body = world.createBody(bodyDef)

        // Create circular shape for the ball
        val circleShape = CircleShape().apply {
            radius = RADIUS
        }

        body.createFixture(circleShape, 1.0f).apply {
            restitution = 0.6f // Bounce
            friction = 0.0f
            userData = this@Puck
        }
        circleShape.dispose()
    }

    fun shoot(angle: Float) {
        val direction = Vector2(1f, 0f).rotateRad(angle)
        body.fixtureList.forEach { it.isSensor = false }
        body.applyLinearImpulse(direction.scl(SHOT_FORCE), Vector2.Zero, true)
    }

    fun drop() {
        body.fixtureList.forEach { it.isSensor = false }
    }

    fun slowDown() {
        body.linearVelocity = body.linearVelocity.scl(0.03f)
    }

    fun reset() {
        body.setTransform(startingPoint, 0f)
        body.linearVelocity = Vector2.Zero
    }

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = Color.BLACK
        shapeRenderer.circle(
            body.position.x,
            body.position.y,
            RADIUS,
            20
        )
    }
}
