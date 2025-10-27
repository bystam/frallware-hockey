package dev.frallware

import com.badlogic.gdx.math.MathUtils
import dev.frallware.api.GameState
import dev.frallware.api.Player
import dev.frallware.api.Point
import dev.frallware.api.Puck
import dev.frallware.api.Vector
import java.util.UUID

class StateImpl(
    val player: HockeyPlayer,
    val thePuck: dev.frallware.Puck,
    val hockeyRink: HockeyRink,
) : GameState {
    override val puck: PuckImpl = PuckImpl()
    override val me: PlayerImpl = PlayerImpl(player)
    override val friendlyGoalie: PlayerImpl
        get() = TODO("Not yet implemented")
    override val friendlyPlayers: List<PlayerImpl> = listOf(me)
    override val enemyGoalie: PlayerImpl
        get() = TODO("Not yet implemented")
    override val enemyPlayers: List<PlayerImpl> = listOf(hockeyRink.leftPlayer, hockeyRink.rightPlayer)
        .filter { it !== player }
        .map { PlayerImpl(it) }

    private val allPlayers = friendlyPlayers + enemyPlayers

    inner class PuckImpl : Puck {
        override val holder: Player?
            get() {
                return allPlayers.find { it.player === thePuck.holder }
            }
        override val position: Point
            get() {
                val pos = thePuck.body.worldCenter
                return Point(pos.x, pos.y)
            }

    }

    class PlayerImpl(val player: HockeyPlayer) : Player {
        override val id: String = UUID.randomUUID().toString()
        override val position: Point
            get() {
                val pos = player.body.worldCenter
                return Point(pos.x, pos.y)
            }
        override val heading: Vector
            get() {
                val angle = player.body.angle
                return Vector(MathUtils.cos(angle), MathUtils.sin(angle))
            }
        override val hasPuck: Boolean get() = player.puck != null
    }
}
