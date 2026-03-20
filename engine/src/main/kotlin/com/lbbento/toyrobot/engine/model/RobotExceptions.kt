package com.lbbento.toyrobot.engine.model

data class RobotCommandException(
    val errorType: RobotErrorType,
) : RuntimeException(errorType.toString())

enum class RobotErrorType {
    ROBOT_NOT_PLACED,
    POSITION_OUT_OF_BOUNDS,
    MOVEMENT_WOULD_FALL,
}
