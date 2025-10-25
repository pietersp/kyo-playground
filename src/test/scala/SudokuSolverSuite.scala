package com.example.choice

import kyo.*
import munit.FunSuite

/** Tests for the SudokuSolver validation logic. */
class SudokuSolverSuite extends FunSuite:

  test("isValid rejects number already in row") {
    val grid = Vector.fill(9, 9)(0).updated(0, Vector(1, 2, 3, 4, 5, 6, 7, 8, 9))
    assertEquals(SudokuSolver.isValid(grid, 0, 0, 1), false)
  }

  test("isValid rejects number already in column") {
    val grid = Vector.fill(9, 9)(0).updated(0, Vector.fill(9)(0).updated(0, 5))
    assertEquals(SudokuSolver.isValid(grid.updated(1, grid(1).updated(0, 0)), 1, 0, 5), false)
  }

  test("isValid rejects number already in 3x3 box") {
    val grid = Vector.fill(9, 9)(0).updated(0, Vector.fill(9)(0).updated(0, 7))
    assertEquals(SudokuSolver.isValid(grid, 1, 1, 7), false)
  }

  test("isValid accepts valid placement") {
    val grid = Vector.fill(9, 9)(0)
    assertEquals(SudokuSolver.isValid(grid, 0, 0, 1), true)
  }

  test("printGrid formats correctly") {
    val grid   = Vector.fill(9, 9)(0)
    val output = SudokuSolver.printGrid(grid)
    assertEquals(output.contains("┌"), true)
    assertEquals(output.contains("└"), true)
    assertEquals(output.contains("│"), true)
  }
end SudokuSolverSuite
