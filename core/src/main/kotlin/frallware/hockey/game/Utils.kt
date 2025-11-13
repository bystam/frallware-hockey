package frallware.hockey.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.EarClippingTriangulator
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import kotlin.math.min
import kotlin.random.Random

fun Color.withAlpha(alpha: Float): Color = cpy().set(r, g, b, alpha)
fun frallware.hockey.api.Color.toGdx(): Color = Color(r, g, b, 1f)

operator fun Vector2.plus(v: Vector2): Vector2 = cpy().add(v)

fun Random.Default.boolean(chance: Double): Boolean {
    return nextDouble(0.0, 1.0) < chance
}

enum class Side {
    Left, Right;

    val opponent: Side
        get() = when (this) {
            Left -> Right
            Right -> Left
        }
}

class RoundedRect(
    val width: Float,
    val height: Float,
    val topRightPoints: List<Vector2>,
    val topLeftPoints: List<Vector2>,
    val bottomLeftPoints: List<Vector2>,
    val bottomRightPoints: List<Vector2>,
) {

    val allPoints = topRightPoints + topLeftPoints + bottomLeftPoints + bottomRightPoints

    fun polygonTriangles(): List<Triple<Vector2, Vector2, Vector2>> {
        val vertices = allPoints.flatMap { listOf(it.x, it.y) }.toFloatArray()
        val indices = EarClippingTriangulator().computeTriangles(vertices)

        val result = mutableListOf<Triple<Vector2, Vector2, Vector2>>()
        for (i in (0..<indices.size) step 3) {
            val idx1 = indices.get(i) * 2
            val idx2 = indices.get(i + 1) * 2
            val idx3 = indices.get(i + 2) * 2

            result += Triple(
                Vector2(vertices[idx1], vertices[idx1 + 1]),
                Vector2(vertices[idx2], vertices[idx2 + 1]),
                Vector2(vertices[idx3], vertices[idx3 + 1]),
            )
        }
        return result
    }

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
                width = width,
                height = height,
                topLeftPoints = topLeftPoints,
                topRightPoints = topRightPoints,
                bottomRightPoints = bottomRightPoints,
                bottomLeftPoints = bottomLeftPoints,
            )
        }
    }
}

class TimedInterpolation(
    val fromValue: Float,
    val toValue: Float,
    val duration: Float,
    val interpolation: Interpolation,
) {
    private var elapsed: Float = 0f

    fun next(): Float {
        elapsed = (elapsed + Gdx.graphics.deltaTime).coerceAtMost(duration)
        val t = elapsed / duration

        // Smooth ease-in/ease-out
        val easedT = interpolation.apply(t)
        return MathUtils.lerp(fromValue, toValue, easedT)
    }

    fun reset() {
        elapsed = 0f
    }
}
