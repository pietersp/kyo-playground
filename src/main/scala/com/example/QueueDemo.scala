package com.example

import kyo.*

object QueueDemo extends KyoApp:

  case object Done

  run {
    for
      _        <- Console.printLine("Starting QueueDemo")
      q        <- Queue.init[Int | Done.type](16)
      producer <- Fiber.init(produce(q, 10))
      consumer <- Fiber.init(consume(q))
      _        <- producer.join
      _        <- consumer.join
      _        <- q.close
      _        <- Console.printLine("QueueDemo finished")
    yield ()
  }

  def produce(q: Queue[Int | Done.type], n: Int): Unit < (Async & Abort[Closed]) =
    def loop(i: Int): Unit < (Async & Abort[Closed]) =
      if i < n then
        for
          _ <- if i == 8 then Console.printLine("Hickup") *> Async.sleep(5.seconds)
          else Console.printLine(s"Producing $i")
          _ <- q.offer(i)
          _ <- Async.sleep(10.millis)
          _ <- loop(i + 1)
        yield ()
      else
        q.offer(Done).unit
    loop(0)
  end produce

  def consume(q: Queue[Int | Done.type]): Unit < (Async & Abort[Closed]) =
    def loop(): Unit < (Async & Abort[Closed]) =
      Abort.run(q.poll).map {
        case Result.Success(Present(v)) =>
          v match
            case _: Done.type =>
              Console.printLine("Received Done signal, consumer exiting.")
            case v: Int =>
              for
                _ <- Console.printLine(s"Consumed $v")
                _ <- Async.sleep(200.millis)
                _ <- loop()
              yield ()
        case Result.Success(_: Absent) =>
          Async.sleep(100.millis) *>
            Console.printLine("Queue is empty, waiting...") *>
            loop()
        case Result.Failure(_: Closed) =>
          Console.printLine("Queue is closed, consumer exiting.")
      }
    loop()
  end consume
end QueueDemo
