package frallware.hockey.teams

import frallware.hockey.Constants
import frallware.hockey.api.Color
import frallware.hockey.api.GameState
import frallware.hockey.api.GoalieOperations
import frallware.hockey.api.GoalieStrategy
import frallware.hockey.api.HockeyTeam
import frallware.hockey.api.Point
import frallware.hockey.api.SkaterOperations
import frallware.hockey.api.SkaterStrategy
import kotlin.random.Random

class FredrikTeam(override val color: Color) : HockeyTeam {

    private val restingPoints = listOf(
        Point(x = 1 * Constants.WORLD_WIDTH / 3, y = 2 * Constants.WORLD_HEIGHT / 3), // topLeft
        Point(x = 2 * Constants.WORLD_WIDTH / 3, y = 2 * Constants.WORLD_HEIGHT / 3), // topRight
        Point(x = 1 * Constants.WORLD_WIDTH / 3, y = 1 * Constants.WORLD_HEIGHT / 3), // bottomLeft
        Point(x = 2 * Constants.WORLD_WIDTH / 3, y = 1 * Constants.WORLD_HEIGHT / 3), // bottomRight
    )

    override val name: String = "StupidTeam"
    override val goalie: GoalieStrategy = Goalie()
    override val skaters: List<SkaterStrategy> = restingPoints.mapIndexed { index, point ->
        Skater("Player $index", point)
    }

    class Skater(
        override val name: String,
        val restingPoint: Point,
    ) : SkaterStrategy {
        private val speed = Random.nextDouble(5.0, 20.0).toFloat()

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
                if (distanceToTarget < 10f) {
                    operations.skate(state.enemyGoalPosition, 25f)
                }

            } else {
                val closestFriendlyPlayer = state.friendlyPlayers
                    .minBy { it.position.distanceTo(state.puck.position) }

                if (closestFriendlyPlayer == state.me) { // chase puck
                    operations.skate(state.puck.position, 25f)
                } else { // or stay at resting point
                    operations.skate(restingPoint, state.me.position.distanceTo(restingPoint))
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
