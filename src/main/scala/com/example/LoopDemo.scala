package com.example

import kyo.*

/** Demonstrates Loop construct and Stream processing in Kyo.
  *
  * Shows various looping patterns including:
  *   - Animated progress bar with Loop
  *   - Efficient Fibonacci calculation
  *   - Parallel stream processing
  */
object LoopDemo extends KyoApp:

  /** Creates an animated progress bar that fills over n iterations.
    *
    * @param n
    *   number of steps in the progress bar
    */
  def progressBar(n: Int) = Loop(1)(i =>
    val hx = "#" * i
    val sx = " " * (n - i)
    val a  = s"[$hx$sx]"
    Console.printLine(a).andThen(
      if i == n then Loop.done(())
      else Async.sleep(1000.millis).andThen(Loop.continue(i + 1))
    )
  )

  /** Computes the nth Fibonacci number efficiently using tail recursion via Loop.
    *
    * @param n
    *   the position in the Fibonacci sequence
    * @return
    *   the Fibonacci number at position n
    */
  def fib(n: Int): BigInt < Any =
    Loop((BigInt(0), BigInt(1), n)) {
      case (a, b, 0) => Loop.done(a)
      case (a, b, c) => Loop.continue((b, a + b, c - 1))
    }

  /** Computes and prints Fibonacci numbers 0-300 using async parallel collection. */
  val fibAsyncPrint: Unit < Async =
    for
      fibs <- Async.collectAll((0 to 300).map(fib))
      _ <- Console.printLine(
        fibs
          .zipWithIndex
          .map { case (n, i) => s"$i: $n" }
          .mkString("\n")
      )
    yield ()

  /** Alternative implementation using Stream with parallel mapping. */
  val fibStreamImpl: Chunk[String] < Async =
    Stream.range(0, 301)
      .mapPar(i => s"$i, ${fib(i)}")
      .run

  run {
    fibStreamImpl
      .map(Kyo.foreach(_)(Console.printLine))
      .unit
    // progressBar(20)

  }
end LoopDemo
