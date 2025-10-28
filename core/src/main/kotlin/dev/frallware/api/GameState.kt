package dev.frallware.api

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

data class Point(
    val x: Float,
    val y: Float,
) {
    operator fun plus(vector: Vector): Point = Point(this.x + vector.dx, this.y + vector.dy)

    companion object {
        val zero: Point = Point(0f, 0f)
    }
}

data class Vector(
    val dx: Float,
    val dy: Float,
)
