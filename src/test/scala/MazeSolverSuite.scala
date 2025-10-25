package com.example.choice

import com.example.choice.MazeSolver.Maze
import com.example.choice.MazeSolver.Position
import kyo.*
import kyo.test.KyoSpecDefault
import zio.test.*

/** Tests for the MazeSolver components. */
object MazeSolverSuite extends KyoSpecDefault:

  val simpleMaze = Chunk(
    "S  ",
    " # ",
    "  E"
  )

  def spec = suite("MazeSolverSuite")(
    test("Maze construction succeeds with valid input") {
      for
        result <- Abort.run[Maze.InvalidMaze](Maze(simpleMaze))
      yield assertTrue(result.isSuccess)
    },
    test("Maze construction fails without start") {
      val noStart = Chunk("   ", " # ", "  E")
      for
        result <- Abort.run[Maze.InvalidMaze](Maze(noStart))
      yield assertTrue(result.isFailure)
    },
    test("Maze construction fails without end") {
      val noEnd = Chunk("S  ", " # ", "   ")
      for
        result <- Abort.run[Maze.InvalidMaze](Maze(noEnd))
      yield assertTrue(result.isFailure)
    },
    test("Maze construction fails with invalid characters") {
      val invalid = Chunk("S  ", " X ", "  E")
      for
        result <- Abort.run[Maze.InvalidMaze](Maze(invalid))
      yield assertTrue(result.isFailure)
    },
    test("Maze construction fails with multiple starts") {
      val multiStart = Chunk("S  ", " S ", "  E")
      for
        result <- Abort.run[Maze.InvalidMaze](Maze(multiStart))
      yield assertTrue(result.isFailure)
    },
    test("Position allMoves returns 4 directions") {
      val pos   = Position(5, 5)
      val moves = pos.allMoves
      assertTrue(moves.length == 4) &&
      assertTrue(moves.contains(Position(4, 5))) && // up
      assertTrue(moves.contains(Position(6, 5))) && // down
      assertTrue(moves.contains(Position(5, 4))) && // left
      assertTrue(moves.contains(Position(5, 6)))    // right
    }
  )
end MazeSolverSuite
