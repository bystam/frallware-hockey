package frallware.hockey.game

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.ChainShape
import com.badlogic.gdx.physics.box2d.World

class GdxRink(
    val world: World,
) {
    companion object {
        const val WIDTH = 100f
        const val HEIGHT = 50f
        const val FACEOFF_CIRCLE_RADIUS = 7f
        const val TEAM_ZONE_LINE_OFFSET = 20f
    }

    val body: Body = world.createBody(BodyDef().apply {
        type = BodyDef.BodyType.StaticBody
        position.set(WIDTH / 2, HEIGHT / 2)
    })

    object OutsideRink

    private val rect: RoundedRect = RoundedRect.create(WIDTH, HEIGHT, 3f, 20)
    private val outsideRinkRect: RoundedRect = RoundedRect.create(WIDTH + 1, HEIGHT + 1, 3f, 20)

    private val absoluteEdgePoints: List<Vector2> by lazy(LazyThreadSafetyMode.NONE) {
        val rinkCenter = body.worldCenter
        rect.allPoints.map { rinkCenter + it }
    }

    init {
        // Create a static body for the container at origin
        val shape = ChainShape()
        shape.createLoop(rect.allPoints.toTypedArray())

        body.createFixture(shape, 0f).apply {
            restitution = 0.2f // No bounce - absorbs impact
            friction = 0.6f
        }

        shape.dispose()

        // Create an "outside rink" body that we can use to detect if the puck was player outside the game
        val outsideShape = ChainShape()
        outsideShape.createLoop(outsideRinkRect.allPoints.toTypedArray())

        body.createFixture(outsideShape, 0f).apply {
            userData = OutsideRink
        }

        outsideShape.dispose()
    }

    fun render(shapeRenderer: ShapeRenderer) {
        val center = body.position

        shapeRenderer.drawFaceoffCircle(center)
        shapeRenderer.drawVerticalLine(Color.RED.withAlpha(0.5f), WIDTH / 2)

        shapeRenderer.drawVerticalLine(Color.BLUE.withAlpha(0.5f), WIDTH / 2 - 18f)
        shapeRenderer.drawVerticalLine(Color.BLUE.withAlpha(0.5f), WIDTH / 2 + 18f)

        // draw outer rink
        shapeRenderer.color = Color.DARK_GRAY
        for ((from, to) in absoluteEdgePoints.windowed(2)) {
            shapeRenderer.rectLine(from, to, 0.3f)
        }
        shapeRenderer.rectLine(absoluteEdgePoints.last(), absoluteEdgePoints.first(), 0.3f)
    }

    private fun ShapeRenderer.drawFaceoffCircle(point: Vector2) {
        color = Color.RED.withAlpha(0.2f)
        circle(point.x, point.y, FACEOFF_CIRCLE_RADIUS, 40)
        color = Color.WHITE
        circle(point.x, point.y, FACEOFF_CIRCLE_RADIUS - 0.2f, 40)
        color = Color.RED.withAlpha(0.8f)
        circle(point.x, point.y, 0.5f, 20)
    }

    private fun ShapeRenderer.drawVerticalLine(color: Color, x: Float) {
        this.color = color
        rectLine(x, 0f, x, HEIGHT, 0.4f)
    }
}
