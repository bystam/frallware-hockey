package dev.frallware.teams

import com.badlogic.gdx.graphics.Color
import dev.frallware.api.GameState
import dev.frallware.api.HockeyTeam
import dev.frallware.api.PlayerOperations
import dev.frallware.api.PlayerStrategy

class StupidTeam : HockeyTeam {

    override val name: String = "StupidTeam"
    override val color: Color = Color.BLUE
    override val goalie: PlayerStrategy = Player
    override val players: List<PlayerStrategy> = listOf(Player, Player, Player, Player)

    object Player : PlayerStrategy {
        override val name: String = "StupidPlayer"

        override fun step(state: GameState, operations: PlayerOperations) {
            if (state.me.hasPuck) {
                operations.skate(state.enemyGoalPosition, 20f)
            } else {
                operations.skate(state.puck.position, 20f)
            }
        }
    }
}
