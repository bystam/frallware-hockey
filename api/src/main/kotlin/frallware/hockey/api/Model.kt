package frallware.hockey.api

import kotlin.math.hypot
import kotlin.math.sqrt

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
    operator fun minus(vector: Vector): Point = Point(this.x - vector.dx, this.y - vector.dy)
    operator fun minus(point: Point): Vector = Vector(this.x - point.x, this.y - point.y)

    fun offset(dx: Float = 0f, dy: Float = 0f): Point = Point(this.x + dx, this.y + dy)

    fun distanceTo(other: Point): Float = hypot(this.x - other.x, this.y - other.y)

    companion object {
        val zero: Point = Point(0f, 0f)
    }
}

data class Vector(
    val dx: Float = 0f,
    val dy: Float = 0f,
) {
    val length: Float get() = sqrt(dx * dx + dy * dy)

    operator fun times(factor: Float): Vector = Vector(dx * factor, dy * factor)
    operator fun plus(vector: Vector): Vector = Vector(this.dx + vector.dx, this.dy + vector.dy)
}
