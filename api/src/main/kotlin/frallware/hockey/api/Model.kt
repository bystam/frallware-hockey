package frallware.hockey.api

import kotlin.math.hypot

data class Color(
    val r: Float,
    val g: Float,
    val b: Float
)

data class Point(
    val x: Float,
    val y: Float,
) {
    operator fun plus(vector: Vector): Point = Point(this.x + vector.dx, this.y + vector.dy)

    fun offset(dx: Float = 0f, dy: Float = 0f): Point = Point(this.x + dx, this.y + dy)

    fun distanceTo(other: Point): Float = hypot(this.x - other.x, this.y - other.y)

    companion object {
        val zero: Point = Point(0f, 0f)
    }
}

data class Vector(
    val dx: Float,
    val dy: Float,
)
