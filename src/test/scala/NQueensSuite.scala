package com.example.choice

import kyo.*
import munit.FunSuite

/** Tests for the NQueens Board operations.
  *
  * Note: Board is an opaque type, so tests work with the public API only.
  */
class NQueensSuite extends FunSuite:
  import NQueens.Board
  import NQueens.Board.*

  test("empty board is safe for first queen") {
    assertEquals(Board.empty.safe(0, 0), true)
    assertEquals(Board.empty.safe(0, 5), true)
  }

  test("can solve 4-queens problem") {
    val solutions = Choice.run(Board.empty.queens(4)).eval
    // 4-queens has 2 solutions
    assertEquals(solutions.length, 2)
  }

  test("each 4-queens solution has 4 queens") {
    val solutions = Choice.run(Board.empty.queens(4)).eval
    solutions.foreach { board =>
      // A Board is a Vector[Int] where each element is a queen position
      // For 4x4 board, we should have 4 queens (4 rows)
      val boardAsVector = board.asInstanceOf[Vector[Int]]
      assertEquals(boardAsVector.length, 4)
    }
  }
end NQueensSuite
