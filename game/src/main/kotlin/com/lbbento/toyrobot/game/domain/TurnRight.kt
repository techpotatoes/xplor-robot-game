package com.lbbento.toyrobot.game.domain

fun interface TurnRight {
    fun turnRight(position: GamePosition?, direction: GameDirection?): GameRobotPosition

    companion object {
        operator fun TurnRight.invoke(position: GamePosition?, direction: GameDirection?) =
            turnRight(position, direction)
    }

}
