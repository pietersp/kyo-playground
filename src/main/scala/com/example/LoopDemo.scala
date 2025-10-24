package com.example

import kyo.*

object LoopDemo extends KyoApp:

  def progressBar(n: Int) = Loop(1)(i =>
    val hx = "#" * i
    val sx = " " * (n - i)
    val a  = s"[$hx$sx]"
    Console.printLine(a).andThen(
      if i == n then Loop.done(())
      else Async.sleep(1000.millis).andThen(Loop.continue(i + 1))
    )
  )

  val a =
    for
      f1 <- Fiber.init(Async.sleep(10.seconds).andThen(Console.printLine("10s done")).andThen(20))
      f2 <- Fiber.init(Async.sleep(5.seconds).andThen(Console.printLine("5s done")).andThen(f1.interrupt).andThen(20))
      r  <- f1.block(20.seconds)
    yield r.getOrElse(42)

  def fib(n: Int): BigInt < Any =
    Loop((BigInt(0), BigInt(1), n)) {
      case (a, b, 0) => Loop.done(a)
      case (a, b, c) => Loop.continue((b, a + b, c - 1))
    }

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

  val fibStreamImpl: Chunk[String] < Async =
    Stream.range(0, 301)
      .mapPar(i => s"$i, ${fib(i)}")
      .run

  run {
    // fibStreamImpl
    //   .map(Kyo.foreach(_)(Console.printLine))
    //   .unit
    // progressBar(20)
    a.map(Console.printLine)
  }
end LoopDemo
