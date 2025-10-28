package dev.frallware.game

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import dev.frallware.api.GameState
import dev.frallware.api.Player
import dev.frallware.api.Point
import dev.frallware.api.Puck
import dev.frallware.api.Vector

class StateImpl(
    val player: GdxPlayer,
    val thePuck: GdxPuck,
    friendlyGoalPosition: Vector2,
    friendlyPlayers: List<GdxPlayer>,
    friendlyGoalie: GdxPlayer,
    enemyGoalPosition: Vector2,
    enemyPlayers: List<GdxPlayer>,
    enemyGoalie: GdxPlayer,
) : GameState {
    override val puck: PuckImpl = PuckImpl()
    override val me: PlayerImpl = PlayerImpl(player)
    override val friendlyGoalPosition: Point = Point(friendlyGoalPosition.x, friendlyGoalPosition.y)
    override val friendlyGoalie: PlayerImpl = PlayerImpl(friendlyGoalie)
    override val friendlyPlayers: List<PlayerImpl> = friendlyPlayers.map { PlayerImpl(it) }
    override val enemyGoalPosition: Point = Point(enemyGoalPosition.x, enemyGoalPosition.y)
    override val enemyGoalie: PlayerImpl = PlayerImpl(enemyGoalie)
    override val enemyPlayers: List<PlayerImpl> = enemyPlayers.map { PlayerImpl(it) }

    private val allPlayers = this.friendlyPlayers + this.enemyPlayers

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

    class PlayerImpl(val player: GdxPlayer) : Player {
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
