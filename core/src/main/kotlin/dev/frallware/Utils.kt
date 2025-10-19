package dev.frallware

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

fun Color.withAlpha(alpha: Float): Color = cpy().set(r, g, b, alpha)

operator fun Vector2.plus(v: Vector2): Vector2 = cpy().add(v)

fun ShapeRenderer.batch(type: ShapeRenderer.ShapeType, block: ShapeRenderer.() -> Unit) {
    begin(type)
    block()
    end()
}
