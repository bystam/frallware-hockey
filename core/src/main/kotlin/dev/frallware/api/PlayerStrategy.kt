package dev.frallware.api

import com.badlogic.gdx.graphics.Color

interface HockeyTeam {
    val name: String
    val color: Color // TODO no gdx dependency

    val goalie: PlayerStrategy
    val players: List<PlayerStrategy>
}

interface PlayerStrategy {
    val name: String

    fun step(state: GameState, operations: PlayerOperations)
}

interface PlayerOperations {
    fun skate(destination: Point, speed: Float): PlayerOperations
    fun pass(player: Player, force: Float): PlayerOperations
    fun shoot(destination: Point, force: Float): PlayerOperations
}
