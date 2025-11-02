package dev.frallware.game

import com.badlogic.gdx.Gdx
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
import dev.frallware.Constants
import dev.frallware.api.HockeyTeam
import java.time.Duration
import java.time.Instant

class GdxGame(
    private val world: World,
    private val viewport: FitViewport,
    private val leftTeam: HockeyTeam,
    private val rightTeam: HockeyTeam,
) {

    companion object {
        val leftStartingPoints = listOf(
            Vector2(Constants.WORLD_WIDTH / 2 - 5f, Constants.WORLD_HEIGHT / 2 - 3f),
            Vector2(Constants.WORLD_WIDTH / 2 - 5f, Constants.WORLD_HEIGHT / 2 + 3f),
            Vector2(Constants.WORLD_WIDTH / 2 - 12f, Constants.WORLD_HEIGHT / 2 - 5f),
            Vector2(Constants.WORLD_WIDTH / 2 - 12f, Constants.WORLD_HEIGHT / 2 + 5f),
        )
        val rightStartingPoints = listOf(
            Vector2(Constants.WORLD_WIDTH / 2 + 5f, Constants.WORLD_HEIGHT / 2 - 3f),
            Vector2(Constants.WORLD_WIDTH / 2 + 5f, Constants.WORLD_HEIGHT / 2 + 3f),
            Vector2(Constants.WORLD_WIDTH / 2 + 12f, Constants.WORLD_HEIGHT / 2 - 5f),
            Vector2(Constants.WORLD_WIDTH / 2 + 12f, Constants.WORLD_HEIGHT / 2 + 5f),
        )

        const val FPS = 1f / 60
    }

    private val scores = mutableMapOf(Side.Left to 0, Side.Right to 0)

    private val hockeyRink: GdxRink = GdxRink(world)
    private val scoreBoard: GdxScoreBoard = GdxScoreBoard(viewport, scores)
    private val puck: GdxPuck = GdxPuck(world)
    private val leftGoal = GdxGoal(world, Side.Left)
    private val rightGoal = GdxGoal(world, Side.Right)

    private val shapeRenderer: ShapeRenderer = ShapeRenderer()

    private var fpsAcc: Float = 0f

    private var goalResetAt: Instant? = null
    private var gameStartAt: Instant = Instant.now() + Duration.ofSeconds(2)
    private val zoomAnimation: TimedInterpolation = TimedInterpolation(
        fromValue = 0.4f,
        toValue = 0.7f,
        duration = 2f,
        interpolation = Interpolation.sine,
    )

    private val leftPlayers: List<GdxPlayer> = leftTeam.players.mapIndexed { index, strategy ->
        GdxPlayer(
            world = world,
            isGoalie = false,
            strategy = strategy,
            color = leftTeam.color,
            startingPoint = leftStartingPoints[index],
            startingAngle = 0f,
            stateMaker = {
                StateImpl(
                    player = it,
                    thePuck = puck,
                    friendlyGoalPosition = leftGoal.body.worldCenter,
                    friendlyPlayers = leftPlayers,
                    friendlyGoalie = leftPlayers.first(),
                    enemyGoalPosition = rightGoal.body.worldCenter,
                    enemyPlayers = rightPlayers,
                    enemyGoalie = rightPlayers.first()
                )
            }
        )
    }

    private val rightPlayers: List<GdxPlayer> = rightTeam.players.mapIndexed { index, strategy ->
        GdxPlayer(
            world = world,
            isGoalie = false,
            strategy = strategy,
            color = rightTeam.color,
            startingPoint = rightStartingPoints[index],
            startingAngle = MathUtils.PI,
            stateMaker = {
                StateImpl(
                    player = it,
                    thePuck = puck,
                    friendlyGoalPosition = rightGoal.body.worldCenter,
                    friendlyPlayers = rightPlayers,
                    friendlyGoalie = rightPlayers.first(),
                    enemyGoalPosition = leftGoal.body.worldCenter,
                    enemyPlayers = leftPlayers,
                    enemyGoalie = leftPlayers.first()
                )
            }
        )
    }

    init {
        world.setContactListener(HockeyContactListener())
    }

    fun reset() {
        for (player in leftPlayers) {
            player.reset()
        }
        for (player in rightPlayers) {
            player.reset()
        }
        puck.reset()
    }

    fun render() {
        if (goalResetAt != null && goalResetAt!! < Instant.now()) {
            reset()
            goalResetAt = null
            zoomAnimation.reset()
            gameStartAt = Instant.now() + Duration.ofSeconds(2)
        }

        if (gameStartAt < Instant.now()) {
            for (player in leftPlayers) {
                player.update()
            }
            for (player in rightPlayers) {
                player.update()
            }
        }

        fpsAcc += Gdx.graphics.deltaTime
        while (fpsAcc > FPS) {
            world.step(FPS, 6, 2)
            fpsAcc -= FPS
        }

        val puckPos = puck.body.worldCenter
        val camera = viewport.camera as OrthographicCamera
        camera.position.set(puckPos.x, puckPos.y, 0f)
        camera.zoom = zoomAnimation.next()

        ScreenUtils.clear(Color.WHITE)
        viewport.apply()

        shapeRenderer.projectionMatrix = viewport.camera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        hockeyRink.render(shapeRenderer)
        puck.render(shapeRenderer)
        leftGoal.render(shapeRenderer)
        rightGoal.render(shapeRenderer)
        for (player in leftPlayers) {
            player.render(shapeRenderer)
        }
        for (player in rightPlayers) {
            player.render(shapeRenderer)
        }

        shapeRenderer.end()
        scoreBoard.render()
    }

    fun dispose() {
        world.dispose()
        shapeRenderer.dispose()
    }

    inner class HockeyContactListener : ContactListener {
        override fun beginContact(contact: Contact) {
            val aData = contact.fixtureA.userData
            val bData = contact.fixtureB.userData
            val puck = (aData as? GdxPuck) ?: (bData as? GdxPuck)
            val player = (aData as? GdxPlayer) ?: (bData as? GdxPlayer)
            val goal = (aData as? GdxGoal) ?: (bData as? GdxGoal)
            val goalSensor = (aData as? GdxGoal.Sensor) ?: (bData as? GdxGoal.Sensor)

            puck?.registerContact()

            if (puck != null && player != null) {
                player.takePuck(puck)
            }
            if (puck != null && goal != null) {
                puck.slowDown()
            }
            if (puck != null && goalSensor != null && goalResetAt == null) {
                scores[goalSensor.side.opponent] = scores[goalSensor.side.opponent]!! + 1
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
            val aData = contact.fixtureA.userData
            val bData = contact.fixtureB.userData
            val puck = (aData as? GdxPuck) ?: (bData as? GdxPuck)

            puck?.deregisterContact()
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
}
