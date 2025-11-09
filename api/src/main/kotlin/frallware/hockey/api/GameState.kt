package frallware.hockey.api

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

    val hasPuck: Boolean
}

interface Puck {
    val holder: Player?
    val position: Point
}
