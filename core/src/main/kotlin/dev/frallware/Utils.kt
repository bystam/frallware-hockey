package dev.frallware

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2


fun Color.withAlpha(alpha: Float): Color = cpy().set(r, g, b, alpha)

operator fun Vector2.plus(v: Vector2): Vector2 = cpy().add(v)

enum class Side {
    Left, Right;

    val opponent: Side
        get() = when (this) {
            Left -> Right
            Right -> Left
        }
}

fun interpolateArc90Deg(p1: Vector2, p2: Vector2, segments: Int): Array<Vector2> {
    val result = Array<Vector2>(segments + 1) { Vector2.Zero }

    val center = Vector2(p1.x, p2.y)

    val radius = center.dst(p1)
    val startAngle = MathUtils.atan2(p1.y - center.y, p1.x - center.x)
    val endAngle = MathUtils.atan2(p2.y - center.y, p2.x - center.x)

    for (i in 0..segments) {
        val angle = MathUtils.lerp(startAngle, endAngle, i.toFloat() / segments)
        result[i] = Vector2(
            center.x + MathUtils.cos(angle) * radius,
            center.y + MathUtils.sin(angle) * radius
        )
    }
    return result
}
