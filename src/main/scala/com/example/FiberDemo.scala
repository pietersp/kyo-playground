package com.example

import kyo.*

object FiberDemo extends KyoApp:

  val a =
    for
      f1 <- Fiber.init(Async.sleep(10.seconds).andThen(Console.printLine("10s done")).andThen(20))
      f2 <- Fiber.init(Async.sleep(5.seconds).andThen(Console.printLine("5s done")).andThen(f1.interrupt).andThen(20))
      r  <- f1.block(20.seconds)
    yield r.getOrElse(42)

  run {
    a.map(Console.printLine)
  }
end FiberDemo
