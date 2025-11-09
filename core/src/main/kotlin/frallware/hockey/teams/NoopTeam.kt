package frallware.hockey.teams

import frallware.hockey.api.Color
import frallware.hockey.api.GameState
import frallware.hockey.api.GoalieOperations
import frallware.hockey.api.GoalieStrategy
import frallware.hockey.api.HockeyTeam
import frallware.hockey.api.SkaterOperations
import frallware.hockey.api.SkaterStrategy

class NoopTeam : HockeyTeam {

    override val name: String = "NoopTeam"
    override val color: Color = Color(1f, 0f, 0f)
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
