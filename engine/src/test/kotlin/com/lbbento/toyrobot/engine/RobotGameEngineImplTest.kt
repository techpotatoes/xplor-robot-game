package com.lbbento.toyrobot.engine

import com.lbbento.toyrobot.engine.model.Command
import com.lbbento.toyrobot.engine.model.Direction
import com.lbbento.toyrobot.engine.model.Position
import com.lbbento.toyrobot.engine.model.RobotCommandException
import com.lbbento.toyrobot.engine.model.RobotErrorType
import com.lbbento.toyrobot.engine.model.RobotPosition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RobotGameEngineImplTest {

    private val processor: ProcessCommand = RobotGameEngineImpl()

    @Nested
    inner class PlaceCommand {

        @Test
        fun `robot can be placed on the table at a valid position`() {
            val result = processor.process(null, Command.Place(Position(0, 0), Direction.NORTH))

            assertEquals(RobotPosition(Position(0, 0), Direction.NORTH), result.newPosition)
        }

        @Test
        fun `robot can be placed at the maximum boundary position`() {
            val result = processor.process(null, Command.Place(Position(4, 4), Direction.SOUTH))

            assertEquals(RobotPosition(Position(4, 4), Direction.SOUTH), result.newPosition)
        }

        @Test
        fun `robot cannot be placed outside the table with x greater than 4`() {
            val exception = assertThrows<RobotCommandException> {
                processor.process(null, Command.Place(Position(5, 0), Direction.NORTH))
            }
            assertEquals(RobotErrorType.POSITION_OUT_OF_BOUNDS, exception.errorType)
        }

        @Test
        fun `robot cannot be placed outside the table with y greater than 4`() {
            val exception = assertThrows<RobotCommandException> {
                processor.process(null, Command.Place(Position(0, 5), Direction.NORTH))
            }
            assertEquals(RobotErrorType.POSITION_OUT_OF_BOUNDS, exception.errorType)
        }

        @Test
        fun `robot cannot be placed with negative x coordinate`() {
            val exception = assertThrows<RobotCommandException> {
                processor.process(null, Command.Place(Position(-1, 0), Direction.NORTH))
            }
            assertEquals(RobotErrorType.POSITION_OUT_OF_BOUNDS, exception.errorType)
        }

        @Test
        fun `robot cannot be placed with negative y coordinate`() {
            val exception = assertThrows<RobotCommandException> {
                processor.process(null, Command.Place(Position(0, -1), Direction.NORTH))
            }
            assertEquals(RobotErrorType.POSITION_OUT_OF_BOUNDS, exception.errorType)
        }

        @Test
        fun `a second PLACE command repositions the robot`() {
            val initialState = RobotPosition(Position(0, 0), Direction.NORTH)

            val result =
                processor.process(initialState, Command.Place(Position(3, 3), Direction.EAST))

            assertEquals(RobotPosition(Position(3, 3), Direction.EAST), result.newPosition)
        }
    }

    @Nested
    inner class MoveCommand {

        @Test
        fun `MOVE advances robot one unit north when facing NORTH`() {
            val state = RobotPosition(Position(0, 0), Direction.NORTH)

            val result = processor.process(state, Command.Move)

            assertEquals(RobotPosition(Position(0, 1), Direction.NORTH), result.newPosition)
        }

        @Test
        fun `MOVE advances robot one unit east when facing EAST`() {
            val state = RobotPosition(Position(0, 0), Direction.EAST)

            val result = processor.process(state, Command.Move)

            assertEquals(RobotPosition(Position(1, 0), Direction.EAST), result.newPosition)
        }

        @Test
        fun `MOVE advances robot one unit south when facing SOUTH`() {
            val state = RobotPosition(Position(0, 1), Direction.SOUTH)

            val result = processor.process(state, Command.Move)

            assertEquals(RobotPosition(Position(0, 0), Direction.SOUTH), result.newPosition)
        }

        @Test
        fun `MOVE advances robot one unit west when facing WEST`() {
            val state = RobotPosition(Position(1, 0), Direction.WEST)

            val result = processor.process(state, Command.Move)

            assertEquals(RobotPosition(Position(0, 0), Direction.WEST), result.newPosition)
        }

        @Test
        fun `MOVE is ignored when it would cause the robot to fall off the north edge`() {
            val state = RobotPosition(Position(0, 4), Direction.NORTH)

            val exception = assertThrows<RobotCommandException> {
                processor.process(state, Command.Move)
            }
            assertEquals(RobotErrorType.MOVEMENT_WOULD_FALL, exception.errorType)
        }

        @Test
        fun `MOVE is ignored when it would cause the robot to fall off the east edge`() {
            val state = RobotPosition(Position(4, 0), Direction.EAST)

            val exception = assertThrows<RobotCommandException> {
                processor.process(state, Command.Move)
            }
            assertEquals(RobotErrorType.MOVEMENT_WOULD_FALL, exception.errorType)
        }

        @Test
        fun `MOVE is ignored when it would cause the robot to fall off the south edge`() {
            val state = RobotPosition(Position(0, 0), Direction.SOUTH)

            val exception = assertThrows<RobotCommandException> {
                processor.process(state, Command.Move)
            }
            assertEquals(RobotErrorType.MOVEMENT_WOULD_FALL, exception.errorType)
        }

        @Test
        fun `MOVE is ignored when it would cause the robot to fall off the west edge`() {
            val state = RobotPosition(Position(0, 0), Direction.WEST)

            val exception = assertThrows<RobotCommandException> {
                processor.process(state, Command.Move)
            }
            assertEquals(RobotErrorType.MOVEMENT_WOULD_FALL, exception.errorType)
        }

        @Test
        fun `MOVE is ignored when robot has not been placed`() {
            val exception = assertThrows<RobotCommandException> {
                processor.process(null, Command.Move)
            }
            assertEquals(RobotErrorType.ROBOT_NOT_PLACED, exception.errorType)
        }
    }

    @Nested
    inner class LeftCommand {

        @Test
        fun `LEFT rotates robot from NORTH to WEST`() {
            val state = RobotPosition(Position(0, 0), Direction.NORTH)

            val result = processor.process(state, Command.Left)

            assertEquals(Direction.WEST, result.newPosition.direction)
            assertEquals(state.position, result.newPosition.position)
        }

        @Test
        fun `LEFT rotates robot from WEST to SOUTH`() {
            val state = RobotPosition(Position(0, 0), Direction.WEST)

            val result = processor.process(state, Command.Left)

            assertEquals(Direction.SOUTH, result.newPosition.direction)
        }

        @Test
        fun `LEFT rotates robot from SOUTH to EAST`() {
            val state = RobotPosition(Position(0, 0), Direction.SOUTH)

            val result = processor.process(state, Command.Left)

            assertEquals(Direction.EAST, result.newPosition.direction)
        }

        @Test
        fun `LEFT rotates robot from EAST to NORTH`() {
            val state = RobotPosition(Position(0, 0), Direction.EAST)

            val result = processor.process(state, Command.Left)

            assertEquals(Direction.NORTH, result.newPosition.direction)
        }

        @Test
        fun `LEFT does not change the position of the robot`() {
            val state = RobotPosition(Position(2, 3), Direction.NORTH)

            val result = processor.process(state, Command.Left)

            assertEquals(Position(2, 3), result.newPosition.position)
        }

        @Test
        fun `LEFT is ignored when robot has not been placed`() {
            val exception = assertThrows<RobotCommandException> {
                processor.process(null, Command.Left)
            }
            assertEquals(RobotErrorType.ROBOT_NOT_PLACED, exception.errorType)
        }
    }

    @Nested
    inner class RightCommand {

        @Test
        fun `RIGHT rotates robot from NORTH to EAST`() {
            val state = RobotPosition(Position(0, 0), Direction.NORTH)

            val result = processor.process(state, Command.Right)

            assertEquals(Direction.EAST, result.newPosition.direction)
        }

        @Test
        fun `RIGHT rotates robot from EAST to SOUTH`() {
            val state = RobotPosition(Position(0, 0), Direction.EAST)

            val result = processor.process(state, Command.Right)

            assertEquals(Direction.SOUTH, result.newPosition.direction)
        }

        @Test
        fun `RIGHT rotates robot from SOUTH to WEST`() {
            val state = RobotPosition(Position(0, 0), Direction.SOUTH)

            val result = processor.process(state, Command.Right)

            assertEquals(Direction.WEST, result.newPosition.direction)
        }

        @Test
        fun `RIGHT rotates robot from WEST to NORTH`() {
            val state = RobotPosition(Position(0, 0), Direction.WEST)

            val result = processor.process(state, Command.Right)

            assertEquals(Direction.NORTH, result.newPosition.direction)
        }

        @Test
        fun `RIGHT does not change the position of the robot`() {
            val state = RobotPosition(Position(2, 3), Direction.NORTH)

            val result = processor.process(state, Command.Right)

            assertEquals(Position(2, 3), result.newPosition.position)
        }

        @Test
        fun `RIGHT is ignored when robot has not been placed`() {
            val exception = assertThrows<RobotCommandException> {
                processor.process(null, Command.Right)
            }
            assertEquals(RobotErrorType.ROBOT_NOT_PLACED, exception.errorType)
        }
    }

    @Nested
    inner class SpecificationExamples {

        @Test
        fun `example a - PLACE 0,0,NORTH then MOVE results in 0,1,NORTH`() {
            var state: RobotPosition? = null

            state =
                processor.process(state, Command.Place(Position(0, 0), Direction.NORTH)).newPosition
            state = processor.process(state, Command.Move).newPosition

            assertEquals(Position(0, 1), state.position)
            assertEquals(Direction.NORTH, state.direction)
        }

        @Test
        fun `example b - PLACE 0,0,NORTH then LEFT results in 0,0,WEST`() {
            var state: RobotPosition? = null

            state =
                processor.process(state, Command.Place(Position(0, 0), Direction.NORTH)).newPosition
            state = processor.process(state, Command.Left).newPosition

            assertEquals(Position(0, 0), state.position)
            assertEquals(Direction.WEST, state.direction)
        }

        @Test
        fun `example c - PLACE 1,2,EAST then MOVE MOVE LEFT MOVE results in 3,3,NORTH`() {
            var state: RobotPosition? = null

            state =
                processor.process(state, Command.Place(Position(1, 2), Direction.EAST)).newPosition
            state = processor.process(state, Command.Move).newPosition
            state = processor.process(state, Command.Move).newPosition
            state = processor.process(state, Command.Left).newPosition
            state = processor.process(state, Command.Move).newPosition

            assertEquals(Position(3, 3), state.position)
            assertEquals(Direction.NORTH, state.direction)
        }
    }

    @Nested
    inner class CommandsBeforePlace {

        @Test
        fun `MOVE before PLACE is ignored`() {
            val exception = assertThrows<RobotCommandException> {
                processor.process(null, Command.Move)
            }
            assertEquals(RobotErrorType.ROBOT_NOT_PLACED, exception.errorType)
        }

        @Test
        fun `LEFT before PLACE is ignored`() {
            val exception = assertThrows<RobotCommandException> {
                processor.process(null, Command.Left)
            }
            assertEquals(RobotErrorType.ROBOT_NOT_PLACED, exception.errorType)
        }

        @Test
        fun `RIGHT before PLACE is ignored`() {
            val exception = assertThrows<RobotCommandException> {
                processor.process(null, Command.Right)
            }
            assertEquals(RobotErrorType.ROBOT_NOT_PLACED, exception.errorType)
        }

        @Test
        fun `valid commands are accepted after a valid PLACE`() {
            var state: RobotPosition? = null

            val exception1 = assertThrows<RobotCommandException> {
                state = processor.process(state, Command.Move).newPosition
            }
            assertEquals(RobotErrorType.ROBOT_NOT_PLACED, exception1.errorType)

            val exception2 = assertThrows<RobotCommandException> {
                state = processor.process(state, Command.Left).newPosition
            }
            assertEquals(RobotErrorType.ROBOT_NOT_PLACED, exception2.errorType)

            val exception3 = assertThrows<RobotCommandException> {
                state = processor.process(state, Command.Right).newPosition
            }
            assertEquals(RobotErrorType.ROBOT_NOT_PLACED, exception3.errorType)

            assertEquals(null, state)

            state =
                processor.process(state, Command.Place(Position(2, 2), Direction.NORTH)).newPosition
            assertNotNull(state)

            val result = processor.process(state, Command.Move)

            assertEquals(RobotPosition(Position(2, 3), Direction.NORTH), result.newPosition)
        }
    }
}
