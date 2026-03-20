package com.lbbento.toyrobot.game.ui

import androidx.compose.material3.MaterialTheme
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.lbbento.toyrobot.game.domain.GameDirection
import com.lbbento.toyrobot.game.domain.GamePosition

import org.junit.Rule
import org.junit.Test

class GameScreenSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5,
    )

    @Test
    fun emptyState_noRobotPlaced() {
        paparazzi.snapshot {
            MaterialTheme {
                GameScreenScaffold(
                    state = GameState.empty(),
                    gridSize = 5,
                    callbacks = GameCallbacks(
                        onEnterPlacementMode = {},
                        onExitPlacementMode = {},
                        onPlace = { _, _, _ -> },
                        onMove = {},
                        onTurnLeft = {},
                        onTurnRight = {},
                        onReset = {},
                    )
                )
            }
        }
    }

    @Test
    fun robotPlaced_atCenterFacingNorth() {
        paparazzi.snapshot {
            MaterialTheme {
                GameScreenScaffold(
                    state = GameState(
                        robotPosition = GamePosition(2, 2),
                        robotDirection = GameDirection.NORTH,
                    ),
                    gridSize = 5,
                    callbacks = GameCallbacks(
                        onEnterPlacementMode = {},
                        onExitPlacementMode = {},
                        onPlace = { _, _, _ -> },
                        onMove = {},
                        onTurnLeft = {},
                        onTurnRight = {},
                        onReset = {},
                    )
                )
            }
        }
    }

    @Test
    fun errorBanner_robotNotPlaced() {
        paparazzi.snapshot {
            MaterialTheme {
                GameScreenScaffold(
                    state = GameState(
                        robotPosition = null,
                        robotDirection = null,
                        error = GameError.RobotNotPlaced,
                    ),
                    gridSize = 5,
                    callbacks = GameCallbacks(
                        onEnterPlacementMode = {},
                        onExitPlacementMode = {},
                        onPlace = { _, _, _ -> },
                        onMove = {},
                        onTurnLeft = {},
                        onTurnRight = {},
                        onReset = {},
                    )
                )
            }
        }
    }
}
