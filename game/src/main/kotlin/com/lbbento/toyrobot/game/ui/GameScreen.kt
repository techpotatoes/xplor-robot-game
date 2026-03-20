package com.lbbento.toyrobot.game.ui

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lbbento.toyrobot.game.R
import com.lbbento.toyrobot.game.domain.GameDirection
import com.lbbento.toyrobot.game.domain.GamePosition

private const val PlacementHighlightOpacity = 0.7f

@Composable
fun GameScreen(viewModel: GameViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    GameScreenScaffold(
        state = state,
        gridSize = viewModel.gridSize,
        callbacks = GameCallbacks(
            onEnterPlacementMode = viewModel::onEnterPlacementMode,
            onExitPlacementMode = viewModel::onExitPlacementMode,
            onPlace = viewModel::onPlace,
            onMove = viewModel::onMove,
            onTurnLeft = viewModel::onTurnLeft,
            onTurnRight = viewModel::onTurnRight,
            onReset = viewModel::onReset,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GameScreenScaffold(
    state: GameState,
    gridSize: Int,
    callbacks: GameCallbacks,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_title_toy_robot)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
    ) { paddingValues ->
        GameContent(
            state = state,
            gridSize = gridSize,
            callbacks = callbacks,
            paddingValues = paddingValues,
        )
    }
}

@Composable
private fun GameContent(
    state: GameState,
    gridSize: Int,
    callbacks: GameCallbacks,
    paddingValues: PaddingValues,
) {
    var selectedDirection by remember { mutableStateOf(GameDirection.NORTH) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.spacedBy(GameDimensions.SpacingMedium),
    ) {
        GameGrid(
            robotPosition = state.robotPosition,
            robotDirection = state.robotDirection,
            isPlacementMode = state.isPlacementMode,
            gridSize = gridSize,
            onCellClick = { x, y -> callbacks.onPlace(x, y, selectedDirection) },
            modifier = Modifier.fillMaxWidth(),
        )
        GameInfoText(state = state)
        if (state.isPlacementMode) {
            PlacementModeContent(
                selectedDirection = selectedDirection,
                onDirectionSelected = { selectedDirection = it },
                onCancelClick = callbacks.onExitPlacementMode,
            )
        } else {
            MovementModeContent(state = state, callbacks = callbacks)
        }
    }
}

@Composable
private fun GameInfoText(state: GameState) {
    val infoText = when {
        state.isPlacementMode -> stringResource(R.string.tap_to_place_robot)
        state.robotPosition != null -> stringResource(
            R.string.report_format,
            state.robotPosition.x,
            state.robotPosition.y,
            state.robotDirection?.name.orEmpty(),
        )
        else -> ""
    }
    Text(
        text = infoText,
        style = MaterialTheme.typography.bodyMedium,
        color = if (infoText.isNotEmpty()) MaterialTheme.colorScheme.onSurface else Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun MovementModeContent(
    state: GameState,
    callbacks: GameCallbacks,
) {
    MovementRemoteControl(
        isRobotPlaced = state.isRobotPlaced,
        onMove = callbacks.onMove,
        onTurnLeft = callbacks.onTurnLeft,
        onTurnRight = callbacks.onTurnRight,
        onReset = callbacks.onReset,
    )
    ErrorBanner(error = state.error)
    Button(
        onClick = callbacks.onEnterPlacementMode,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.button_place))
    }
}

@Composable
private fun GameGrid(
    robotPosition: GamePosition?,
    robotDirection: GameDirection?,
    isPlacementMode: Boolean,
    gridSize: Int,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        for (y in (gridSize - 1) downTo 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                for (x in 0 until gridSize) {
                    val isRobotHere = robotPosition?.x == x && robotPosition.y == y
                    GridCell(
                        isRobotHere = isRobotHere,
                        robotDirection = if (isRobotHere) robotDirection else null,
                        isPlacementMode = isPlacementMode,
                        onClick = { onCellClick(x, y) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun GridCell(
    isRobotHere: Boolean,
    robotDirection: GameDirection?,
    isPlacementMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when {
        isRobotHere -> MaterialTheme.colorScheme.primaryContainer
        isPlacementMode -> MaterialTheme.colorScheme.secondaryContainer.copy(
            alpha = PlacementHighlightOpacity,
        )
        else -> MaterialTheme.colorScheme.surface
    }

    val rotationDegrees = robotDirection?.toRotationDegrees() ?: 0f

    val cellModifier = if (isPlacementMode) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Box(
        modifier = cellModifier
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        if (isRobotHere && robotDirection != null && !isPlacementMode) {
            Icon(
                painter = painterResource(R.drawable.robot_north),
                contentDescription = "Robot facing ${robotDirection.name}",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(GameDimensions.RobotIconSize)
                    .graphicsLayer { this.rotationZ = rotationDegrees }
            )
        }
    }
}

@Composable
private fun PlacementModeContent(
    selectedDirection: GameDirection,
    onDirectionSelected: (GameDirection) -> Unit,
    onCancelClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(R.string.select_direction),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(bottom = GameDimensions.SpacingSmall),
        )
        DirectionSelector(
            selectedDirection = selectedDirection,
            onDirectionSelected = onDirectionSelected,
        )
        OutlinedButton(
            onClick = onCancelClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        ) {
            Text(stringResource(R.string.button_cancel))
        }
    }
}

@Composable
private fun DirectionSelector(
    selectedDirection: GameDirection?,
    onDirectionSelected: (GameDirection) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(GameDimensions.SpacingSmall),
    ) {
        GameDirection.entries.forEach { direction ->
            DirectionButton(
                direction = direction,
                isSelected = selectedDirection == direction,
                onClick = { onDirectionSelected(direction) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DirectionButton(
    direction: GameDirection,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isSelected) {
        FilledTonalButton(onClick = onClick, modifier = modifier) {
            DirectionButtonContent(direction = direction)
        }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) {
            DirectionButtonContent(direction = direction)
        }
    }
}

@Composable
private fun DirectionButtonContent(direction: GameDirection) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(R.drawable.robot_north),
            contentDescription = "Direction ${direction.name}",
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .size(GameDimensions.RobotIconSmall)
                .graphicsLayer { this.rotationZ = direction.toRotationDegrees() }
        )
        Text(direction.name.first().toString())
    }
}

@Composable
private fun MovementRemoteControl(
    isRobotPlaced: Boolean,
    onMove: () -> Unit,
    onTurnLeft: () -> Unit,
    onTurnRight: () -> Unit,
    onReset: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(GameDimensions.SpacingSmall),
            verticalArrangement = Arrangement.spacedBy(GameDimensions.SpacingSmall),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(GameDimensions.SpacingSmall),
            ) {
                MovementControlButton(
                    onClick = onTurnLeft,
                    enabled = isRobotPlaced,
                    modifier = Modifier.weight(1f),
                    rotation = GameDirection.WEST.toRotationDegrees(),
                    label = stringResource(R.string.button_left),
                )
                Button(
                    onClick = onMove,
                    enabled = isRobotPlaced,
                    modifier = Modifier.weight(2f),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(R.drawable.robot_north),
                            contentDescription = "Move",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(GameDimensions.RobotIconTiny),
                        )
                        Text(stringResource(R.string.button_move))
                    }
                }
                MovementControlButton(
                    onClick = onTurnRight,
                    enabled = isRobotPlaced,
                    modifier = Modifier.weight(1f),
                    rotation = GameDirection.EAST.toRotationDegrees(),
                    label = stringResource(R.string.button_right),
                )
            }
            TextButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.button_reset))
            }
        }
    }
}

