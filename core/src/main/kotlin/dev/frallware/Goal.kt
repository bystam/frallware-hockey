package dev.frallware

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.World

class Goal(world: World, val side: Side) {

    val start = Vector2(-0.6f, 1.0f)
    val topArcStart = Vector2(0.4f, 1.0f)
    val backStart = Vector2(0.6f, 0.8f)
    val bottomArcStart = Vector2(0.6f, -0.8f)
    val bottomStart = Vector2(0.4f, -1.0f)
    val end = Vector2(-0.6f, -1.0f)

    // written as the "right" goal
    private val joints: Array<Vector2> = arrayOf(start, topArcStart, backStart, bottomArcStart, bottomStart, end)

    val body: Body

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
        body.userData = this

        for (j in joints) {
            j.scl(3f)
        }

        if (side == Side.Left) {
            for (joint in joints) {
                joint.rotateRad(MathUtils.PI)
            }
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

        val shape = ChainShape()
        shape.createChain(chain.toTypedArray())

        body.createFixture(shape, 0f).apply {
            restitution = 0.0f
            friction = 0.5f
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
