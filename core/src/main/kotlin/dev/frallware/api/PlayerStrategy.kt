package dev.frallware.api

interface PlayerStrategy {
    val name: String

    fun step(state: GameState): PlayerMove
}

class PlayerMove {
    var moveDestination: Point? = null
        private set
    var moveSpeed: Float = 0f
        private set

    var passDestination: Point? = null
        private set
    var passForce: Float = 0f
        private set

    var shotDestination: Point? = null
        private set
    var shotForce: Float = 0f
        private set

    var rotation: Float = 0f
        private set

    fun move(destination: Point, speed: Float): PlayerMove {
        this.moveDestination = destination
        this.moveSpeed = speed
        return this
    }

    fun pass(player: Player, force: Float): PlayerMove {
        this.passDestination = player.position
        this.passForce = force
        return this
    }

    fun shoot(destination: Point, force: Float): PlayerMove {
        this.shotDestination = destination
        this.shotForce = force
        return this
    }

    fun turn(angle: Float): PlayerMove {
        this.rotation += angle
        return this
    }
}
