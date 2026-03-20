package com.lbbento.toyrobot.engine

import com.lbbento.toyrobot.engine.model.Command
import com.lbbento.toyrobot.engine.model.Direction
import com.lbbento.toyrobot.engine.model.Position
import com.lbbento.toyrobot.engine.model.ProcessResult
import com.lbbento.toyrobot.engine.model.RobotCommandException
import com.lbbento.toyrobot.engine.model.RobotErrorType
import com.lbbento.toyrobot.engine.model.RobotPosition
import javax.inject.Inject

internal class RobotGameEngineImpl @Inject constructor() : ProcessCommand, EngineConfig {

    override val tableSize: Int get() = TableSize

    override fun process(currentPosition: RobotPosition?, command: Command): ProcessResult {
        return when (command) {
            is Command.Place -> processPlace(command)
            is Command.Move -> withPlacedRobot(currentPosition) { processMove(it) }
            is Command.Left -> withPlacedRobot(currentPosition) { processTurn(it, ::rotateLeft) }
            is Command.Right -> withPlacedRobot(currentPosition) { processTurn(it, ::rotateRight) }
        }
    }

    private fun processPlace(command: Command.Place): ProcessResult {
        if (!command.position.isWithinBounds()) {
            throw RobotCommandException(RobotErrorType.POSITION_OUT_OF_BOUNDS)
        }
        val newState = RobotPosition(command.position, command.direction)
        return ProcessResult(newPosition = newState)
    }

    private fun processMove(currentState: RobotPosition): ProcessResult {
        val nextPosition = currentState.position.advance(currentState.direction)
        if (!nextPosition.isWithinBounds()) {
            throw RobotCommandException(RobotErrorType.MOVEMENT_WOULD_FALL)
        }
        val newState = currentState.copy(position = nextPosition)
        return ProcessResult(newPosition = newState)
    }

    private fun processTurn(
        currentState: RobotPosition,
        rotate: (Direction) -> Direction,
    ): ProcessResult {
        val newState = currentState.copy(direction = rotate(currentState.direction))
        return ProcessResult(newPosition = newState)
    }
}

private fun withPlacedRobot(
    currentState: RobotPosition?,
    block: (RobotPosition) -> ProcessResult,
): ProcessResult {
    if (currentState == null) {
        throw RobotCommandException(RobotErrorType.ROBOT_NOT_PLACED)
    }
    return block(currentState)
}

private fun Position.advance(direction: Direction): Position = when (direction) {
    Direction.NORTH -> copy(y = y + 1)
    Direction.EAST -> copy(x = x + 1)
    Direction.SOUTH -> copy(y = y - 1)
    Direction.WEST -> copy(x = x - 1)
}

private fun Position.isWithinBounds(): Boolean =
    x in 0 until TableSize && y in 0 until TableSize

private fun rotateLeft(direction: Direction): Direction = when (direction) {
    Direction.NORTH -> Direction.WEST
    Direction.WEST -> Direction.SOUTH
    Direction.SOUTH -> Direction.EAST
    Direction.EAST -> Direction.NORTH
}

private fun rotateRight(direction: Direction): Direction = when (direction) {
    Direction.NORTH -> Direction.EAST
    Direction.EAST -> Direction.SOUTH
    Direction.SOUTH -> Direction.WEST
    Direction.WEST -> Direction.NORTH
}

private const val TableSize = 5
