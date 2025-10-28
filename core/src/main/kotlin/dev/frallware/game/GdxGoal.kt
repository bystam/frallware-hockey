package dev.frallware.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import dev.frallware.Constants

class GdxGoal(world: World, val side: Side) {

    val start = Vector2(-0.6f, 1.0f)
    val topArcStart = Vector2(0.4f, 1.0f)
    val backStart = Vector2(0.6f, 0.8f)
    val bottomArcStart = Vector2(0.6f, -0.8f)
    val bottomStart = Vector2(0.4f, -1.0f)
    val end = Vector2(-0.6f, -1.0f)

    // written as the "right" goal
    private val joints: Array<Vector2> = arrayOf(start, topArcStart, backStart, bottomArcStart, bottomStart, end)

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

        for (j in joints) {
            j.scl(3f)
        }

        // mirror
        if (side == Side.Left) {
            for (joint in joints) {
                joint.x = -joint.x
            }
        }

        // goals sensor
        val floor = PolygonShape()
        floor.set(
            // scale down to make it not collide from outside the goal
            arrayOf(
                start.cpy().scl(0.95f),
                topArcStart.cpy().scl(0.95f),
                bottomStart.cpy().scl(0.95f),
                end.cpy().scl(0.95f)
            )
        )
        body.createFixture(floor, 0f).apply {
            isSensor = true
            userData = Sensor(side)
        }

        /*
        A chain that looks like this
        ---\
            |
        ---/
         */
        val chain = mutableListOf(start)
        chain += interpolateArc90Deg(topArcStart, backStart, 10)
        chain += interpolateArc90Deg(bottomArcStart, bottomStart, 10)
        chain += end

        val cage = ChainShape()
        cage.createChain(chain.toTypedArray())

        // cage
        body.createFixture(cage, 0f).apply {
            restitution = 0.0f
            friction = 0.5f
            userData = this@GdxGoal
        }
    }

    fun render(shapeRenderer: ShapeRenderer) {
        val center = body.worldCenter

        shapeRenderer.color = Color.RED
        for (i in (0..<joints.lastIndex)) {
            val from = center + joints[i]
            val to = center + joints[i + 1]
            shapeRenderer.rectLine(from, to, 0.2f)
        }
    }
}
