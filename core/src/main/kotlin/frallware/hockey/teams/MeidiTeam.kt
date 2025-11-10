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
            if (state.me.hasPuck) {
                operations.shoot(state.enemyGoalPosition, 5f)
            } else if (victim != null && state.enemyPlayers.contains(victim)) {
                operations.skate(victim.position, 25f)
            } else {
                // go stand in front of my own goal
                val defendPoint = getDefendPointInTheMiddle(state, 10f)
                operations.skate(defendPoint, 10f)
            }
        }
    }

    class Shooty : SkaterStrategy {
        override val name: String = "ShootyPlayer"

        override fun step(state: GameState, operations: SkaterOperations) {
            val isMyGoalOnLeftSide = isGoalOnLeftSide(state)
            if (state.me.hasPuck && state.me.position.distanceTo(state.enemyGoalPosition) < 15f && amIInFrontOfGoal(state, isMyGoalOnLeftSide)) {
                operations.shoot(state.enemyGoalPosition, 7f)
            } else if (state.me.hasPuck) {
                operations.skate(getShootyPointInTheMiddle(state), 25f)
            } else if (state.enemyPlayers.contains(state.puck.holder)){
                operations.skate(state.puck.position, 25f)
            } else if (state.friendlyPlayers.contains(state.puck.holder)) {
                // keep a respectful distance
                val holder = state.puck.holder!!
                operations.skate(holder.position.offset(dy = 5f), 15f)
            } else {
                operations.skate(state.puck.position, 25f)
            }
        }

        private fun amIInFrontOfGoal(state: GameState, myGoalOnLeftSide: Boolean): Boolean {
            return if (myGoalOnLeftSide) {
                state.me.position.x < state.enemyGoalPosition.x
            } else {
                state.me.position.x > state.enemyGoalPosition.x
            }
        }
    }

    class Defendy : SkaterStrategy {
        override val name: String = "DefendyPlayer"

        override fun step(state: GameState, operations: SkaterOperations) {
            // a little bit above the middle point between the goal and the center of the rink
            val defendPoint = getDefendPointInTheMiddle(state, 5f).offset(dy = 2f)

            when {
                state.me.hasPuck && state.me.position.distanceTo(defendPoint) < 10f -> {
                    operations.shoot(state.enemyGoalPosition, 10f)
                }
                state.me.position.distanceTo(defendPoint) > 10f -> {
                    operations.skate(defendPoint, 15f)
                }
                state.enemyPlayers.contains(state.puck.holder) -> {
                    val holder = state.puck.holder!!
                    operations.skate(holder.position, 25f)
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

private fun getShootyPointInTheMiddle(state: GameState): Point {
    val goalPosition = state.enemyGoalPosition
    val enemyGoalOnRight = isGoalOnLeftSide(state)
    val shootyPoint = if (enemyGoalOnRight) {
        goalPosition.offset(dx = -5f, dy = 0f)
    } else {
        goalPosition.offset(dx = 5f, dy = 0f)
    }

    return shootyPoint
}

private fun getDefendPointInTheMiddle(state: GameState, xOffset: Float): Point {
    val myGoalIsOnLeftSide = isGoalOnLeftSide(state)

    val defendPoint = if (myGoalIsOnLeftSide) {
        state.friendlyGoalPosition.offset(dx = xOffset, dy = 0f)
    } else {
        state.friendlyGoalPosition.offset(dx = -xOffset, dy = 0f)
    }
    return defendPoint
}

private fun isGoalOnLeftSide(state: GameState): Boolean {
    val goalPos = state.friendlyGoalPosition
    val enemyGoalPos = state.enemyGoalPosition
    val myGoalIsOnLeftSide = goalPos.x < enemyGoalPos.x
    return myGoalIsOnLeftSide
}
