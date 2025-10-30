package com.example

import kyo.*

object QueueDemo extends KyoApp:

  run {
    for
      _        <- Console.printLine("Starting QueueDemo with multiple consumers")
      ch       <- Channel.init[Int](16)
      producer <- Fiber.init(produce(ch, 100))
      // Start multiple consumers - they can work concurrently
      consumer1 <- Fiber.init(consume(ch, "Consumer-1"))
      consumer2 <- Fiber.init(consume(ch, "Consumer-2"))
      consumer3 <- Fiber.init(consume(ch, "Consumer-3"))
      _         <- producer.join
      // Wait for all consumers to finish processing
      _ <- consumer1.join
      _ <- consumer2.join
      _ <- consumer3.join
      _ <- Console.printLine("QueueDemo finished")
    yield ()
  }

  def produce(ch: Channel[Int], n: Int): Unit < (Async & Abort[Closed]) =
    def loop(i: Int): Unit < (Async & Abort[Closed]) =
      if i < n then
        for
          _ <- if i == 8 then Console.printLine("Hickup") *> Async.sleep(5.seconds)
          else Console.printLine(s"Producing $i")
          _ <- ch.put(i) // Blocking put - waits if channel is full
          _ <- Async.sleep(10.millis)
          _ <- loop(i + 1)
        yield ()
      else
        // Close the channel and wait for it to be empty
        // This ensures all items are consumed before closing
        for
          _ <- Console.printLine("Producer done, waiting for channel to drain...")
          _ <- ch.closeAwaitEmpty
          _ <- Console.printLine("Channel drained and closed")
        yield ()
    loop(0)
  end produce

  def consume(ch: Channel[Int], name: String): Unit < Async =
    // Use streamUntilClosed which properly handles channel closure
    // and allows multiple consumers to work concurrently
    for
      _ <- Console.printLine(s"$name starting...")
      stream = ch.streamUntilClosed()
      _ <- stream.foreach { v =>
        for
          _ <- Console.printLine(s"$name consumed $v")
          _ <- Async.sleep(200.millis) // Simulate processing time
        yield ()
      }
      _ <- Console.printLine(s"$name: Channel stream ended, consumer exiting.")
    yield ()
  end consume
end QueueDemo
