package dev.frallware

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import dev.frallware.api.GameState
import dev.frallware.api.PlayerOperations
import dev.frallware.api.PlayerStrategy
import dev.frallware.api.Point
import dev.frallware.game.Side

class KeyboardPlayerStrategy(val side: Side) : PlayerStrategy {

    private val startingPoint: Vector2
    private val startingAngle: Float
    private val leftKey: Int
    private val rightKey: Int
    private val forwardKey: Int
    private val backwardsKey: Int
    private val shootKey: Int

    override val name: String = "KeyboardPlayerStrategy-$side"

    init {
        when (side) {
            Side.Left -> {
                startingPoint = Vector2(Constants.WORLD_WIDTH / 2 - 5f, Constants.WORLD_HEIGHT / 2)
                startingAngle = 0f
                leftKey = Input.Keys.A
                rightKey = Input.Keys.D
                forwardKey = Input.Keys.W
                backwardsKey = Input.Keys.S
                shootKey = Input.Keys.SHIFT_LEFT
            }

            Side.Right -> {
                startingPoint = Vector2(Constants.WORLD_WIDTH / 2 + 5f, Constants.WORLD_HEIGHT / 2)
                startingAngle = MathUtils.PI
                leftKey = Input.Keys.LEFT
                rightKey = Input.Keys.RIGHT
                forwardKey = Input.Keys.UP
                backwardsKey = Input.Keys.DOWN
                shootKey = Input.Keys.SPACE
            }
        }
    }

    override fun step(state: GameState, operations: PlayerOperations) {
        val forward = Point.zero + state.me.heading

        if (Gdx.input.isKeyPressed(leftKey)) {
            operations.turn(0.05f)
        }
        if (Gdx.input.isKeyPressed(rightKey)) {
            operations.turn(-0.05f)
        }

        if (Gdx.input.isKeyPressed(forwardKey)) {
            operations.move(forward, 40f)
        } else if (Gdx.input.isKeyPressed(backwardsKey)) {
            operations.move(forward, -40f)
        }
        if (Gdx.input.isKeyPressed(shootKey) && state.me.hasPuck) {
            operations.shoot(forward, 20f)
        }
    }
}
