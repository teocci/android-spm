# Sudoku Puzzle Master

A modern Android Sudoku game built with Kotlin and Jetpack Compose, featuring multiple difficulty levels, daily challenges, and a comprehensive scoring system.

## Features

### Core Gameplay
- **9x9 Sudoku Grid**: Classic Sudoku puzzle with 3x3 box constraints
- **Four Difficulty Levels**: Easy, Medium, Hard, and Expert with varying complexity
- **Smart Cell Highlighting**: Visual feedback for selected cells, same numbers, and related rows/columns/boxes
- **Mistake Tracking**: Maximum 3 mistakes allowed per game
- **Timer**: Track your solving time with pause functionality

### Advanced Features
- **Notes System**: 3x3 mini-grid note-taking with auto-removal when numbers are placed
- **Hint System**: Get help when stuck (costs 50 points)
- **Undo Functionality**: Revert your last number placement
- **Long-Press Number Lock**: Lock a number for quick multi-cell entry
- **Completion Animations**: Visual celebrations for row, column, and box completions

### Scoring System
Earn points for correct placements with difficulty multipliers:
- Correct number: 10 points × multiplier
- Row/Column/Box completion: 100 points × multiplier
- Game completion: 500 points × multiplier
- Time bonus: Additional points for faster completion
- Penalties: -50 for hints, -20 for mistakes

**Difficulty Multipliers**: Easy (1.0×), Medium (1.5×), Hard (2.0×), Expert (3.0×)

### Daily Challenges
- Unique puzzle every day (same for all players)
- Monthly calendar view with completion tracking
- Trophy display for achievements
- Replayable past challenges

### Statistics & Profile
- Track your game history and performance
- View completion statistics by difficulty
- Achievement system

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material3 Design
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: Navigation Compose
- **State Management**: StateFlow for reactive UI updates
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Java Version**: 11

## Project Structure

```
app/src/main/java/com/github/teocci/sudoku/
├── core/                      # Core utilities and constants
├── domain/                    # Business logic layer
│   ├── model/                # Game models (Cell, Board, GameState, etc.)
│   ├── validator/            # Sudoku validation rules
│   ├── solver/               # Backtracking solver algorithm
│   ├── generator/            # Puzzle generation with difficulty
│   └── scoring/              # Point calculation engine
├── data/                      # Data persistence layer
│   ├── local/                # SharedPreferences and local storage
│   └── repository/           # Data repositories
├── presentation/              # UI layer (Compose)
│   ├── components/           # Reusable UI components
│   ├── game/                 # Game screen and ViewModel
│   ├── main/                 # Home/difficulty selection
│   ├── daily/                # Daily challenges screen
│   ├── profile/              # Statistics screen
│   ├── settings/             # Settings screen
│   └── navigation/           # Navigation graph and bottom bar
└── ui/theme/                  # Theme configuration (colors, typography)
```

## Architecture

The app follows **Clean Architecture** principles with clear separation of concerns:

- **Domain Layer**: Pure Kotlin business logic (validation, solving, generation, scoring)
- **Data Layer**: Repositories and data sources (preferences, storage)
- **Presentation Layer**: Jetpack Compose UI with ViewModels managing state

### Key Design Patterns
- **MVVM**: ViewModels expose StateFlow for reactive UI
- **Repository Pattern**: Abstract data sources
- **Command Pattern**: Undo functionality via action history
- **Strategy Pattern**: Flexible scoring algorithms

## Build Instructions

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or later
- Android SDK with API level 24+

### Build the project
```bash
./gradlew build
```

### Run tests
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator or device)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests com.github.teocci.sudoku.ExampleUnitTest
```

### Install on device/emulator
```bash
# Debug build
./gradlew installDebug

# Release build
./gradlew installRelease
```

### Clean build
```bash
./gradlew clean
```

### Lint checks
```bash
./gradlew lint
```

## Development Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle dependencies
4. Run on an emulator or physical device (API 24+)

## Dependencies

Dependencies are managed using Gradle version catalogs (`gradle/libs.versions.toml`):
- AndroidX Core KTX
- Lifecycle & ViewModel
- Jetpack Compose (UI, Material3, Navigation)
- Compose BOM for version alignment
- JUnit & Espresso for testing

## Game Rules

1. Fill the 9x9 grid so that each row, column, and 3x3 box contains digits 1-9
2. Each digit must appear exactly once in each row, column, and box
3. You have 3 mistakes allowed per game
4. Use hints sparingly (they cost points)
5. Complete rows, columns, and boxes for bonus points

## Settings

Customize your gameplay experience:
- Sound effects toggle
- Haptic feedback toggle
- Auto-remove notes when placing numbers
- Highlight related cells (row/column/box)
- Highlight cells with same number

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

**Teocci**
- GitHub: [@teocci](https://github.com/teocci)
- Package: com.github.teocci.sudoku

## Acknowledgments

Built with modern Android development best practices and Jetpack Compose.
