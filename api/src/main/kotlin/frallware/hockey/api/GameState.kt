package frallware.hockey.api

/**
 * TODO:
 * - starting positions
 * - speed constants
 * - make center of rink origin
 */
interface GameState {
    val puck: Puck

    val me: Player

    val friendlyGoalPosition: Point
    val friendlyGoalie: Player
    val friendlyPlayers: List<Player>

    val enemyGoalPosition: Point
    val enemyGoalie: Player
    val enemyPlayers: List<Player>
}

interface Player {
    val position: Point
    val heading: Vector
    val velocity: Vector
    val isFacingOff: Boolean

    val hasPuck: Boolean
}

interface Puck {
    val holder: Player?
    val position: Point
    val velocity: Vector
}
