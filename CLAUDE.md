# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Build and Run

```bash
sbt compile                    # Compile the project
sbt clean                      # Clean build artifacts
sbt test                      # Run all tests
sbt run                       # Run the main application

# Run individual examples
sbt "runMain com.example.FileAccessTest"
sbt "runMain com.example.FiberDemo"
sbt "runMain com.example.HubDemo"
sbt "runMain com.example.LoopDemo"
sbt "runMain com.example.ClockDemo"
sbt "runMain com.example.QueueDemo"
sbt "runMain com.example.choice.ChoiceDemo"
sbt "runMain com.example.choice.MazeSolver"
sbt "runMain com.example.choice.SudokuSolver"
sbt "runMain com.example.choice.NQueens"
```

### Code Quality

```bash
sbt scalafmtAll               # Format code with Scalafmt
sbt scalafmtCheck             # Check formatting without changing
```

## Project Architecture

This is a Scala 3 playground project demonstrating the Kyo effect system. The project showcases various Kyo features through practical examples with comprehensive testing.

### Core Components

- **Example Applications**: Standalone applications in `src/main/scala/com/example/` demonstrating specific Kyo effects
- **Choice Examples**: Advanced non-deterministic computation examples in `src/main/scala/com/example/choice/`
- **Test Suites**: Comprehensive tests in `src/test/scala/` using Kyo's testing utilities

### Effect System

All examples extend `KyoApp` and demonstrate different Kyo effects:

- `Sync`, `Async` - Basic synchronous/asynchronous operations
- `Fiber` - Concurrent execution
- `Choice` - Non-deterministic computation
- `Abort` - Error handling
- `Loop` - Tail-recursive operations
- `Stream` - Stream processing
- `Channel` - Concurrent data flow
- `Hub` - Multi-producer, multi-consumer broadcasting
- `Clock` - Timing operations
- `Env` - Dependency injection

### Testing Framework

- **Framework**: ZIO Test + Kyo testing utilities
- **Base Class**: `KyoSpecDefault` for effect-aware testing
- **Test Structure**: Tests can directly use Kyo effects and work with async operations

### Key Patterns

- Effect signatures are explicitly typed in method signatures
- Services are defined as traits with dependency injection using `Layer`
- For-comprehensions are used for sequential effect composition
- Direct style programming avoids callback-based APIs

### JVM Configuration

The project is configured with 24GB max heap memory for large computations (see `.jvmopts`).

### Dependencies

- **Kyo Version**: 1.0-RC1 (core effects and combinators)
- **Scala Version**: 3.7.2
- **Testing**: ZIO Test framework with Kyo integration

### Code Style

The project uses Scalafmt with Scala 3 syntax support, strict compiler warnings, and Unicode arrow replacements for better readability.
