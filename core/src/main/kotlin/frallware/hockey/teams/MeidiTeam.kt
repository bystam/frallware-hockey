package frallware.hockey.teams

import frallware.hockey.api.Color
import frallware.hockey.api.GameState
import frallware.hockey.api.GoalieOperations
import frallware.hockey.api.GoalieStrategy
import frallware.hockey.api.HockeyTeam
import frallware.hockey.api.Point
import frallware.hockey.api.SkaterOperations
import frallware.hockey.api.SkaterStrategy
import kotlin.random.Random

class MeidiTeam(override val color: Color) : HockeyTeam {

    override val name: String = "StupidTeam"
    override val goalie: GoalieStrategy = Goalie()
    override val skaters: List<SkaterStrategy> = listOf(Brute(), Shooty(), Defendy(), Shooty())

    class Skater : SkaterStrategy {
        override val name: String = "StupidPlayer"

        private val speed = Random.nextDouble(5.0, 20.0).toFloat()

        override fun step(state: GameState, operations: SkaterOperations) {
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

    class Brute : SkaterStrategy {
        override val name: String = "BrutePlayer"

        override fun step(state: GameState, operations: SkaterOperations) {
            val victim = state.puck.holder
            if (victim != null && state.enemyPlayers.contains(victim)) {
                operations.skate(victim.position, 25f)
            } else {
                // go stand in front of my own goal
                val defendPoint = getDefendPointInTheMiddle(state)
                operations.skate(defendPoint, 10f)
            }
        }
    }

    class Shooty : SkaterStrategy {
        override val name: String = "ShootyPlayer"

        override fun step(state: GameState, operations: SkaterOperations) {
            if (state.me.hasPuck && state.me.position.distanceTo(state.enemyGoalPosition) < 15f) {
                operations.shoot(state.enemyGoalPosition, 7f)
            } else if (state.me.hasPuck) {
                operations.skate(state.enemyGoalPosition, 20f)
            } else {
                operations.skate(state.puck.position, 25f)
            }
        }
    }

    class Defendy : SkaterStrategy {
        override val name: String = "DefendyPlayer"

        override fun step(state: GameState, operations: SkaterOperations) {
            // a little bit above the middle point between the goal and the center of the rink
            val defendPoint = getDefendPointInTheMiddle(state).offset(dy = 2f)

            if (state.me.position.distanceTo(defendPoint) > 10f) {
                operations.skate(defendPoint, 15f)
            } else if (state.enemyPlayers.contains(state.puck.holder)) {
                // stand in front of the puck holder
                val holder = state.puck.holder!!
                operations.skate(holder.position.offset(dx = if (isGoalOnLeftSide(state)) -1f else 1f), 10f)
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

private fun getDefendPointInTheMiddle(state: GameState): Point {
    val myGoalIsOnLeftSide = isGoalOnLeftSide(state)

    val defendPoint = if (myGoalIsOnLeftSide) {
        state.friendlyGoalPosition.offset(dx = 3f, dy = 0f)
    } else {
        state.friendlyGoalPosition.offset(dx = -3f, dy = 0f)
    }
    return defendPoint
}

private fun isGoalOnLeftSide(state: GameState): Boolean {
    val goalPos = state.friendlyGoalPosition
    val enemyGoalPos = state.enemyGoalPosition
    val myGoalIsOnLeftSide = goalPos.x < enemyGoalPos.x
    return myGoalIsOnLeftSide
}