@Composable
private fun MovementControlButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier,
    rotation: Float,
    label: String,
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(R.drawable.robot_north),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(GameDimensions.RobotIconTiny)
                    .graphicsLayer { this.rotationZ = rotation }
            )
            Text(label)
        }
    }
}

@Composable
private fun ErrorBanner(error: GameError?) {
    AnimatedVisibility(visible = error != null) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = when (error) {
                    is GameError.RobotNotPlaced -> stringResource(R.string.error_robot_not_placed)
                    is GameError.OutOfBounds -> stringResource(R.string.error_position_out_of_bounds)
                    is GameError.WouldFall -> stringResource(R.string.error_move_would_fall)
                    null -> ""
                },
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(GameDimensions.SpacingMedium),
            )
        }
    }
}

class GameCallbacks(
    val onEnterPlacementMode: () -> Unit,
    val onExitPlacementMode: () -> Unit,
    val onPlace: (Int, Int, GameDirection) -> Unit,
    val onMove: () -> Unit,
    val onTurnLeft: () -> Unit,
    val onTurnRight: () -> Unit,
    val onReset: () -> Unit,
)

private fun GameDirection.toRotationDegrees(): Float = when (this) {
    GameDirection.NORTH -> 0f
    GameDirection.EAST -> 90f
    GameDirection.SOUTH -> 180f
    GameDirection.WEST -> 270f
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewGameContentEmpty() {
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

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewGame() {
    MaterialTheme {
        GameScreenScaffold(
            state = GameState(
                robotPosition = GamePosition(2, 3),
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

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewGameContentPlacementMode() {
    MaterialTheme {
        GameScreenScaffold(
            state = GameState(
                robotPosition = GamePosition(2, 3),
                robotDirection = GameDirection.NORTH,
                isPlacementMode = true,
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

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewGameContentWithError() {
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
