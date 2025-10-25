package com.example.choice

import kyo.*
import scala.math.abs

/** Solves the N-Queens chess problem using the Choice effect.
  *
  * Demonstrates:
  *   - Classic backtracking algorithm with Choice
  *   - Opaque types for safe domain modeling
  *   - Extension methods on opaque types
  *   - Streaming solutions with Choice.runStream
  *   - Board visualization with Unicode characters
  *
  * The N-Queens problem asks: How can N chess queens be placed on an NxN chessboard so that no two queens attack each other?
  */
object NQueens extends KyoApp:
  opaque type Board = Vector[Int]

  object Board:
    /** An empty board with no queens placed. */
    val empty: Board = Vector.empty

    extension (b: Board)
      /** Checks if a queen can be safely placed at the given position.
        *
        * A queen is safe if no other queen shares the same column or diagonal.
        */
      def safe(row: Int, col: Int): Boolean =
        (0 until row).forall: i =>
          b(i) != col && abs(i - row) != abs(b(i) - col)

      /** Recursively places queens on the board using backtracking.
        *
        * @param n
        *   board size (NxN)
        * @param row
        *   current row being processed
        * @return
        *   all valid board configurations
        */
      def queens(n: Int, row: Int = 0): Board < Choice =
        if row == n then b
        else
          Choice.evalWith(0 until n): col =>
            Choice.dropIf(!b.safe(row, col)).andThen:
              (b :+ col).queens(n, row + 1)

      /** Renders the board with Unicode box drawing characters. */
      def show: String =
        val n   = b.length
        val top = "╭" + ("─┬" * (n - 1)) + "─╮"
        val bot = "╰" + ("─┴" * (n - 1)) + "─╯"
        val rows = (0 until n).map { r =>
          b.indices.map(c => if b(r) == c then "Q" else "·").mkString("│", "│", "│")
        }
        (top +: rows :+ bot).mkString("\n")
      end show
    end extension
  end Board

  run {
    import Board.*
    Choice.runStream(Board.empty.queens(8))
      .tap(board => println(board.show))
      .fold(0)((count, _) => count + 1)
      .map(count => println(s"Total solutions: $count"))
      .eval
  }

end NQueens
