package dev.frallware.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.World
import dev.frallware.Constants

class GdxRink(
    val world: World,
) {
    companion object {
        const val WIDTH = 60f
        const val HEIGHT = 30f
    }

    val body: Body = world.createBody(BodyDef().apply {
        type = BodyDef.BodyType.StaticBody
        position.set(Constants.worldCenter)
    })

    private val rect: RoundedRect = RoundedRect.create(WIDTH, HEIGHT, 3f, 20)

    private val absoluteEdgePoints: List<Vector2> by lazy(LazyThreadSafetyMode.NONE) {
        val rinkCenter = body.worldCenter
        rect.allPoints.map { rinkCenter + it }
    }

    init {
        // Create a static body for the container at origin
        val shape = ChainShape()
        shape.createLoop(rect.allPoints.toTypedArray())

        body.createFixture(shape, 0f).apply {
            restitution = 0.2f // No bounce - absorbs impact
            friction = 0.6f
        }

        shape.dispose()
    }

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = Color.RED
        for ((from, to) in absoluteEdgePoints.windowed(2)) {
            shapeRenderer.rectLine(from, to, 0.3f)
        }
        shapeRenderer.rectLine(absoluteEdgePoints.last(), absoluteEdgePoints.first(), 0.3f)
    }
}
