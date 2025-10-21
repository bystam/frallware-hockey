package dev.frallware

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
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

    val points = mutableMapOf(Side.Left to 0, Side.Right to 0)

    override fun create() {
        Box2D.init()

        // Enable blending for smooth circles
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)

        hockeyRink = HockeyRink(viewport, world)

        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact) {
                val aData = contact.fixtureA.userData
                val bData = contact.fixtureB.userData
                val puck = (aData as? Puck) ?: (bData as? Puck)
                val player = (aData as? HockeyPlayer) ?: (bData as? HockeyPlayer)
                val goal = (aData as? Goal) ?: (bData as? Goal)
                val goalSensor = (aData as? Goal.Sensor) ?: (bData as? Goal.Sensor)

                if (puck != null && player != null) {
                    player.takePuck(puck)
                }
                if (puck != null && goal != null) {
                    puck.slowDown()
                }
                if (puck != null && goalSensor != null) {
                    points[goalSensor.side.opponent] = points[goalSensor.side.opponent]!! + 1
                    println(points)
                }
            }

            override fun endContact(contact: Contact) {
            }

            override fun preSolve(
                contact: Contact,
                oldManifold: Manifold
            ) {
            }

            override fun postSolve(
                contact: Contact,
                impulse: ContactImpulse
            ) {
            }
        })
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
