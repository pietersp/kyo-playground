package com.example.choice

import kyo.*
import kyo.test.KyoSpecDefault
import zio.test.*

/** Tests for the NQueens Board operations.
  *
  * Note: Board is an opaque type, so tests work with the public API only.
  */
object NQueensSuite extends KyoSpecDefault:
  import NQueens.Board
  import NQueens.Board.*

  def spec = suite("NQueensSuite")(
    test("empty board is safe for first queen") {
      assertTrue(Board.empty.safe(0, 0)) &&
      assertTrue(Board.empty.safe(0, 5))
    },
    test("can solve 4-queens problem") {
      for
        solutions <- Choice.run(Board.empty.queens(4))
      yield assertTrue(solutions.length == 2)
    },
    test("8-queens has 92 solutions") {
      for
        solutions <- Choice.run(Board.empty.queens(8))
      yield assertTrue(solutions.length == 92)
    }
  )
end NQueensSuite
