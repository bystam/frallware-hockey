package dev.frallware.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.World
import dev.frallware.Constants

class GdxGoal(world: World, val side: Side) {

    val cageRect = RoundedRect.create(4f, 5f, 1.2f, 10)

    val cagePoints: List<Vector2> = when (side) {
        Side.Left -> buildList {
            add(cageRect.topRightPoints.last())
            addAll(cageRect.topLeftPoints)
            addAll(cageRect.bottomLeftPoints)
            add(cageRect.bottomRightPoints.first())
        }

        Side.Right -> buildList {
            add(cageRect.bottomLeftPoints.last())
            addAll(cageRect.bottomRightPoints)
            addAll(cageRect.topRightPoints)
            add(cageRect.topLeftPoints.first())
        }
    }

    val body: Body

    data class Sensor(val side: Side)

    init {
        val bodyDef = BodyDef().apply {
            type = BodyDef.BodyType.StaticBody
            val yPos = Constants.WORLD_HEIGHT / 2
            when (side) {
                Side.Left -> position.set(15f, yPos)
                Side.Right -> position.set(Constants.WORLD_WIDTH - 15f, yPos)
            }
        }

        body = world.createBody(bodyDef)

        // goals sensor
        val floor = ChainShape()
        val floorPoints = cagePoints
            .map { it.cpy().scl(0.95f) }
        floor.createLoop(floorPoints.toTypedArray())
        body.createFixture(floor, 0f).apply {
            isSensor = true
            userData = Sensor(side)
        }

        floor.dispose()

        val cage = ChainShape()
        cage.createChain(cagePoints.toTypedArray())

        // cage
        body.createFixture(cage, 0f).apply {
            restitution = 0.0f
            friction = 0.5f
            userData = this@GdxGoal
        }

        cage.dispose()
    }

    fun render(shapeRenderer: ShapeRenderer) {
        val center = body.worldCenter

        shapeRenderer.color = Color.RED
        for ((from, to) in cagePoints.windowed(2)) {
            shapeRenderer.rectLine(center + from, center + to, 0.2f)
        }
    }
}
