package com.example

import kyo.*

object ClockDemo extends KyoApp:

  run {
    for
      sw <- Clock.stopwatch
      f  <- Fiber.init(Console.printLine("yo") *> Async.sleep(10.seconds) *> Console.printLine("ya"))
      f1 <- Fiber.init(Console.printLine("reps").repeatAtInterval(_ => 1.second, 100))
      _  <- Console.printLine("Hello")
      _  <- sw.elapsed.map(Console.printLine)
      _  <- f.join
      _  <- f1.interrupt
      t  <- sw.elapsed
    yield t.toSeconds
    end for
  }
end ClockDemo
