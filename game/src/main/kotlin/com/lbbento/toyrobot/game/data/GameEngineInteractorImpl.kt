package com.lbbento.toyrobot.game.data

import com.lbbento.toyrobot.engine.EngineConfig
import com.lbbento.toyrobot.engine.ProcessCommand
import com.lbbento.toyrobot.engine.model.Command
import com.lbbento.toyrobot.engine.model.Direction
import com.lbbento.toyrobot.engine.model.Position
import com.lbbento.toyrobot.engine.model.ProcessResult
import com.lbbento.toyrobot.engine.model.RobotCommandException
import com.lbbento.toyrobot.engine.model.RobotErrorType
import com.lbbento.toyrobot.engine.model.RobotPosition
import com.lbbento.toyrobot.game.domain.GameDirection
import com.lbbento.toyrobot.game.domain.GameException
import com.lbbento.toyrobot.game.domain.GamePosition
import com.lbbento.toyrobot.game.domain.GameRobotPosition
import com.lbbento.toyrobot.game.domain.GetGameConfig
import com.lbbento.toyrobot.game.domain.MoveRobot
import com.lbbento.toyrobot.game.domain.MovementWouldFallException
import com.lbbento.toyrobot.game.domain.OutOfBoundsException
import com.lbbento.toyrobot.game.domain.PlaceRobot
import com.lbbento.toyrobot.game.domain.RobotNotPlacedException
import com.lbbento.toyrobot.game.domain.TurnLeft
import com.lbbento.toyrobot.game.domain.TurnRight
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameEngineInteractorImpl @Inject constructor(
    private val processCommand: ProcessCommand,
    private val engineConfig: EngineConfig,
) : PlaceRobot, MoveRobot, TurnLeft, TurnRight, GetGameConfig {

    override fun place(position: GamePosition, direction: GameDirection): GameRobotPosition =
        execute {
            processCommand.process(
                null,
                Command.Place(position.toEngine(), direction.toEngine())
            )
        }

    override fun move(position: GamePosition?, direction: GameDirection?): GameRobotPosition =
        execute { processCommand.process(currentRobotPosition(position, direction), Command.Move) }

    override fun turnLeft(position: GamePosition?, direction: GameDirection?): GameRobotPosition =
        execute { processCommand.process(currentRobotPosition(position, direction), Command.Left) }

    override fun turnRight(position: GamePosition?, direction: GameDirection?): GameRobotPosition =
        execute { processCommand.process(currentRobotPosition(position, direction), Command.Right) }

    override fun getTableSize(): Int = engineConfig.tableSize

    private fun execute(block: () -> ProcessResult): GameRobotPosition {
        try {
            return block().newPosition.toGame()
        } catch (e: RobotCommandException) {
            throw e.toGame()
        }
    }

    private fun currentRobotPosition(position: GamePosition?, direction: GameDirection?) =
        if (position != null && direction != null) RobotPosition(
            position.toEngine(),
            direction.toEngine()
        ) else null

    private fun GamePosition.toEngine() = Position(x, y)

    private fun GameDirection.toEngine() = Direction.valueOf(name)

    private fun RobotPosition.toGame() =
        GameRobotPosition(
            GamePosition(position.x, position.y),
            GameDirection.valueOf(direction.name)
        )

    private fun RobotCommandException.toGame(): GameException = when (errorType) {
        RobotErrorType.ROBOT_NOT_PLACED -> RobotNotPlacedException()
        RobotErrorType.POSITION_OUT_OF_BOUNDS -> OutOfBoundsException()
        RobotErrorType.MOVEMENT_WOULD_FALL -> MovementWouldFallException()
    }
}
