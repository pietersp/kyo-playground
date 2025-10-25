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

  test("8-queens has 92 solutions") {
    val solutions = Choice.run(Board.empty.queens(8)).eval
    // 8-queens has 92 solutions
    assertEquals(solutions.length, 92)
  }
end NQueensSuite
