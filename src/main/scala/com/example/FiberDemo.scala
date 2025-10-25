package com.example

import kyo.*

/** Demonstrates concurrent fiber operations in Kyo.
  *
  * Shows how to:
  *   - Create and manage concurrent fibers
  *   - Interrupt fibers before completion
  *   - Block on fiber results with timeouts
  *   - Handle fiber cancellation
  */
object FiberDemo extends KyoApp:

  val a =
    for
      // Start a fiber that sleeps for 10 seconds then returns 20
      f1 <- Fiber.init(Async.sleep(10.seconds).andThen(Console.printLine("10s done")).andThen(20))
      // Start a fiber that sleeps 5 seconds, then interrupts f1
      f2 <- Fiber.init(Async.sleep(5.seconds).andThen(Console.printLine("5s done")).andThen(f1.interrupt).andThen(20))
      // Block waiting for f1 to complete (or be interrupted) with 20s timeout
      r <- f1.block(20.seconds)
    yield r.getOrElse(42) // Return 42 if f1 was interrupted

  run {
    a.map(Console.printLine)
  }
end FiberDemo
