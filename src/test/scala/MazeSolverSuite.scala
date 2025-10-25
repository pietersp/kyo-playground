package com.example.choice

import kyo.*
import munit.FunSuite

/** Tests for the MazeSolver components. */
class MazeSolverSuite extends FunSuite:
  import MazeSolver.*

  val simpleMaze = Chunk(
    "S  ",
    " # ",
    "  E"
  )

  test("Maze construction succeeds with valid input") {
    val result = Abort.run[Maze.InvalidMaze](Maze(simpleMaze)).eval
    result match
      case Result.Success(_) => assert(true)
      case Result.Failure(_) => fail("Expected success")
      case Result.Error(_)   => fail("Expected success")
    end match
  }

  test("Maze construction fails without start") {
    val noStart = Chunk("   ", " # ", "  E")
    val result  = Abort.run[Maze.InvalidMaze](Maze(noStart)).eval
    result match
      case Result.Failure(_) => assert(true)
      case Result.Success(_) => fail("Expected failure")
      case Result.Error(_)   => fail("Expected failure")
    end match
  }

  test("Maze construction fails without end") {
    val noEnd  = Chunk("S  ", " # ", "   ")
    val result = Abort.run[Maze.InvalidMaze](Maze(noEnd)).eval
    result match
      case Result.Failure(_) => assert(true)
      case Result.Success(_) => fail("Expected failure")
      case Result.Error(_)   => fail("Expected failure")
    end match
  }

  test("Maze construction fails with invalid characters") {
    val invalid = Chunk("S  ", " X ", "  E")
    val result  = Abort.run[Maze.InvalidMaze](Maze(invalid)).eval
    result match
      case Result.Failure(_) => assert(true)
      case Result.Success(_) => fail("Expected failure")
      case Result.Error(_)   => fail("Expected failure")
    end match
  }

  test("Maze construction fails with multiple starts") {
    val multiStart = Chunk("S  ", " S ", "  E")
    val result     = Abort.run[Maze.InvalidMaze](Maze(multiStart)).eval
    result match
      case Result.Failure(_) => assert(true)
      case Result.Success(_) => fail("Expected failure")
      case Result.Error(_)   => fail("Expected failure")
    end match
  }

  test("Position allMoves returns 4 directions") {
    val pos   = Position(5, 5)
    val moves = pos.allMoves
    assertEquals(moves.length, 4)
    assertEquals(moves.contains(Position(4, 5)), true) // up
    assertEquals(moves.contains(Position(6, 5)), true) // down
    assertEquals(moves.contains(Position(5, 4)), true) // left
    assertEquals(moves.contains(Position(5, 6)), true) // right
  }
end MazeSolverSuite
