package dev.frallware

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.physics.box2d.Box2D

/** [com.badlogic.gdx.ApplicationListener] implementation shared by all platforms. */
class Main : ApplicationAdapter() {

    private lateinit var hockeyRink: HockeyRink

    override fun create() {

        // Initialize Box2D world with gravity
        Box2D.init()
        hockeyRink = HockeyRink()
    }

    override fun resize(width: Int, height: Int) {
        hockeyRink.viewport.update(width, height, true)
    }

    override fun render() {
        hockeyRink.render()
    }

    override fun dispose() {
        hockeyRink.dispose()
    }
}
