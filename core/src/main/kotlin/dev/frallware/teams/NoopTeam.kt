package dev.frallware.teams

import com.badlogic.gdx.graphics.Color
import dev.frallware.api.GameState
import dev.frallware.api.GoalieOperations
import dev.frallware.api.GoalieStrategy
import dev.frallware.api.HockeyTeam
import dev.frallware.api.SkaterOperations
import dev.frallware.api.SkaterStrategy

class NoopTeam : HockeyTeam {

    override val name: String = "NoopTeam"
    override val color: Color = Color.RED
    override val goalie: GoalieStrategy = Goalie
    override val skaters: List<SkaterStrategy> = listOf(Skater, Skater, Skater, Skater)

    object Skater : SkaterStrategy {
        override val name: String = "NoopPlayer"

        override fun step(state: GameState, operations: SkaterOperations) {
        }
    }

    object Goalie : GoalieStrategy {
        override val name: String = "Goalie"

        override fun step(state: GameState, operations: GoalieOperations) {
        }
    }
}
