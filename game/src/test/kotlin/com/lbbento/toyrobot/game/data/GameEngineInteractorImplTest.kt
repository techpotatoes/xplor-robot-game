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
import com.lbbento.toyrobot.game.domain.GamePosition
import com.lbbento.toyrobot.game.domain.MovementWouldFallException
import com.lbbento.toyrobot.game.domain.OutOfBoundsException
import com.lbbento.toyrobot.game.domain.RobotNotPlacedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameEngineInteractorImplTest {

    private lateinit var fakeProcessCommand: FakeProcessCommand
    private lateinit var interactor: GameEngineInteractorImpl

    @BeforeEach
    fun setUp() {
        fakeProcessCommand = FakeProcessCommand()
        interactor = GameEngineInteractorImpl(
            processCommand = fakeProcessCommand,
            engineConfig = object : EngineConfig {
                override val tableSize = 5
            },
        )
    }

    @Nested
    inner class ExecutingCommands {

        @Test
        fun `place returns the new robot position`() {
            val enginePosition = RobotPosition(Position(0, 0), Direction.NORTH)
            fakeProcessCommand.resultToReturn = ProcessResult(newPosition = enginePosition)

            val result = interactor.place(GamePosition(0, 0), GameDirection.NORTH)

            assertEquals(GamePosition(0, 0), result.position)
            assertEquals(GameDirection.NORTH, result.direction)
            assertEquals(
                Command.Place(Position(0, 0), Direction.NORTH),
                fakeProcessCommand.lastReceivedCommand
            )
        }

        @Test
        fun `move assembles RobotPosition from position and direction before passing to processor`() {
            fakeProcessCommand.resultToReturn =
                ProcessResult(newPosition = RobotPosition(Position(0, 0), Direction.NORTH))

            interactor.move(GamePosition(2, 3), GameDirection.EAST)

            assertEquals(
                RobotPosition(Position(2, 3), Direction.EAST),
                fakeProcessCommand.lastReceivedPosition
            )
            assertEquals(Command.Move, fakeProcessCommand.lastReceivedCommand)
        }

        @Test
        fun `move passes null to processor when position is null`() {
            fakeProcessCommand.resultToReturn =
                ProcessResult(newPosition = RobotPosition(Position(0, 0), Direction.NORTH))

            interactor.move(null, null)

            assertNull(fakeProcessCommand.lastReceivedPosition)
        }

        @Test
        fun `move passes null to processor when position is null but direction is not`() {
            fakeProcessCommand.resultToReturn =
                ProcessResult(newPosition = RobotPosition(Position(0, 0), Direction.NORTH))

            interactor.move(null, GameDirection.NORTH)

            assertNull(fakeProcessCommand.lastReceivedPosition)
        }

        @Test
        fun `move passes null to processor when direction is null but position is not`() {
            fakeProcessCommand.resultToReturn =
                ProcessResult(newPosition = RobotPosition(Position(0, 0), Direction.NORTH))

            interactor.move(GamePosition(1, 1), null)

            assertNull(fakeProcessCommand.lastReceivedPosition)
        }

        @Test
        fun `turnLeft passes Command Left to processor`() {
            fakeProcessCommand.resultToReturn =
                ProcessResult(newPosition = RobotPosition(Position(0, 0), Direction.NORTH))

            interactor.turnLeft(GamePosition(1, 1), GameDirection.NORTH)

            assertEquals(Command.Left, fakeProcessCommand.lastReceivedCommand)
        }

        @Test
        fun `turnRight passes Command Right to processor`() {
            fakeProcessCommand.resultToReturn =
                ProcessResult(newPosition = RobotPosition(Position(0, 0), Direction.NORTH))

            interactor.turnRight(GamePosition(1, 1), GameDirection.NORTH)

            assertEquals(Command.Right, fakeProcessCommand.lastReceivedCommand)
        }
    }

    @Nested
    inner class ErrorMapping {

        @Test
        fun `ROBOT_NOT_PLACED maps to RobotNotPlacedException`() {
            fakeProcessCommand.exceptionToThrow =
                RobotCommandException(RobotErrorType.ROBOT_NOT_PLACED)

            assertThrows<RobotNotPlacedException> { interactor.move(null, null) }
        }

        @Test
        fun `POSITION_OUT_OF_BOUNDS maps to OutOfBoundsException`() {
            fakeProcessCommand.exceptionToThrow =
                RobotCommandException(RobotErrorType.POSITION_OUT_OF_BOUNDS)

            assertThrows<OutOfBoundsException> {
                interactor.move(
                    GamePosition(0, 0),
                    GameDirection.NORTH
                )
            }
        }

        @Test
        fun `MOVEMENT_WOULD_FALL maps to MovementWouldFallException`() {
            fakeProcessCommand.exceptionToThrow =
                RobotCommandException(RobotErrorType.MOVEMENT_WOULD_FALL)

            assertThrows<MovementWouldFallException> {
                interactor.move(
                    GamePosition(0, 0),
                    GameDirection.NORTH
                )
            }
        }
    }

    @Nested
    inner class GettingConfig {

        @Test
        fun `getTableSize returns the table size from engine config`() {
            assertEquals(5, interactor.getTableSize())
        }
    }
}

private class FakeProcessCommand : ProcessCommand {
    var resultToReturn = ProcessResult(RobotPosition(Position(0, 0), Direction.NORTH))
    var lastReceivedPosition: RobotPosition? = null
    var lastReceivedCommand: Command? = null
    var exceptionToThrow: Exception? = null

    override fun process(currentPosition: RobotPosition?, command: Command): ProcessResult {
        exceptionToThrow?.let { throw it }
        lastReceivedPosition = currentPosition
        lastReceivedCommand = command
        return resultToReturn
    }
}
