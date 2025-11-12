package frallware.hockey

import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.physics.box2d.Box2D
import frallware.hockey.api.Color
import frallware.hockey.game.GdxGame
import frallware.hockey.teams.FredrikTeam
import frallware.hockey.teams.MeidiTeam

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
 * - Stop making players spin around when they are standing where they want to be
 */
class Main : Game() {

    override fun create() {
        Box2D.init()

        screen = GdxGame(
            leftTeam = MeidiTeam(Color(0.8f, 0.2f, 0.2f)),
            rightTeam = FredrikTeam(Color(0.2f, 0.2f, 0.8f))
        ) {
            screen.dispose()
            println("Winner: $it")
        }
    }

    override fun render() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        super.render()
    }

    override fun dispose() {
        screen.dispose()
    }
}
