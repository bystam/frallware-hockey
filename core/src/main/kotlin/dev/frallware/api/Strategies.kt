package dev.frallware.api

import com.badlogic.gdx.graphics.Color

interface HockeyTeam {
    val name: String
    val color: Color // TODO no gdx dependency

    val goalie: GoalieStrategy
    val skaters: List<SkaterStrategy>
}

sealed interface PlayerStrategy {
    val name: String
}

interface SkaterStrategy : PlayerStrategy {
    fun step(state: GameState, operations: SkaterOperations)
}

interface SkaterOperations {
    fun skate(destination: Point, speed: Float): SkaterOperations
    fun pass(player: Player, force: Float): SkaterOperations
    fun shoot(destination: Point, force: Float): SkaterOperations
}

interface GoalieStrategy : PlayerStrategy {
    fun step(state: GameState, operations: GoalieOperations)
}

interface GoalieOperations : SkaterOperations {
    fun glide(destination: Point, speed: Float): GoalieOperations
    fun face(point: Point): GoalieOperations
}
