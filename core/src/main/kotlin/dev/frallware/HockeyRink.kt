package dev.frallware

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport

class HockeyRink {
    companion object {
        const val WIDTH = 60f
        const val HEIGHT = 30f
    }

    val viewport: Viewport = FitViewport(WIDTH, HEIGHT)
    val world: World = World(Vector2.Zero, true)
    val body: Body = world.createRink()
    val player: HockeyPlayer = HockeyPlayer(world)

    private val shapeRenderer: ShapeRenderer = ShapeRenderer()

    fun render() {
        val delta = Gdx.graphics.deltaTime

        // Update player input
        player.update()

        // Step physics simulation
        world.step(1/60f, 6, 2)

        // Clear screen
        ScreenUtils.clear(Color.BLACK)
        viewport.apply()

        // Render shapes
        shapeRenderer.projectionMatrix = viewport.camera.combined

        // Enable blending for smooth circles
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)

        // Draw red container (centered)
        shapeRenderer.color = Color.RED
        shapeRenderer.rect(
            0f,
            0f,
            WIDTH,
            HEIGHT,
        )

        shapeRenderer.end()

        player.render(shapeRenderer)
    }

    fun dispose() {
        shapeRenderer.dispose()
        world.dispose()
    }
}


private fun World.createRink(): Body {
    // Create a static body for the container at origin
    val bodyDef = BodyDef().apply {
        type = BodyDef.BodyType.StaticBody
        position.set(0f, 0f)
    }

    val body = createBody(bodyDef)

    // Create rectangular boundary using chain of edges
    // Vertices are in world coordinates since body is at origin
    val vertices = listOf(
        Vector2(0f, 0f),
        Vector2(0f, HockeyRink.HEIGHT),
        Vector2(HockeyRink.WIDTH, HockeyRink.HEIGHT),
        Vector2(HockeyRink.WIDTH, 0f),
    )

    val shape = ChainShape()
    shape.createLoop(vertices.toTypedArray())

    body.createFixture(shape, 0f).apply {
        restitution = 0.2f // No bounce - absorbs impact
        friction = 0.6f // Low friction to slide along walls
    }

    shape.dispose()

    return body
}
