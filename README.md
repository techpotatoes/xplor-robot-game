# Toy Robot Simulator

A simulation of a toy robot moving on a 5x5 square tabletop. The robot responds to PLACE, MOVE, LEFT, and RIGHT commands via a button-driven UI. The robot must not fall off the table — invalid moves are ignored with feedback.

## Architecture

Three Gradle modules following Clean Architecture with MVVM:

- **`:engine`** — Pure Kotlin JVM library (no Android). Owns all game rules and boundary validation. Exposes a single stateless `ProcessCommand` fun interface; `RobotGameEngineImpl` implements it. No state is held here.
- **`:game`** — Android library. Contains three sub-layers:
  - *Data*: `GameEngineInteractorImpl` — implements all use case interfaces, maps between engine types and game domain types, and converts engine exceptions into game domain exceptions. This is the sole file that imports engine types.
  - *Domain*: Five use case `fun interface`s (`PlaceRobot`, `MoveRobot`, `TurnLeft`, `TurnRight`, `GetGameConfig`) and the game's own model types (`GamePosition`, `GameDirection`, `GameRobotPosition`, `GameException`).
  - *UI*: `GameViewModel` (owns `StateFlow<GameState>`, error handling), `GameScreen` (Compose, state-hoisted), `GameState` (single UI state), `GameError` (sealed error representation for UI).
- **`:app`** — Android application entry point. `ToyRobotApplication` (`@HiltAndroidApp`) + `MainActivity` + Material3 theme. No business logic.

### Module Dependencies

```
:app
 └── :game (feature module)
      └── :engine (pure Kotlin, no Android)
```

Object-level dependency direction:

```
GameScreen
    └── GameViewModel
         ├── PlaceRobot   ─┐
         ├── MoveRobot    ─┤
         ├── TurnLeft     ─┼── GameEngineInteractorImpl
         ├── TurnRight    ─┤        └── ProcessCommand ── RobotGameEngineImpl
         └── GetGameConfig─┘
```

### Command/Data Flow

What happens when the user taps Move:

1. User taps Move → `GameScreen` invokes the ViewModel callback
2. ViewModel calls `moveRobot.move(currentPosition, currentDirection)`
3. `GameEngineInteractorImpl.move()` maps game types to engine types and calls `processCommand.process(RobotPosition, Command.Move)`
4. `RobotGameEngineImpl` validates bounds and returns a `ProcessResult` (or throws `RobotCommandException`)
5. `GameEngineInteractorImpl` maps the result back to `GameRobotPosition`, or converts the engine exception to a `GameException` subtype
6. `GameViewModel.executeWithErrorHandling` catches `GameException`, maps it to a `GameError` value, and updates `MutableStateFlow<GameState>`
7. `GameScreen` recomposes

### Design Decisions

- **Per-command use case interfaces** — `PlaceRobot`, `MoveRobot`, `TurnLeft`, `TurnRight` each have a single, descriptively named method. Each consumer declares exactly the capability it needs, and each interface is trivially replaced with a lambda in tests.
- **Engine type isolation** — `:game` owns its own model types (`GamePosition`, `GameDirection`, `GameRobotPosition`). `GameEngineInteractorImpl` is the only place engine types cross the boundary, acting as an anti-corruption layer.
- **Stateless engine** — `ProcessCommand` takes `(currentState?, command)` and returns `ProcessResult`. No hidden state, fully deterministic, easy to unit-test in isolation.
- **Fakes over mocks** — test fakes are explicit, readable, and don't couple tests to implementation details. No Mockito/MockK dependency needed.
- **Continuous REPORT instead of explicit command** — The specification requires REPORT to "announce the X,Y and F of the robot" but leaves the output format open. Rather than an explicit REPORT command that outputs to console (which doesn't fit a graphical UI), the robot's position and direction are displayed continuously in the UI header. This provides better UX — the user always sees the robot's state without needing to request it. The requirement is satisfied; the reporting is continuous rather than on-demand.
- **Grid-based touch UI** — The specification allows either text output or graphical display. A touch-based grid was chosen for better interactivity: users tap cells to place the robot rather than typing coordinates. This also demonstrates modern Android UI practices with Jetpack Compose.

## Build & Run

```bash
# Build the app
./gradlew assembleDebug

# Deploy to a device/emulator from Android Studio
# Run configuration: app
```

## Testing

```bash
# Run all tests
./gradlew test

# Run engine tests only
./gradlew :engine:test

# Run game module tests only
./gradlew :game:testDebugUnitTest

# Record Paparazzi snapshots
./gradlew :game:recordPaparazziDebug

# Verify Paparazzi snapshots
./gradlew :game:verifyPaparazziDebug
```

The test suite covers all specification example scenarios:
- **Example A** (`PLACE 0,0,NORTH → MOVE → REPORT`): See `RobotGameEngineImplTest.kt` → "PlaceCommand" and "MoveCommand" tests
- **Example B** (`PLACE 0,0,NORTH → LEFT → REPORT`): See `RobotGameEngineImplTest.kt` → "LeftCommand" tests
- **Example C** (`PLACE 1,2,EAST → MOVE → MOVE → LEFT → MOVE → REPORT`): See `RobotGameEngineImplTest.kt` → "SpecificationExamples" nested class

## Static Analysis

```bash
./gradlew detekt
```

Zero-tolerance configuration (`maxIssues: 0`).

## Known Limitations

**Landscape orientation is not supported.** This is intentional — the app is a take-home exercise and demo, and responsive layout work is out of scope.

## Tech Stack

| Category | Library | Version |
|---|---|---|
| Language | Kotlin | 2.2.10 |
| Build | AGP | 9.0.0 |
| UI | Jetpack Compose (BOM) | 2025.05.01 |
| DI | Hilt | 2.59.2 |
| Async | Coroutines | 1.10.2 |
| Testing | JUnit5 | 5.11.4 |
| Testing | Turbine | 1.2.0 |
| Snapshots | Paparazzi | 2.0.0-alpha04 |
| Analysis | Detekt | 1.23.8 |
