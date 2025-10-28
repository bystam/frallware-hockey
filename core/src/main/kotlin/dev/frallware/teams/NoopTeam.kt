package dev.frallware.teams

import com.badlogic.gdx.graphics.Color
import dev.frallware.api.GameState
import dev.frallware.api.HockeyTeam
import dev.frallware.api.PlayerOperations
import dev.frallware.api.PlayerStrategy

class NoopTeam : HockeyTeam {

    override val name: String = "NoopTeam"
    override val color: Color = Color.RED
    override val goalie: PlayerStrategy = Player
    override val players: List<PlayerStrategy> = listOf(Player, Player, Player, Player)

    object Player : PlayerStrategy {
        override val name: String = "NoopPlayer"

        override fun step(state: GameState, operations: PlayerOperations) {
        }
    }
}
