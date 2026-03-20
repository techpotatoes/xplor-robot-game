package com.lbbento.toyrobot.game.domain

fun interface MoveRobot {
    fun move(position: GamePosition?, direction: GameDirection?): GameRobotPosition

    companion object {
        operator fun MoveRobot.invoke(position: GamePosition?, direction: GameDirection?) =
            move(position, direction)
    }

}
