package dev.frallware.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.min

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

class RoundedRect(
    val topRightPoints: List<Vector2>,
    val topLeftPoints: List<Vector2>,
    val bottomLeftPoints: List<Vector2>,
    val bottomRightPoints: List<Vector2>,
) {

    val allPoints = topRightPoints + topLeftPoints + bottomLeftPoints + bottomRightPoints

    companion object {
        fun create(width: Float, height: Float, radius: Float, segmentsPerCorner: Int): RoundedRect {

            val hw = width / 2f
            val hh = height / 2f
            val r = min(radius, min(hw, hh)) // clamp radius so corners don't overlap

            fun createArc(center: Vector2, startAngle: Float) = buildList {
                for (j in 0..segmentsPerCorner) {
                    val angle = startAngle + (j / segmentsPerCorner.toFloat()) * (MathUtils.PI / 2f)
                    val x = center.x + MathUtils.cos(angle) * r
                    val y = center.y + MathUtils.sin(angle) * r
                    add(Vector2(x, y))
                }
            }

            val topRightPoints = createArc(
                center = Vector2(hw - r, hh - r),  // top-right
                startAngle = 0f,
            )

            val topLeftPoints = createArc(
                center = Vector2(-hw + r, hh - r),  // top-left
                startAngle = MathUtils.PI / 2f,
            )

            val bottomLeftPoints = createArc(
                center = Vector2(-hw + r, -hh + r),  // bottom-left
                startAngle = MathUtils.PI,
            )

            val bottomRightPoints = createArc(
                center = Vector2(hw - r, -hh + r), // bottom-right
                startAngle = 3f * MathUtils.PI / 2f,
            )

            return RoundedRect(
                topLeftPoints = topLeftPoints,
                topRightPoints = topRightPoints,
                bottomRightPoints = bottomRightPoints,
                bottomLeftPoints = bottomLeftPoints,
            )
        }
    }
}
