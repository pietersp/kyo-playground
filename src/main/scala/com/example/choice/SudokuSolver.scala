package com.example.choice

import kyo.*

object SudokuSolver extends KyoApp:

  type Grid = Vector[Vector[Int]]

  def printGrid(grid: Grid): String =
    val rowSeparator = "├───────┼───────┼───────┤"
    val topBorder    = "┌───────┬───────┬───────┐"
    val bottomBorder = "└───────┴───────┴───────┘"

    val rows = for (i <- 0 until 9) yield
      val row   = for (j <- 0 until 9) yield " " + (if grid(i)(j) == 0 then "." else grid(i)(j).toString)
      val boxes = row.grouped(3).map(_.mkString("")).mkString(" │")
      "│" + boxes + " │"

    val gridWithSeparators = rows.grouped(3).map(_.mkString("\n")).mkString(s"\n$rowSeparator\n")

    s"$topBorder\n$gridWithSeparators\n$bottomBorder"
  end printGrid

  // Checks if a number can be placed in a given cell without violating Sudoku rules.
  def isValid(grid: Grid, row: Int, col: Int, num: Int): Boolean =
    !grid(row).contains(num) &&
      !grid.map(_(col)).contains(num) && {
        val startRow = row - row % 3
        val startCol = col - col % 3
        !(for
          r <- 0 to 2
          c <- 0 to 2
        yield grid(startRow + r)(startCol + c)).contains(num)
      }

  // Uses Kyo's Choice effect to perform a backtracking search for a valid solution.
  // For each empty cell, it tries all numbers from 1 to 9, dropping invalid choices.
  def solve(grid: Grid): Grid < Choice =
    val emptyCells =
      for
        r <- 0 to 8
        c <- 0 to 8
        if grid(r)(c) == 0
      yield (r, c)

    emptyCells.foldLeft(grid: Grid < Choice) { case (currentGrid, (row, col)) =>
      for
        g   <- currentGrid
        num <- Choice.evalSeq(1 to 9)
        _   <- Choice.dropIf(!isValid(g, row, col, num))
      yield g.updated(row, g(row).updated(col, num))
    }
  end solve

  val initialGrid: Grid = Vector(
    Vector(5, 3, 0, 0, 7, 0, 0, 0, 0),
    Vector(6, 0, 0, 1, 9, 5, 0, 0, 0),
    Vector(0, 9, 8, 0, 0, 0, 0, 6, 0),
    Vector(8, 0, 0, 0, 6, 0, 0, 0, 3),
    Vector(4, 0, 0, 8, 0, 3, 0, 0, 1),
    Vector(7, 0, 0, 0, 2, 0, 0, 0, 6),
    Vector(0, 6, 0, 0, 0, 0, 2, 8, 0),
    Vector(0, 0, 0, 4, 1, 9, 0, 0, 5),
    Vector(0, 0, 0, 0, 8, 0, 0, 7, 9)
  )

  run {
    for
      solution <- Choice.run(solve(initialGrid)).map(_.head)
      _        <- Console.printLine(printGrid(solution))
    yield ()
  }

end SudokuSolver
