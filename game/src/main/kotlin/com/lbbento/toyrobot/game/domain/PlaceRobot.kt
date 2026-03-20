package com.lbbento.toyrobot.game.domain

fun interface PlaceRobot {
    fun place(position: GamePosition, direction: GameDirection): GameRobotPosition

    companion object {
        operator fun PlaceRobot.invoke(position: GamePosition, direction: GameDirection) = place(position, direction)
    }
}
