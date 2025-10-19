package dev.frallware

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2


val Rectangle.bottomLeft: Vector2 get() = Vector2(x, y)
val Rectangle.topLeft: Vector2 get() = Vector2(x, y + height)
val Rectangle.topRight: Vector2 get() = Vector2(x + width, y + height)
val Rectangle.bottomRight: Vector2 get() = Vector2(x + width, y)
