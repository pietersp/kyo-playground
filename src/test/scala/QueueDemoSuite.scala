package com.example

import kyo.*
import kyo.test.KyoSpecDefault
import zio.test.*

object QueueDemoSuite extends KyoSpecDefault:

  def spec = suite("QueueDemoSuite")(
    test("produce should add items to channel in order") {
      for
        ch    <- Channel.init[Int](16)
        _     <- QueueDemo.produce(ch, 5)
        items <- collectAllItems(ch, 5)
      yield assertTrue(items == List(0, 1, 2, 3, 4))
    },
    test("produce should produce all items without closing channel") {
      for
        ch       <- Channel.init[Int](16)
        _        <- QueueDemo.produce(ch, 3)
        items    <- collectAllItems(ch, 3)
        isClosed <- ch.closed
      yield assertTrue(
        items == List(0, 1, 2) && !isClosed // Producer no longer closes
      )
    },
    test("produce should handle zero items") {
      for
        ch       <- Channel.init[Int](16)
        _        <- QueueDemo.produce(ch, 0)
        isClosed <- ch.closed
      yield assertTrue(!isClosed) // Producer doesn't close channel
    },
    test("consume should process items until channel closes") {
      for
        ch    <- Channel.init[Int](16)
        _     <- ch.put(1)
        _     <- ch.put(2)
        _     <- ch.put(3)
        _     <- ch.close
        fiber <- Fiber.init(QueueDemo.consume(ch, "test-consumer"))
        _     <- fiber.join // Should complete when channel is closed
      yield assertCompletes
    },
    test("consume should block waiting for items") {
      for
        ch    <- Channel.init[Int](16)
        fiber <- Fiber.init(QueueDemo.consume(ch, "test-consumer"))
        _     <- Async.sleep(500.millis)
        _     <- ch.put(1)
        _     <- Async.sleep(300.millis)
        _     <- ch.close
        _     <- fiber.join
      yield assertCompletes
    },
    test("producer and consumer should work together with awaitChannelDrained") {
      for
        ch       <- Channel.init[Int](16)
        producer <- Fiber.init(QueueDemo.produce(ch, 5))
        consumer <- Fiber.init(QueueDemo.consume(ch, "test-consumer"))
        _        <- producer.join
        _        <- QueueDemo.awaitChannelDrained(ch)
        _        <- consumer.join
      yield assertCompletes
    },
    test("multiple consumers should share work from channel") {
      for
        ch        <- Channel.init[Int](32)
        producer  <- Fiber.init(QueueDemo.produce(ch, 10))
        consumer1 <- Fiber.init(QueueDemo.consume(ch, "consumer-1"))
        consumer2 <- Fiber.init(QueueDemo.consume(ch, "consumer-2"))
        _         <- producer.join
        _         <- QueueDemo.awaitChannelDrained(ch)
        _         <- consumer1.join
        _         <- consumer2.join
      yield assertCompletes
    },
    test("consume should exit when channel is closed") {
      for
        ch    <- Channel.init[Int](16)
        fiber <- Fiber.init(QueueDemo.consume(ch, "test-consumer"))
        _     <- Async.sleep(200.millis)
        _     <- ch.close
        _     <- fiber.join // Should complete gracefully
      yield assertCompletes
    },
    test("awaitChannelDrained should wait for all items to be consumed") {
      for
        ch       <- Channel.init[Int](16)
        _        <- ch.put(1)
        _        <- ch.put(2)
        _        <- ch.put(3)
        consumer <- Fiber.init(QueueDemo.consume(ch, "test-consumer"))
        _        <- QueueDemo.awaitChannelDrained(ch)
        _        <- consumer.join
      yield assertCompletes
    }
  )

  // Helper function to collect items from channel
  private def collectAllItems(
      ch: Channel[Int],
      count: Int
  ): List[Int] < (Async & Abort[Closed]) =
    def loop(acc: List[Int], remaining: Int): List[Int] < (Async & Abort[Closed]) =
      if remaining <= 0 then acc.reverse
      else
        Abort.run(ch.take).map {
          case kyo.Result.Success(v: Int)    => loop(v :: acc, remaining - 1)
          case kyo.Result.Failure(_: Closed) => acc.reverse
          case _                             => acc.reverse
        }
    loop(List.empty, count)
  end collectAllItems

end QueueDemoSuite
