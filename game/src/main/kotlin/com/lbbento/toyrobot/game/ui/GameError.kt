package com.lbbento.toyrobot.game.ui

sealed class GameError {
    data object RobotNotPlaced : GameError()
    data object OutOfBounds : GameError()
    data object WouldFall : GameError()
}
