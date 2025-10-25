package com.example.choice

import kyo.*
import kyo.test.KyoSpecDefault
import zio.test.*

/** Tests for the SudokuSolver validation logic. */
object SudokuSolverSuite extends KyoSpecDefault:

  def spec = suite("SudokuSolverSuite")(
    test("isValid rejects number already in row") {
      val grid = Vector.fill(9, 9)(0).updated(0, Vector(1, 2, 3, 4, 5, 6, 7, 8, 9))
      assertTrue(!SudokuSolver.isValid(grid, 0, 0, 1))
    },
    test("isValid rejects number already in column") {
      val grid = Vector.fill(9, 9)(0).updated(0, Vector.fill(9)(0).updated(0, 5))
      assertTrue(!SudokuSolver.isValid(grid.updated(1, grid(1).updated(0, 0)), 1, 0, 5))
    },
    test("isValid rejects number already in 3x3 box") {
      val grid = Vector.fill(9, 9)(0).updated(0, Vector.fill(9)(0).updated(0, 7))
      assertTrue(!SudokuSolver.isValid(grid, 1, 1, 7))
    },
    test("isValid accepts valid placement") {
      val grid = Vector.fill(9, 9)(0)
      assertTrue(SudokuSolver.isValid(grid, 0, 0, 1))
    },
    test("printGrid formats correctly") {
      val grid   = Vector.fill(9, 9)(0)
      val output = SudokuSolver.printGrid(grid)
      assertTrue(output.contains("┌")) &&
      assertTrue(output.contains("└")) &&
      assertTrue(output.contains("│"))
    }
  )
end SudokuSolverSuite
