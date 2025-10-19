package dev.frallware

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.FitViewport

class HockeyRink(
    val viewport: FitViewport,
    val world: World,
) {
    companion object {
        const val WIDTH = 60f
        const val HEIGHT = 30f

        val bottomLeft = Vector2(-WIDTH / 2, -HEIGHT / 2)
        val topLeft = Vector2(-WIDTH / 2, HEIGHT / 2)
        val topRight = Vector2(WIDTH / 2, HEIGHT / 2)
        val bottomRight = Vector2(WIDTH / 2, -HEIGHT / 2)
    }

    val body: Body = createRink()
    val player: HockeyPlayer = HockeyPlayer(world)
    private val rinkCenter: Vector2 = body.worldCenter.cpy()

    private val shapeRenderer: ShapeRenderer = ShapeRenderer()

    fun update() {
        player.update()
    }

    fun render() {
        shapeRenderer.projectionMatrix = viewport.camera.combined
        val bl = rinkCenter + bottomLeft
        val tl = rinkCenter + topLeft
        val tr = rinkCenter + topRight
        val br = rinkCenter + bottomRight

        shapeRenderer.batch(ShapeRenderer.ShapeType.Filled) {
            color = Color.WHITE.withAlpha(0.8f)
            rect(bl.x, bl.y, WIDTH, HEIGHT)

            color = Color.RED
            rectLine(bl, tl, 0.3f)
            rectLine(tl, tr, 0.3f)
            rectLine(tr, br, 0.3f)
            rectLine(br, bl, 0.3f)
        }

        player.render(shapeRenderer)
    }

    fun dispose() {
        shapeRenderer.dispose()
        world.dispose()
    }

    private fun createRink(): Body {
        // Create a static body for the container at origin
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            position.set(Constants.worldCenter)
        }

        val body = world.createBody(bodyDef)

        val shape = ChainShape()
        shape.createLoop(
            arrayOf(bottomLeft, topLeft, topRight, bottomRight)
        )

        body.createFixture(shape, 0f).apply {
            restitution = 0.2f // No bounce - absorbs impact
            friction = 0.6f
        }

        shape.dispose()

        return body
    }
}
