package com.example.choice

import kyo.*
import kyo.debug.*

object MazeSolver extends KyoApp:

  private val maze = Chunk(
    "S         ",
    " ######## ",
    " ######## ",
    " ######## ",
    "    E     ",
    " ######## ",
    " ######## ",
    " ######## ",
    " ######## ",
    "          "
  )

  
  opaque type Maze = MazeImpl
  case class MazeImpl (rowStrings: Chunk[String], start: Position, end: Position)
  
  object Maze:
    enum InvalidMaze:
      case InvalidChars
      case StartAbsent
      case EndAbsent
      case MultipleStartsOrEnds

    private val validChars: Set[Char] = Set(' ', '#', 'S', 'E')
    def apply(rows: Chunk[String]): Maze < Abort[InvalidMaze] =

      import InvalidMaze.*
      def find(charToFind: 'S' | 'E'): Position < Abort[InvalidMaze] = 
        val pos = for 
          r <- 0 until rows.length
          c <- 0 until rows(r).length
          if rows(r)(c) == charToFind
        yield Position(r, c)
        
        pos.toList match 
          case Nil => charToFind match 
            case 'S' => Abort.fail(StartAbsent)
            case 'E' => Abort.fail(EndAbsent)
          case p :: Nil => p
          case _ => Abort.fail(MultipleStartsOrEnds)
      end find

      val allValidChars = rows.flatten.forall(validChars.contains)
      for 
        _ <- Abort.unless(allValidChars)(InvalidChars)
        s <- find('S')
        e <- find('E')
      yield MazeImpl(rows, s, e)
    end apply
  end Maze

  extension (m: Maze)
    def rows: Int                       = m.rowStrings.length
    def cols: Int                       = if m.rowStrings.isEmpty then 0 else m.rowStrings.head.length
    def apply(pos: Position): Char      = m.rowStrings(pos.row)(pos.col)
    def isValid(pos: Position): Boolean = pos.row >= 0 && pos.row < rows && pos.col >= 0 && pos.col < cols && !isWall(pos)
    def isValid(pos: Position, currentPath: Path): Boolean =
      isValid(pos) && !currentPath.contains(pos)
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
    def allMoves: Chunk[Position] =
      Chunk(
        Position(p.row - 1, p.col), // up
        Position(p.row + 1, p.col), // down
        Position(p.row, p.col - 1), // left
        Position(p.row, p.col + 1)  // right
      )
  end extension

  opaque type Path = Chunk[Position]
  object Path:
    def apply(position: Position): Path = Chunk(position)
  

  extension (p: Path)
    def findPath(maze: Maze, end: Position): Path < (Choice & Sync) =
      if p.last == end then p
      else
        Choice.evalWith(p.last.allMoves) { next =>
          Choice.dropIf(!maze.isValid(next, p)).andThen:
            (p.append(next)).findPath(maze, end)
        }

    def contains(pos: Position): Boolean = p.contains(pos)
  end extension

  private def solveMaze(maze: Maze): Path < (Choice & Sync) =
    val start = maze.start
    val end   = maze.end
    Path(start).findPath(maze, end)
  end solveMaze

  run {
    for
      m <- Maze(maze).recover(e => Abort.fail(new Exception(s"Maze creation failed: $e")))
      _ <- Choice.runStream(solveMaze(m))
        .map(m.renderSolution)
        .map("Solution:\n" + _ + "\n")
        .map(Console.printLine)
        .run
    yield ()
  }
end MazeSolver
