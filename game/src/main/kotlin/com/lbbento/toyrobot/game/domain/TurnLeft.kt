package com.lbbento.toyrobot.game.domain

fun interface TurnLeft {
    fun turnLeft(position: GamePosition?, direction: GameDirection?): GameRobotPosition

    companion object {
        operator fun TurnLeft.invoke(position: GamePosition?, direction: GameDirection?) =
            turnLeft(position, direction)
    }

}
