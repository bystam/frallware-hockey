package dev.frallware

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import dev.frallware.HockeyRink.Companion.HEIGHT
import dev.frallware.HockeyRink.Companion.WIDTH

/**
 * TODO:
 * - Reset game upon goal
 */
class Main : ApplicationAdapter() {

    val viewport: FitViewport = FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT)
    val world: World = World(Vector2.Zero, true)
    private lateinit var hockeyRink: HockeyRink

    override fun create() {
        Box2D.init()

        // Enable blending for smooth circles
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        hockeyRink = HockeyRink(viewport, world)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render() {
        hockeyRink.update()

        // Step physics simulation
        world.step(1 / 60f, 6, 2)

        ScreenUtils.clear(Color.BLACK)
        viewport.apply()

        hockeyRink.render()
    }

    override fun dispose() {
        hockeyRink.dispose()
    }
}

object Constants {
    const val WORLD_WIDTH: Float = WIDTH + 10
    const val WORLD_HEIGHT: Float = HEIGHT + 10
    val worldCenter = Vector2(WORLD_WIDTH / 2, WORLD_HEIGHT / 2)
}
