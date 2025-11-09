package dev.frallware.teams

import com.badlogic.gdx.graphics.Color
import dev.frallware.api.GameState
import dev.frallware.api.GoalieOperations
import dev.frallware.api.GoalieStrategy
import dev.frallware.api.HockeyTeam
import dev.frallware.api.Point
import dev.frallware.api.SkaterOperations
import dev.frallware.api.SkaterStrategy
import kotlin.random.Random

class StupidTeam(override val color: Color) : HockeyTeam {

    override val name: String = "StupidTeam"
    override val goalie: GoalieStrategy = Goalie()
    override val skaters: List<SkaterStrategy> = listOf(Skater(), Skater(), Skater(), Skater())

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
