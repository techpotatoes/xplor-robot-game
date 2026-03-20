package com.lbbento.toyrobot.game.ui

import com.lbbento.toyrobot.game.domain.GameDirection
import com.lbbento.toyrobot.game.domain.GamePosition

data class GameState(
    val robotPosition: GamePosition?,
    val robotDirection: GameDirection?,
    val isPlacementMode: Boolean = false,
    val error: GameError? = null,
) {
    val isRobotPlaced: Boolean get() = robotPosition != null

    companion object {
        fun empty() = GameState(robotPosition = null, robotDirection = null)
    }
}
