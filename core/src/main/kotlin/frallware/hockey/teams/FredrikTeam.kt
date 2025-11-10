package frallware.hockey.teams

import frallware.hockey.api.Color
import frallware.hockey.api.GameState
import frallware.hockey.api.GoalieOperations
import frallware.hockey.api.GoalieStrategy
import frallware.hockey.api.HockeyTeam
import frallware.hockey.api.Point
import frallware.hockey.api.SkaterOperations
import frallware.hockey.api.SkaterStrategy
import frallware.hockey.api.Vector

class FredrikTeam(override val color: Color) : HockeyTeam {

    override val name: String = "StupidTeam"
    override val goalie: GoalieStrategy = Goalie()
    override val skaters: List<SkaterStrategy> = listOf(
        Attacker("Attacker 1", offsetY = 5f),
        Attacker("Attacker 2", offsetY = -5f),
        Defender("Defender 1", goalOffsetY = -2f),
        Defender("Defender 2", goalOffsetY = -2f),
    )

    class Attacker(
        override val name: String,
        val offsetY: Float,
    ) : SkaterStrategy {

        override fun step(state: GameState, operations: SkaterOperations) {
            if (state.me.hasPuck) {
                operations.skate(state.enemyGoalPosition, 25f)

                val distanceBetweenGoals = state.friendlyGoalPosition.distanceTo(state.enemyGoalPosition)
                val distanceToTarget = state.me.position.distanceTo(state.enemyGoalPosition)
                val distanceToOwnGoal = state.me.position.distanceTo(state.friendlyGoalPosition)
                if (distanceToOwnGoal > distanceBetweenGoals) {
                    // Likely behind the goals - pass someone
                    val someoneElse = state.friendlyPlayers.first { it != state.me }
                    operations.pass(someoneElse, 5f)
                }
                if (distanceToTarget > distanceBetweenGoals) {
                    // very far away - pass!
                    val someoneElse = state.friendlyPlayers.first { it != state.me }
                    operations.pass(someoneElse, 5f)
                }
                if (distanceToTarget < 10f) {
                    operations.shoot(state.enemyGoalPosition, 10f)
                }

            } else {
                val closestFriendlyPlayers = state.friendlyPlayers
                    .sortedBy { it.position.distanceTo(state.puck.position) }
                    .take(2)

                if (state.me in closestFriendlyPlayers) { // chase puck
                    operations.skate(state.puck.position, 25f)
                } else { // or stay at resting point

                    val forward = state.enemyGoalPosition - state.friendlyGoalPosition
                    val goalOffset = forward * 0.8f + Vector(dy = offsetY)
                    val myPoint = state.friendlyGoalPosition + goalOffset

                    operations.skate(myPoint, state.me.position.distanceTo(myPoint))
                }
            }
        }
    }

    class Defender(
        override val name: String,
        val goalOffsetY: Float,
    ) : SkaterStrategy {

        override fun step(state: GameState, operations: SkaterOperations) {
            if (state.me.hasPuck) {
                operations.skate(state.enemyGoalPosition, 25f)

                val distanceBetweenGoals = state.friendlyGoalPosition.distanceTo(state.enemyGoalPosition)
                val distanceToTarget = state.me.position.distanceTo(state.enemyGoalPosition)
                val distanceToOwnGoal = state.me.position.distanceTo(state.friendlyGoalPosition)

                val friendlyClosestToGoal = state.friendlyPlayers.minBy {
                    it.position.distanceTo(state.enemyGoalPosition)
                }

                if (distanceToTarget < distanceBetweenGoals) {
                    operations.pass(friendlyClosestToGoal, 3f)
                }
            } else {
                val closestFriendlyPlayers = state.friendlyPlayers
                    .sortedBy { it.position.distanceTo(state.puck.position) }
                    .take(1)

                if (state.me in closestFriendlyPlayers) { // chase puck
                    operations.skate(state.puck.position, 25f)
                } else { // or stay at resting point

                    val forward = state.enemyGoalPosition - state.friendlyGoalPosition
                    val goalOffset = forward * 0.1f + Vector(dy = goalOffsetY)
                    val myPoint = state.friendlyGoalPosition + goalOffset

                    operations.skate(myPoint, state.me.position.distanceTo(myPoint))
                }
            }
        }
    }

    class Goalie : GoalieStrategy {
        override val name: String = "StupidGoalie"
        lateinit var startingPoint: Point
        lateinit var oneSide: Point
        lateinit var anotherSide: Point

        override fun step(state: GameState, operations: GoalieOperations) {
            if (!::startingPoint.isInitialized) {
                startingPoint = state.me.position
                oneSide = startingPoint.offset(dy = 3f)
                anotherSide = startingPoint.offset(dy = -3f)
            }
            operations.face(state.puck.position)

            val time = (System.currentTimeMillis() / 1000) % 4
            if (time < 2) {
                operations.glide(oneSide, 2f)
            } else {
                operations.glide(anotherSide, 2f)
            }
        }
    }
}
