package com.example

import kyo.*
import kyo.test.KyoSpecDefault
import zio.test.*

object HubDemoSuite extends KyoSpecDefault:

  def spec = suite("HubDemoSuite")(
    test("Hub should initialize successfully") {
      for
        hub <- Hub.init[String](16)
        isClosed <- hub.closed
      yield assertTrue(!isClosed)
    },

    test("Hub should accept articles via offer") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)
        article = HubDemo.NewsArticle("Test", "Test content", "test")
        result <- Abort.run(hub.offer(article))
      yield assertTrue(result.isSuccess)
    },

    test("Hub should close properly") {
      for
        hub <- Hub.init[String](16)
        _ <- hub.close
        isClosed <- hub.closed
      yield assertTrue(isClosed)
    },

    test("Hub should refuse operations after closing") {
      for
        hub <- Hub.init[String](16)
        _ <- hub.close
        result <- Abort.run(hub.offer("test"))
      yield assertTrue(result.fold(_ => true, _ => false, _ => false))
    },

    test("publishTechNews should publish articles in sequence") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)
        // Run publisher for a short time
        fiber <- Fiber.init(HubDemo.publishTechNews(hub))
        _ <- Async.sleep(1.second)
        _ <- fiber.interrupt
      yield assertCompletes
    },

    test("publishSportsNews should publish articles in sequence") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)
        // Run publisher for a short time
        fiber <- Fiber.init(HubDemo.publishSportsNews(hub))
        _ <- Async.sleep(1.second)
        _ <- fiber.interrupt
      yield assertCompletes
    },

    test("publishBusinessNews should publish articles in sequence") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)
        // Run publisher for a short time
        fiber <- Fiber.init(HubDemo.publishBusinessNews(hub))
        _ <- Async.sleep(1.second)
        _ <- fiber.interrupt
      yield assertCompletes
    },

    test("subscribeToChannel should process articles correctly") {
      for
        channel <- Channel.init[HubDemo.NewsArticle](16)
        // Add some test articles
        article1 = HubDemo.NewsArticle("Test 1", "Content 1", "test")
        article2 = HubDemo.NewsArticle("Test 2", "Content 2", "test")
        _ <- channel.offer(article1)
        _ <- channel.offer(article2)
        _ <- channel.close

        // Run subscriber
        fiber <- Fiber.init(HubDemo.subscribeToChannel(channel, "Test-Subscriber", 10.millis))
        _ <- fiber.join
      yield assertCompletes
    },

    test("subscribeToChannel should handle empty channel") {
      for
        channel <- Channel.init[HubDemo.NewsArticle](16)
        _ <- channel.close

        // Run subscriber
        fiber <- Fiber.init(HubDemo.subscribeToChannel(channel, "Test-Subscriber", 10.millis))
        _ <- fiber.join
      yield assertCompletes
    },

    test("broadcastDistributor should handle multiple channels") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)
        channel1 <- Channel.init[HubDemo.NewsArticle](16)
        channel2 <- Channel.init[HubDemo.NewsArticle](16)

        // Run broadcaster briefly
        fiber <- Fiber.init(HubDemo.broadcastDistributor(hub, List(channel1, channel2)))
        _ <- Async.sleep(200.millis)
        _ <- fiber.interrupt

        // Close channels
        _ <- channel1.close
        _ <- channel2.close
      yield assertCompletes
    },

    test("NewsArticle should be created correctly") {
      val article = HubDemo.NewsArticle("Title", "Content", "Category")
      assertTrue(
        article.title == "Title" &&
        article.content == "Content" &&
        article.category == "Category"
      )
    },

    test("Multiple publishers should work concurrently") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](32)

        // Start multiple publishers
        techFiber <- Fiber.init(HubDemo.publishTechNews(hub))
        sportsFiber <- Fiber.init(HubDemo.publishSportsNews(hub))
        businessFiber <- Fiber.init(HubDemo.publishBusinessNews(hub))

        // Let them run for a bit
        _ <- Async.sleep(1.second)

        // Stop all publishers
        _ <- techFiber.interrupt
        _ <- sportsFiber.interrupt
        _ <- businessFiber.interrupt

        // Close hub
        _ <- hub.close
      yield assertCompletes
    },

    test("Hub should handle rapid publishing") {
      for
        hub <- Hub.init[String](16)

        // Publish many items rapidly
        _ <- Kyo.foreach(1 to 100) { i =>
          hub.offer(s"item-$i")
        }

        // Close hub
        _ <- hub.close
      yield assertCompletes
    },

    test("subscribeToChannel should filter system messages") {
      for
        channel <- Channel.init[HubDemo.NewsArticle](16)

        // Add system message and regular message
        systemMsg = HubDemo.NewsArticle("System", "Heartbeat", "system")
        regularMsg = HubDemo.NewsArticle("Regular", "Regular content", "news")

        _ <- channel.offer(systemMsg)
        _ <- channel.offer(regularMsg)
        _ <- channel.close

        // Run subscriber
        fiber <- Fiber.init(HubDemo.subscribeToChannel(channel, "Test-Subscriber", 10.millis))
        _ <- fiber.join
      yield assertCompletes
    }
  )

  
end HubDemoSuite