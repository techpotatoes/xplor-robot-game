package com.lbbento.toyrobot.game.domain

sealed class GameException : Exception()
class RobotNotPlacedException : GameException()
class OutOfBoundsException : GameException()
class MovementWouldFallException : GameException()
