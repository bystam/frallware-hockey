package dev.frallware.teams

import com.badlogic.gdx.graphics.Color
import dev.frallware.api.GameState
import dev.frallware.api.HockeyTeam
import dev.frallware.api.PlayerOperations
import dev.frallware.api.PlayerStrategy
import kotlin.random.Random

class StupidTeam(override val color: Color) : HockeyTeam {

    override val name: String = "StupidTeam"
    override val goalie: PlayerStrategy = Player()
    override val players: List<PlayerStrategy> = listOf(Player(), Player(), Player(), Player())

    class Player : PlayerStrategy {
        override val name: String = "StupidPlayer"

        private val speed = Random.nextDouble(5.0, 20.0).toFloat()

        override fun step(state: GameState, operations: PlayerOperations) {
            if (state.me.hasPuck) {
                if (Random.nextInt(100) == 0) {
                    operations.shoot(state.enemyGoalPosition, 40f)
                }
                operations.skate(state.enemyGoalPosition, speed)
            } else {
                operations.skate(state.puck.position, speed)
            }
        }
    }
}
