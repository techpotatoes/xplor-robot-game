package com.lbbento.toyrobot.game.ui

import androidx.lifecycle.ViewModel
import com.lbbento.toyrobot.game.domain.GameDirection
import com.lbbento.toyrobot.game.domain.GameException
import com.lbbento.toyrobot.game.domain.GamePosition
import com.lbbento.toyrobot.game.domain.GameRobotPosition
import com.lbbento.toyrobot.game.domain.GetGameConfig
import com.lbbento.toyrobot.game.domain.MoveRobot
import com.lbbento.toyrobot.game.domain.MoveRobot.Companion.invoke
import com.lbbento.toyrobot.game.domain.MovementWouldFallException
import com.lbbento.toyrobot.game.domain.OutOfBoundsException
import com.lbbento.toyrobot.game.domain.PlaceRobot
import com.lbbento.toyrobot.game.domain.PlaceRobot.Companion.invoke
import com.lbbento.toyrobot.game.domain.RobotNotPlacedException
import com.lbbento.toyrobot.game.domain.TurnLeft
import com.lbbento.toyrobot.game.domain.TurnLeft.Companion.invoke
import com.lbbento.toyrobot.game.domain.TurnRight
import com.lbbento.toyrobot.game.domain.TurnRight.Companion.invoke
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    private val placeRobot: PlaceRobot,
    private val moveRobot: MoveRobot,
    private val turnLeft: TurnLeft,
    private val turnRight: TurnRight,
    getGameConfig: GetGameConfig,
) : ViewModel() {

    val gridSize: Int = getGameConfig.getTableSize()

    private val _state = MutableStateFlow(GameState.empty())
    val state: StateFlow<GameState> = _state.asStateFlow()

    private fun executeWithErrorHandling(command: () -> GameRobotPosition) {
        try {
            val newRobotPosition = command()
            _state.update {
                it.copy(
                    robotPosition = newRobotPosition.position,
                    robotDirection = newRobotPosition.direction,
                    isPlacementMode = false,
                    error = null,
                )
            }
        } catch (e: GameException) {
            val gameError = when (e) {
                is RobotNotPlacedException -> GameError.RobotNotPlaced
                is OutOfBoundsException -> GameError.OutOfBounds
                is MovementWouldFallException -> GameError.WouldFall
            }
            _state.update { it.copy(error = gameError, isPlacementMode = false) }
        }
    }

    fun onPlace(x: Int, y: Int, direction: GameDirection) {
        executeWithErrorHandling { placeRobot(GamePosition(x, y), direction) }
    }

    fun onMove() {
        executeWithErrorHandling {
            moveRobot(
                _state.value.robotPosition,
                _state.value.robotDirection
            )
        }
    }

    fun onTurnLeft() {
        executeWithErrorHandling {
            turnLeft(
                _state.value.robotPosition,
                _state.value.robotDirection
            )
        }
    }

    fun onTurnRight() {
        executeWithErrorHandling {
            turnRight(
                _state.value.robotPosition,
                _state.value.robotDirection
            )
        }
    }

    fun onReset() {
        _state.value = GameState.empty()
    }

    fun onEnterPlacementMode() {
        _state.update { it.copy(error = null, isPlacementMode = true) }
    }

    fun onExitPlacementMode() {
        _state.update { it.copy(error = null, isPlacementMode = false) }
    }
}
