package com.example

import kyo.*

object QueueDemo extends KyoApp:

  run {
    for
      _        <- Console.printLine("Starting QueueDemo")
      q        <- Queue.init[Int](3)
      producer <- Fiber.init(produce(q, 10))
      consumer <- Fiber.init(consume(q))
      _        <- producer.join
      _        <- consumer.join
      _        <- Console.printLine("QueueDemo finished")
    yield ()
  }

  def produce(q: Queue[Int], n: Int): Unit < Async =
    def loop(i: Int): Unit < Async =
      if i < n then
        for
          _ <- Console.printLine(s"Producing $i")
          _ <- Abort.run(q.offer(i))
          _ <- Async.sleep(100.millis)
          _ <- loop(i + 1)
        yield ()
      else
        for
          _ <- Console.printLine("Producer finished, closing queue")
          _ <- q.close
        yield ()
    loop(0)
  end produce

  def consume(q: Queue[Int]): Unit < Async =
    def loop(): Unit < Async =
      (Abort.run(q.poll)).map {
        case Result.Success(Present(v)) =>
          for
            _ <- Console.printLine(s"Consumed $v")
            _ <- Async.sleep(200.millis)
            _ <- loop()
          yield ()
        case Result.Success(Absent) =>
          Async.sleep(100.millis).andThen(loop())
        case Result.Failure(_: Closed) =>
          Console.printLine("Consumer finished")
        case Result.Panic(e) =>
          Console.printLine(s"Consumer panicked with $e")
      }
    loop()
  end consume
end QueueDemo
