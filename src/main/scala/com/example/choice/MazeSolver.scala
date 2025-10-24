package com.example.choice

import kyo.*

object MazeSolver extends KyoApp:

  private val maze = Chunk(
    "S ########",
    "#     ## #",
    "# ### ## #",
    "#        #",
    "# ### ## #",
    "# #   ## #",
    "# ### ## #",
    "# # #     ",
    "# # ##### ",
    "#        E"
  )

  case class Maze(rowStrings: Chunk[String], start: Position, end: Position)

  object Maze:
    def apply(rows: Chunk[String]): Maze < Abort[Exception] =
      def find(charToFind: Char): Maybe[Position] =
        Loop(0, 0)((row, col) =>
          if row >= rows.length then Loop.done(Absent)
          else if rows(row)(col) == charToFind then Loop.done(Present(row, col))
          else if col + 1 >= rows(row).length then Loop.continue(row + 1, 0)
          else Loop.continue(row, col + 1)
        ).eval
      end find

      Abort.recover[Absent](_ =>
        Abort.fail(new Exception("Invalid maze"))
      )(
        Abort.get(
          find('S').zip(find('E'))
            .map(Maze(rows, _, _))
        )
      )

    end apply
  end Maze

  extension (m: Maze)
    def rows: Int                       = m.rowStrings.length
    def cols: Int                       = if m.rowStrings.isEmpty then 0 else m.rowStrings.head.length
    def apply(pos: Position): Char      = m.rowStrings(pos.row)(pos.col)
    def isValid(pos: Position): Boolean = pos.row >= 0 && pos.row < rows && pos.col >= 0 && pos.col < cols
    def isWall(pos: Position): Boolean  = apply(pos) == '#'
    def renderSolution(path: Path): String =
      m.rowStrings.zipWithIndex.map { case (row, r) =>
        row.zipWithIndex.map { case (char, c) =>
          if char == ' ' && path.contains(Position(r, c)) then '.'
          else char
        }.mkString
      }.mkString("\n")
  end extension

  opaque type Position = (Int, Int)
  object Position:
    def apply(row: Int, col: Int): Position = (row, col)
  end Position

  extension (p: Position)
    def row: Int = p._1
    def col: Int = p._2
    def allMoves: List[Position] =
      List(
        Position(p.row - 1, p.col), // up
        Position(p.row + 1, p.col), // down
        Position(p.row, p.col - 1), // left
        Position(p.row, p.col + 1)  // right
      )
  end extension

  opaque type Path = Chunk[Position]
  object Path:
    val empty: Path = Chunk.empty

    def apply(pos: Position*): Path = Chunk.from(pos)
  end Path

  extension (p: Path)
    def findPath(maze: Maze, end: Position): Path < (Choice & Sync) =
      if p.last == end then p
      else
        val current = p.last
        val possibleMoves = current.allMoves.filter { next =>
          maze.isValid(next) && !maze.isWall(next) && !p.contains(next)
        }
        Choice.evalWith(possibleMoves): next =>
          (p.append(next)).findPath(maze, end)

    def contains(pos: Position): Boolean =
      p.contains(pos)
  end extension

  private def solveMaze(maze: Maze): Path < (Choice & Sync) =
    val start = maze.start
    val end   = maze.end
    Path(start).findPath(maze, end)
  end solveMaze

  run {
    for
      m <- Maze(maze)
      _ <- Choice.runStream(solveMaze(m))
        .map(m.renderSolution)
        .map("Solution:\n" + _ + "\n")
        .map(Console.printLine)
        .run
    yield ()
  }
end MazeSolver
