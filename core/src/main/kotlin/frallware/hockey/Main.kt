package frallware.hockey

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Box2D
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.FitViewport
import frallware.hockey.game.GdxGame
import frallware.hockey.game.GdxRink
import frallware.hockey.teams.StupidTeam

/**
 * TODO:
 * - Make puck not able to travel through things
 * - make scores overlay
 * - Rewrite how skate speed works:
 *    - Instead of treating speed as acceleration, we treat it as speed
 *    - If the speed is lower than wanted, we apply acceleration,
 *    - If the speed is higher than wanted, we apply break
 * - Add names to players
 * - make shot/pass accuracy worse at wide angles
 * - paint circles
 * - make goalie bigger maybe?
 * - don't allow puck pickup from behind
 * - animate shooting puck
 * - make puck pickup something that one has to act upon instead of automatic? (except for goalies maybe?)
 * - generally improve rendering
 * - [frallware.hockey.api.GameState].isFaceOff?
 * - Improve puck going through things...
 */
class Main : ApplicationAdapter() {

    val viewport: FitViewport = FitViewport(Constants.WORLD_WIDTH, Constants.WORLD_HEIGHT)
    val world: World = World(Vector2.Zero, true)

    private lateinit var game: GdxGame

    override fun create() {
        Box2D.init()

        game = GdxGame(
            world = world,
            viewport = viewport,
            leftTeam = StupidTeam(frallware.hockey.api.Color(0.8f, 0.2f, 0.2f)),
            rightTeam = StupidTeam(frallware.hockey.api.Color(0.2f, 0.2f, 0.8f))
        )
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun render() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        game.render()
    }

    override fun dispose() {
        game.dispose()
    }
}

object Constants {
    const val PADDING: Float = 5f
    const val WORLD_WIDTH: Float = GdxRink.WIDTH + 2 * PADDING
    const val WORLD_HEIGHT: Float = GdxRink.HEIGHT + 2 * PADDING
    val worldCenter = Vector2(WORLD_WIDTH / 2, WORLD_HEIGHT / 2)
}
