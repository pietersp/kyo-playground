package com.example.choice

import kyo.*

/** State tracking for the maze search optimization. */
enum SearchState:
  case NoLimit
  case NotYetFound
  case Shortest(value: Int)
end SearchState

/** Demonstrates backtracking maze solving using the Choice effect.
  *
  * Features:
  *   - Opaque types for type-safe domain modeling (Maze, Position, Path)
  *   - Validated maze construction with error handling
  *   - Optimized search that tracks shortest path found
  *   - Visual solution rendering
  *
  * The solver explores all possible paths through the maze but prunes paths that exceed the current shortest solution.
  */
object MazeSolver extends KyoApp:

  private val maze = Chunk(
    "S         ",
    " ## ##### ",
    " ## ##    ",
    " ##    ## ",
    " ## ## ## ",
    " ## ##    ",
    " ## ##### ",
    " #     ## ",
    " # ### ## ",
    "   #  E   "
  )

  private val empty = Chunk(
    "S         ",
    "          ",
    "          ",
    "          ",
    "          ",
    "          ",
    "          ",
    "          ",
    "          ",
    "         E"
  )

  opaque type Maze = MazeImpl

  /** Internal representation of a maze with start and end positions. */
  case class MazeImpl(rowStrings: Chunk[String], start: Position, end: Position)

  object Maze:
    /** Validation errors for maze construction. */
    enum InvalidMaze:
      case InvalidChars
      case StartAbsent
      case EndAbsent
      case MultipleStartsOrEnds
    end InvalidMaze

    private val validChars: Set[Char] = Set(' ', '#', 'S', 'E')

    /** Constructs a validated Maze from string rows.
      *
      * @param rows
      *   the maze layout as strings
      * @return
      *   a Maze or validation errors
      */
    def apply(rows: Chunk[String]): Maze < Abort[InvalidMaze] =

      import InvalidMaze.*

      /** Finds the unique position of 'S' or 'E' in the maze. */
      def find(charToFind: 'S' | 'E'): Position < Abort[InvalidMaze] =
        val pos =
          for
            r <- 0 until rows.length
            c <- 0 until rows(r).length
            if rows(r)(c) == charToFind
          yield Position(r, c)

        pos.toList match
          case Nil => charToFind match
              case 'S' => Abort.fail(StartAbsent)
              case 'E' => Abort.fail(EndAbsent)
          case p :: Nil => p
          case _        => Abort.fail(MultipleStartsOrEnds)
        end match
      end find

      val allValidChars = rows.flatten.forall(validChars.contains)
      for
        _ <- Abort.unless(allValidChars)(InvalidChars)
        s <- find('S')
        e <- find('E')
      yield MazeImpl(rows, s, e)
      end for
    end apply
  end Maze

  extension (m: Maze)
    def rows: Int                       = m.rowStrings.length
    def cols: Int                       = if m.rowStrings.isEmpty then 0 else m.rowStrings.head.length
    def apply(pos: Position): Char      = m.rowStrings(pos.row)(pos.col)
    def isValid(pos: Position): Boolean = pos.row >= 0 && pos.row < rows && pos.col >= 0 && pos.col < cols && !isWall(pos)
    def isValid(pos: Position, currentPath: Path): Boolean =
      isValid(pos) && !currentPath.contains(pos)
    def isWall(pos: Position): Boolean = apply(pos) == '#'
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
  end Path

  extension (p: Path)
    def findPath(maze: Maze, end: Position): Path < (Choice & Sync & Var[SearchState]) =
      import SearchState.*
      if p.last == end then
        Var.update[SearchState] {
          case _: NotYetFound.type                   => Shortest(p.length)
          case Shortest(length) if p.length < length => Shortest(p.length)
          case other                                 => other
        }.andThen(p)
      else
        Var.get[SearchState].map {
          case Shortest(shortest) if p.length >= shortest => Choice.drop
          case _ =>
            Choice.evalWith(p.last.allMoves) { next =>
              Choice.dropIf(!maze.isValid(next, p)).andThen:
                (p.append(next)).findPath(maze, end)
            }
        }
      end if
    end findPath

    def contains(pos: Position): Boolean = p.contains(pos)
  end extension

  /** Solves the maze using backtracking with optimization.
    *
    * Uses Var[SearchState] to track the shortest path found so far, allowing early pruning of longer paths.
    */
  private def solveMaze(maze: Maze): Path < (Choice & Sync & Var[SearchState]) =
    val start = maze.start
    val end   = maze.end
    Path(start).findPath(maze, end)
  end solveMaze

  run {
    for
      m <- Maze(empty).mapAbort(e => new Exception(s"Maze creation failed: $e"))
      _ <- Var.run(SearchState.NotYetFound) {
        Choice.runStream(solveMaze(m))
          .map(m.renderSolution)
          .map("Solution:\n" + _ + "\n")
          .map(Console.printLine)
          .run
      }
    yield ()
  }
end MazeSolver
