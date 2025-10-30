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
      _         <- Console.printLine("Producer finished, waiting for channel to drain...")
      _         <- awaitChannelDrained(ch)
      _         <- Console.printLine("Channel drained, waiting for consumers...")
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
        Console.printLine("Producer done producing items")
    loop(0)
  end produce

  /** Wait for the channel to drain and then close it. This should be called after the producer is done and while consumers are still
    * running.
    */
  def awaitChannelDrained(ch: Channel[Int]): Unit < Async =
    for
      _ <- ch.closeAwaitEmpty
      _ <- Console.printLine("Channel drained and closed")
    yield ()
  end awaitChannelDrained

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
