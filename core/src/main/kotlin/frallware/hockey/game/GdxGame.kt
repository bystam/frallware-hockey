package frallware.hockey.game

import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Contact
import com.badlogic.gdx.physics.box2d.ContactImpulse
import com.badlogic.gdx.physics.box2d.ContactListener
import com.badlogic.gdx.physics.box2d.Manifold
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.ScreenUtils
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.ScreenViewport
import frallware.hockey.api.HockeyTeam
import frallware.hockey.api.PlayerStrategy
import java.time.Duration
import java.time.Instant

class GdxGame(
    private val leftTeam: HockeyTeam,
    private val rightTeam: HockeyTeam,
    private val onComplete: (winner: HockeyTeam?) -> Unit,
) : ScreenAdapter() {

    companion object {
        val startingPointOffsets = listOf(
            Vector2(-3f, 0f), // faceoff
            Vector2(-6f, 3f),
            Vector2(-12f, -4f),
            Vector2(-12f, 4f),
        )
        val leftStartingPoints = startingPointOffsets.map {
            Vector2(GdxRink.WIDTH / 2, GdxRink.HEIGHT / 2) + it
        }
        val rightStartingPoints = startingPointOffsets.map {
            Vector2(GdxRink.WIDTH / 2, GdxRink.HEIGHT / 2) + it.cpy().rotateDeg(180f)
        }

        val matchLength: Duration = Duration.ofMinutes(1)

        const val FPS = 1f / 60
    }

    private val scores = mutableMapOf(Side.Left to 0, Side.Right to 0)

    val worldViewport: FitViewport = FitViewport(GdxRink.WIDTH, GdxRink.HEIGHT)
    val hudViewport: ScreenViewport = ScreenViewport()
    private val world: World = World(Vector2.Zero, true)

    private val hockeyRink: GdxRink = GdxRink(world)
    private val scoreBoard: GdxScoreBoard = GdxScoreBoard(hudViewport, scores) { timeLeft }
    private val puck: GdxPuck = GdxPuck(world)
    private val leftGoal = GdxGoal(world, Side.Left)
    private val rightGoal = GdxGoal(world, Side.Right)

    private val shapeRenderer: ShapeRenderer = ShapeRenderer()

    private var fpsAcc: Float = 0f

    private var timeLeft: Duration = matchLength
    private var goalResetAt: Instant? = null
    private var roundStart: Instant = Instant.now() + Duration.ofSeconds(2)
    private var isFaceoff = true
    private var hasEnded: Boolean = false

    private val zoomAnimation: TimedInterpolation = TimedInterpolation(
        fromValue = 0.4f,
        toValue = 0.7f,
        duration = 2f,
        interpolation = Interpolation.sine,
    )

    private val leftGoalie: GdxPlayer = createPlayer(
        leftTeam.goalie, leftGoal.body.position + Vector2(2f, 0f), Side.Left,
    )
    private val leftPlayers: List<GdxPlayer> = leftTeam.skaters.take(4).mapIndexed { index, strategy ->
        createPlayer(strategy, leftStartingPoints[index], Side.Left)
    }

    private val rightGoalie: GdxPlayer = createPlayer(
        rightTeam.goalie, rightGoal.body.position + Vector2(-2f, 0f), Side.Right,
    )
    private val rightPlayers: List<GdxPlayer> = rightTeam.skaters.take(4).mapIndexed { index, strategy ->
        createPlayer(strategy, rightStartingPoints[index], Side.Right)
    }

    private val allPlayers = leftPlayers + rightPlayers + leftGoalie + rightGoalie

    init {
        world.setContactListener(HockeyContactListener())
    }

    override fun resize(width: Int, height: Int) {
        worldViewport.update(width, height, true)
        hudViewport.update(width, height, true)
    }

    override fun render(delta: Float) {
        if (hasEnded) return
        if (timeLeft < Duration.ZERO) {
            hasEnded = true
            val leftPoints = scores[Side.Left]!!
            val rightPoints = scores[Side.Right]!!
            onComplete(
                when {
                    leftPoints > rightPoints -> leftTeam
                    leftPoints < rightPoints -> rightTeam
                    else -> null
                }
            )
            return
        }

        if (goalResetAt != null && goalResetAt!! < Instant.now()) {
            for (player in allPlayers) {
                player.reset()
            }
            puck.reset()
            goalResetAt = null
            zoomAnimation.reset()
            roundStart = Instant.now() + Duration.ofSeconds(2)
            isFaceoff = true
        }

        if (Instant.now() > roundStart) {
            // Actually run game
            for (player in allPlayers) {
                player.update()
            }
            if (goalResetAt == null) {
                timeLeft -= Duration.ofMillis((1000 * delta).toLong())
            }
        }

        fpsAcc += delta
        while (fpsAcc > FPS) {
            world.step(FPS, 6, 2)
            fpsAcc -= FPS
        }

        val puckPos = puck.body.worldCenter
        val camera = worldViewport.camera as OrthographicCamera
        camera.position.set(puckPos.x, puckPos.y, 0f)
        camera.zoom = zoomAnimation.next()

        ScreenUtils.clear(Color.WHITE)
        worldViewport.apply()

        shapeRenderer.projectionMatrix = worldViewport.camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        hockeyRink.render(shapeRenderer)
        puck.render(shapeRenderer)
        leftGoal.render(shapeRenderer)
        rightGoal.render(shapeRenderer)
        for (player in allPlayers) {
            player.render(shapeRenderer)
        }

        shapeRenderer.end()
        scoreBoard.render()
    }

    override fun dispose() {
        world.dispose()
        shapeRenderer.dispose()
        scoreBoard.dispose()
    }

    inner class HockeyContactListener : ContactListener {
        override fun beginContact(contact: Contact) {
            val aData = contact.fixtureA.userData
            val bData = contact.fixtureB.userData
            val puck = (aData as? GdxPuck) ?: (bData as? GdxPuck)
            val player = (aData as? GdxPlayer) ?: (bData as? GdxPlayer)
            val goal = (aData as? GdxGoal) ?: (bData as? GdxGoal)
            val goalSensor = (aData as? GdxGoal.Sensor) ?: (bData as? GdxGoal.Sensor)
            val outsideRink = (aData as? GdxRink.OutsideRink) ?: (bData as? GdxRink.OutsideRink)

            if (puck != null && player != null) {
                player.tryTakePuck(puck)
                isFaceoff = false
            }
            if (puck != null && goal != null && goalResetAt != null) {
                puck.slowDown()
            }
            if (puck != null && goalSensor != null && goalResetAt == null) {
                scores[goalSensor.side.opponent] = scores[goalSensor.side.opponent]!! + 1
                goalResetAt = Instant.now().plusSeconds(3)
            }
            if (puck != null && outsideRink != null) {
                goalResetAt = Instant.now().plusSeconds(3)
            }
            if (aData is GdxPlayer && bData is GdxPlayer) {
                val speed = aData.body.linearVelocity.cpy().sub(bData.body.linearVelocity)
                if (speed.len() > 5f) {
                    aData.dropPuck()
                    bData.dropPuck()
                }
            }
        }

        override fun endContact(contact: Contact) {
        }

        override fun preSolve(
            contact: Contact,
            oldManifold: Manifold
        ) {
        }

        override fun postSolve(
            contact: Contact,
            impulse: ContactImpulse
        ) {
        }
    }

    private fun createPlayer(
        strategy: PlayerStrategy,
        startingPoint: Vector2,
        side: Side,
    ): GdxPlayer = GdxPlayer(
        world = world,
        strategy = strategy,
        color = when (side) {
            Side.Left -> leftTeam.color
            Side.Right -> rightTeam.color
        }.toGdx(),
        startingPoint = startingPoint,
        startingAngle = when (side) {
            Side.Left -> 0f
            Side.Right -> MathUtils.PI
        },
        stateMaker = {
            createState(it, side)
        }
    )

    private fun createState(player: GdxPlayer, side: Side): StateImpl {
        val (friendlyPlayers, enemyPlayers) = when (side) {
            Side.Left -> Pair(leftPlayers, rightPlayers)
            Side.Right -> Pair(rightPlayers, leftPlayers)
        }
        val (friendlyGoalie, enemyGoalie) = when (side) {
            Side.Left -> Pair(leftGoalie, rightGoalie)
            Side.Right -> Pair(rightGoalie, leftGoalie)
        }
        val (friendlyGoal, enemyGoal) = when (side) {
            Side.Left -> Pair(leftGoal.body.position, rightGoal.body.position)
            Side.Right -> Pair(rightGoal.body.position, leftGoal.body.position)
        }
        return StateImpl(
            player = player,
            thePuck = puck,
            // It's always the first player that's taking the faceoff
            isFacingOff = { isFaceoff && (it == friendlyPlayers.first() || it == enemyPlayers.first()) },
            friendlyGoalPosition = friendlyGoal,
            friendlyPlayers = friendlyPlayers,
            friendlyGoalie = friendlyGoalie,
            enemyGoalPosition = enemyGoal,
            enemyPlayers = enemyPlayers,
            enemyGoalie = enemyGoalie,
        )
    }
}
