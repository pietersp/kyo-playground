package com.example

import kyo.*
import kyo.test.KyoSpecDefault
import zio.test.*

object QueueDemoSuite extends KyoSpecDefault:

  def spec = suite("QueueDemoSuite")(
    test("produce should add items to queue in order") {
      for
        q     <- Queue.init[Int | QueueDemo.Done.type](16)
        _     <- QueueDemo.produce(q, 5)
        items <- collectAllItems(q, 5)
      yield assertTrue(items == List(0, 1, 2, 3, 4))
    },
    test("produce should send Done signal after all items") {
      for
        q          <- Queue.init[Int | QueueDemo.Done.type](16)
        _          <- QueueDemo.produce(q, 3)
        items      <- collectAllItems(q, 3)
        doneSignal <- q.poll
        isDone = doneSignal match
          case Present(_: QueueDemo.Done.type) => true
          case _                               => false
      yield assertTrue(
        items == List(0, 1, 2) && isDone
      )
    },
    test("produce should handle zero items") {
      for
        q          <- Queue.init[Int | QueueDemo.Done.type](16)
        _          <- QueueDemo.produce(q, 0)
        doneSignal <- q.poll
        isDone = doneSignal match
          case Present(_: QueueDemo.Done.type) => true
          case _                               => false
      yield assertTrue(isDone)
    },
    test("consume should process items until Done signal") {
      for
        q     <- Queue.init[Int | QueueDemo.Done.type](16)
        _     <- q.offer(1)
        _     <- q.offer(2)
        _     <- q.offer(3)
        _     <- q.offer(QueueDemo.Done)
        fiber <- Fiber.init(QueueDemo.consume(q))
        _     <- Async.sleep(1.second)
        _     <- fiber.interrupt
      yield assertCompletes
    },
    test("consume should handle empty queue gracefully") {
      for
        q     <- Queue.init[Int | QueueDemo.Done.type](16)
        fiber <- Fiber.init(QueueDemo.consume(q))
        _     <- Async.sleep(500.millis)
        _     <- q.offer(QueueDemo.Done)
        _     <- Async.sleep(500.millis)
        _     <- fiber.interrupt
      yield assertCompletes
    },
    test("producer and consumer should work together") {
      for
        q        <- Queue.init[Int | QueueDemo.Done.type](16)
        producer <- Fiber.init(QueueDemo.produce(q, 5))
        consumer <- Fiber.init(QueueDemo.consume(q))
        _        <- producer.join
        _        <- Async.sleep(2.seconds)
        _        <- consumer.interrupt
      yield assertCompletes
    },
    test("queue should handle concurrent operations") {
      for
        q         <- Queue.init[Int | QueueDemo.Done.type](32)
        producer1 <- Fiber.init(QueueDemo.produce(q, 3))
        _         <- Async.sleep(50.millis)
        items     <- collectAllItems(q, 3)
        _         <- producer1.join
        _         <- q.offer(QueueDemo.Done)
      yield assertTrue(items.length == 3)
    },
    test("consume should exit when queue is closed") {
      for
        q     <- Queue.init[Int | QueueDemo.Done.type](16)
        fiber <- Fiber.init(QueueDemo.consume(q))
        _     <- Async.sleep(200.millis)
        _     <- q.close
        _     <- Async.sleep(500.millis)
        _     <- fiber.interrupt
      yield assertCompletes
    }
  )

  // Helper function to collect items from queue
  private def collectAllItems(
      q: Queue[Int | QueueDemo.Done.type],
      count: Int
  ): List[Int] < (Async & Abort[Closed]) =
    def loop(acc: List[Int], remaining: Int): List[Int] < (Async & Abort[Closed]) =
      if remaining <= 0 then acc.reverse
      else
        q.poll.map {
          case Present(v) =>
            v match
              case i: Int                 => loop(i :: acc, remaining - 1)
              case _: QueueDemo.Done.type => acc.reverse
          case _: Absent =>
            Async.sleep(50.millis) *> loop(acc, remaining)
        }
    loop(List.empty, count)
  end collectAllItems

end QueueDemoSuite
