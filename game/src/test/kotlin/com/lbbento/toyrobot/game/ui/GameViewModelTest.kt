package com.lbbento.toyrobot.game.ui

import app.cash.turbine.test
import com.lbbento.toyrobot.game.domain.GameDirection
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testGetGameConfig = GetGameConfig { 5 }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        placeRobot: PlaceRobot = PlaceRobot { _, _ -> throw NotImplementedError() },
        moveRobot: MoveRobot = MoveRobot { _, _ -> throw NotImplementedError() },
        turnLeft: TurnLeft = TurnLeft { _, _ -> throw NotImplementedError() },
        turnRight: TurnRight = TurnRight { _, _ -> throw NotImplementedError() },
        getGameConfig: GetGameConfig = testGetGameConfig,
    ) = GameViewModel(placeRobot, moveRobot, turnLeft, turnRight, getGameConfig)

    @Nested
    inner class InitialState {

        @Test
        fun `initial state shows no robot placed`() {
            val viewModel = createViewModel()

            val state = viewModel.state.value
            assertFalse(state.isRobotPlaced)
            assertNull(state.robotPosition)
            assertNull(state.robotDirection)
        }
    }

    @Nested
    inner class StateMapping {

        @Test
        fun `placing a robot updates position and direction in state`() = runTest {
            val commandResult = GameRobotPosition(GamePosition(2, 3), GameDirection.NORTH)
            val viewModel = createViewModel(placeRobot = { _, _ -> commandResult })

            viewModel.onPlace(2, 3, GameDirection.NORTH)

            viewModel.state.test {
                val state = awaitItem()
                assertEquals(commandResult.position, state.robotPosition)
                assertEquals(commandResult.direction, state.robotDirection)
                assertTrue(state.isRobotPlaced)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class UserActions {

        @Test
        fun `onMove sends a Move command and updates the State`() = runTest {
            val commandResult = GameRobotPosition(GamePosition(0, 1), GameDirection.NORTH)
            val viewModel = createViewModel(moveRobot = { _, _ -> commandResult })

            viewModel.onMove()

            viewModel.state.test {
                val state = awaitItem()
                assertEquals(commandResult.position, state.robotPosition)
                assertEquals(commandResult.direction, state.robotDirection)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `onTurnLeft sends a Left command and updates the State`() = runTest {
            val commandResult = GameRobotPosition(GamePosition(0, 0), GameDirection.WEST)
            val viewModel = createViewModel(turnLeft = { _, _ -> commandResult })

            viewModel.onTurnLeft()

            viewModel.state.test {
                val state = awaitItem()
                assertEquals(commandResult.position, state.robotPosition)
                assertEquals(commandResult.direction, state.robotDirection)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `onTurnRight sends a Right command and updates the State`() = runTest {
            val commandResult = GameRobotPosition(GamePosition(0, 0), GameDirection.EAST)
            val viewModel = createViewModel(turnRight = { _, _ -> commandResult })

            viewModel.onTurnRight()

            viewModel.state.test {
                val state = awaitItem()
                assertEquals(commandResult.position, state.robotPosition)
                assertEquals(commandResult.direction, state.robotDirection)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `onReset clears robot position`() = runTest {
            val initialPosition = GameRobotPosition(
                GamePosition(1, 1), GameDirection.NORTH
            )
            val viewModel = createViewModel(
                placeRobot = { _, _ -> initialPosition }
            )

            viewModel.onPlace(
                initialPosition.position.x,
                initialPosition.position.y,
                initialPosition.direction
            )
            viewModel.onReset()

            viewModel.state.test {
                val state = awaitItem()
                assertFalse(state.isRobotPlaced)
                assertNull(state.robotPosition)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class PlacementMode {

        @Test
        fun `onEnterPlacementMode should set isPlacementMode to true`() = runTest {
            val viewModel = createViewModel()

            viewModel.onEnterPlacementMode()

            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state.isPlacementMode)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `onExitPlacementMode should set isPlacementMode to false`() = runTest {
            val viewModel = createViewModel()

            viewModel.onEnterPlacementMode()
            viewModel.onExitPlacementMode()

            viewModel.state.test {
                val state = awaitItem()
                assertFalse(state.isPlacementMode)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `onPlace should place robot and exit placement mode`() = runTest {
            val commandResult = GameRobotPosition(GamePosition(2, 3), GameDirection.EAST)
            val viewModel = createViewModel(
                placeRobot = { _, _ -> commandResult }
            )

            viewModel.onEnterPlacementMode()
            viewModel.onPlace(
                commandResult.position.x,
                commandResult.position.y,
                commandResult.direction
            )

            viewModel.state.test {
                val state = awaitItem()
                assertEquals(commandResult.position, state.robotPosition)
                assertEquals(commandResult.direction, state.robotDirection)
                assertFalse(state.isPlacementMode)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `onMove should exit placement mode`() = runTest {
            val position = GameRobotPosition(
                GamePosition(0, 0),
                GameDirection.NORTH
            )
            val viewModel = createViewModel(
                moveRobot = { _, _ -> position },
                placeRobot = { _, _ -> position },
            )

            viewModel.onPlace(position.position.x, position.position.y, position.direction)
            viewModel.onEnterPlacementMode()

            viewModel.state.test {
                val state = awaitItem()
                assertTrue(state.isPlacementMode)
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.onMove()

            viewModel.state.test {
                val state = awaitItem()
                assertFalse(state.isPlacementMode)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class ErrorHandling {

        @Test
        fun `error is null initially`() {
            val viewModel = createViewModel()

            assertNull(viewModel.state.value.error)
        }

        @Test
        fun `onMove sets error when robot not placed`() = runTest {
            val viewModel = createViewModel(
                moveRobot = { _, _ -> throw RobotNotPlacedException() }
            )

            viewModel.onMove()

            viewModel.state.test {
                val state = awaitItem()
                assertEquals(GameError.RobotNotPlaced, state.error)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `onPlace sets error when out of bounds`() = runTest {
            val viewModel = createViewModel(
                placeRobot = { _, _ -> throw OutOfBoundsException() }
            )

            viewModel.onPlace(0, 0, GameDirection.NORTH)

            viewModel.state.test {
                val state = awaitItem()
                assertEquals(GameError.OutOfBounds, state.error)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `onMove sets error when robot would fall`() = runTest {
            val viewModel = createViewModel(
                moveRobot = { _, _ -> throw MovementWouldFallException() }
            )

            viewModel.onMove()

            viewModel.state.test {
                val state = awaitItem()
                assertEquals(GameError.WouldFall, state.error)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `error is cleared on next action`() = runTest {
            val positionToPlace = GameRobotPosition(
                GamePosition(0, 0),
                GameDirection.NORTH
            )
            val viewModel = createViewModel(
                moveRobot = { _, _ -> throw RobotNotPlacedException() },
                placeRobot = { _, _ -> positionToPlace }
            )

            viewModel.onMove()

            viewModel.state.test {
                val errorState = awaitItem()
                assertEquals(GameError.RobotNotPlaced, errorState.error)
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.onPlace(
                positionToPlace.position.x,
                positionToPlace.position.y,
                positionToPlace.direction
            )

            viewModel.state.test {
                val clearedState = awaitItem()
                assertNull(clearedState.error)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `onReset clears error`() = runTest {
            val viewModel = createViewModel(
                moveRobot = { _, _ -> throw RobotNotPlacedException() }
            )

            viewModel.onMove()

            viewModel.state.test {
                val errorState = awaitItem()
                assertEquals(GameError.RobotNotPlaced, errorState.error)
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.onReset()

            viewModel.state.test {
                val clearedState = awaitItem()
                assertNull(clearedState.error)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
