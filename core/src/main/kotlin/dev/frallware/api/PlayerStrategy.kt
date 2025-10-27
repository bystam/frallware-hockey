package dev.frallware.api

interface PlayerStrategy {
    val name: String

    fun step(state: GameState, operations: PlayerOperations)
}

interface PlayerOperations {
    fun move(destination: Point, speed: Float): PlayerOperations
    fun pass(player: Player, force: Float): PlayerOperations
    fun shoot(destination: Point, force: Float): PlayerOperations
    fun turn(angle: Float): PlayerOperations
}
