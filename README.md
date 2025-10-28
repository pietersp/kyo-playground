# Kyo Playground

A collection of example applications demonstrating various features and patterns of the [Kyo](https://getkyo.io) effect system for Scala 3.

## Overview

Kyo is a modern effect system for Scala 3 that provides composable, type-safe effects without requiring monad transformers. This playground showcases different Kyo capabilities through practical examples.

## Examples

### Core Concepts

#### FileAccess (`FileAccess.scala`)

Demonstrates file I/O operations using Kyo's effect system:

- **Effects Used:** `Sync`, `Abort[IOException]`, `Env`
- **Key Concepts:**
  - Defining effect-based service traits
  - Using `Layer` for dependency injection
  - Resource-safe file operations
  - Composing effects with for-comprehensions

#### FiberDemo (`FiberDemo.scala`)

Shows concurrent programming with Kyo fibers:

- **Effects Used:** `Fiber`, `Async`
- **Key Concepts:**
  - Creating and managing concurrent fibers
  - Fiber interruption and cancellation
  - Blocking and waiting for fiber results
  - Timeouts and deadlines

#### LoopDemo (`LoopDemo.scala`)

Illustrates looping constructs and stream processing:

- **Effects Used:** `Loop`, `Async`, `Stream`
- **Key Concepts:**
  - Tail-recursive loops with `Loop`
  - Progress indicators and animations
  - Computing Fibonacci numbers efficiently
  - Parallel stream processing with `Stream.mapPar`

#### ClockDemo (`ClockDemo.scala`)

Demonstrates timing and scheduling capabilities:

- **Effects Used:** `Clock`, `Fiber`, `Async`
- **Key Concepts:**
  - Using stopwatches for timing
  - Repeating actions at intervals
  - Coordinating timed concurrent operations

#### QueueDemo (`QueueDemo.scala`)

Demonstrates concurrent producer-consumer patterns using Kyo queues:

- **Effects Used:** `Queue`, `Async`, `Fiber`
- **Key Concepts:**
  - Creating bounded and unbounded queues
  - Concurrent `offer` and `take` operations
  - Building producer/consumer systems with fibers

### Choice Effect Examples

The `choice` package contains advanced examples using Kyo's `Choice` effect for non-deterministic computations and backtracking search algorithms.

#### ChoiceDemo (`choice/ChoiceDemo.scala`)

Basic introduction to the Choice effect:

- **Effects Used:** `Choice`, `Sync`
- **Key Concepts:**
  - Non-deterministic value selection
  - Filtering choices with `Choice.dropIf`
  - Collecting all possible results

#### MazeSolver (`choice/MazeSolver.scala`)

Implements a maze-solving algorithm using backtracking:

- **Effects Used:** `Choice`, `Sync`, `Var`, `Abort`
- **Key Concepts:**
  - Modeling domains with opaque types
  - State management with `Var` effect
  - Optimizing search with state (shortest path tracking)
  - Rendering solutions visually

#### SudokuSolver (`choice/SudokuSolver.scala`)

Solves Sudoku puzzles through constraint propagation and backtracking:

- **Effects Used:** `Choice`
- **Key Concepts:**
  - Constraint satisfaction problems
  - Systematic search through possibilities
  - Pruning invalid branches early
  - Pretty-printing grids with Unicode box drawing

#### NQueens (`choice/NQueens.scala`)

Solves the N-Queens chess problem:

- **Effects Used:** `Choice`
- **Key Concepts:**
  - Classic backtracking algorithm
  - Using `Choice.evalWith` for iteration
  - Streaming solutions with `Choice.runStream`
  - Board visualization

## Getting Started

### Prerequisites

- JDK 17 or later
- sbt 1.11.7 or later

### Building

```bash
sbt compile
```

### Running Examples

Each example can be run individually:

```bash
sbt "runMain com.example.FileAccessTest"
sbt "runMain com.example.FiberDemo"
sbt "runMain com.example.LoopDemo"
sbt "runMain com.example.ClockDemo"
sbt "runMain com.example.QueueDemo"
sbt "runMain com.example.choice.ChoiceDemo"
sbt "runMain com.example.choice.MazeSolver"
sbt "runMain com.example.choice.SudokuSolver"
sbt "runMain com.example.choice.NQueens"
```

### Testing

```bash
sbt test
```

### Code Formatting

This project uses Scalafmt for consistent code formatting:

```bash
sbt scalafmtAll
```

## Project Structure

```
src/main/scala/com/example/
├── FileAccess.scala      # File I/O with effects
├── FiberDemo.scala       # Concurrent programming
├── LoopDemo.scala        # Loops and streams  
├── ClockDemo.scala       # Timing operations
├── QueueDemo.scala       # Producer-consumer queues
└── choice/
    ├── ChoiceDemo.scala      # Choice effect basics
    ├── MazeSolver.scala      # Maze solving algorithm
    ├── SudokuSolver.scala    # Sudoku solver
    └── NQueens.scala         # N-Queens problem
```

## Learning Resources

- [Kyo Documentation](https://getkyo.io)
- [Kyo GitHub Repository](https://github.com/getkyo/kyo)
- [Scala 3 Documentation](https://docs.scala-lang.org/scala3/)

## License

This is a playground/example project for learning purposes.
