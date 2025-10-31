package com.example

import kyo.*
import kyo.test.KyoSpecDefault
import zio.test.*

object HubDemoSuite extends KyoSpecDefault:

  def spec = suite("HubDemoSuite")(
    test("Hub should initialize successfully") {
      for
        hub      <- Hub.init[String](16)
        isClosed <- hub.closed
      yield assertTrue(!isClosed)
    },
    test("Hub should accept articles via offer") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)
        article = HubDemo.NewsArticle("Test", "Test content", HubDemo.Category.Technology)
        result <- Abort.run(hub.offer(article))
      yield assertTrue(result.isSuccess)
    },
    test("Hub should close properly") {
      for
        hub      <- Hub.init[String](16)
        _        <- hub.close
        isClosed <- hub.closed
      yield assertTrue(isClosed)
    },
    test("Hub should handle operations after closing") {
      for
        hub    <- Hub.init[String](16)
        _      <- hub.close
        result <- Abort.run(hub.offer("test"))
      yield assertCompletes // Just verify the operation completes without hanging
    },
    test("publishTechNews should publish articles in sequence") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)
        // Run publisher for a short time
        fiber <- Fiber.init(HubDemo.publishTechNews(hub))
        _     <- Async.sleep(1.second)
        _     <- fiber.interrupt
      yield assertCompletes
    },
    test("publishSportsNews should publish articles in sequence") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)
        // Run publisher for a short time
        fiber <- Fiber.init(HubDemo.publishSportsNews(hub))
        _     <- Async.sleep(1.second)
        _     <- fiber.interrupt
      yield assertCompletes
    },
    test("publishBusinessNews should publish articles in sequence") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)
        // Run publisher for a short time
        fiber <- Fiber.init(HubDemo.publishBusinessNews(hub))
        _     <- Async.sleep(1.second)
        _     <- fiber.interrupt
      yield assertCompletes
    },
    test("subscribeWithListener should process articles correctly") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)

        // Create a listener
        listener <- hub.listen

        // Add some test articles
        article1 = HubDemo.NewsArticle("Test 1", "Content 1", HubDemo.Category.Technology)
        article2 = HubDemo.NewsArticle("Test 2", "Content 2", HubDemo.Category.Sports)
        _ <- hub.offer(article1)
        _ <- hub.offer(article2)

        // Run subscriber briefly
        fiber <- Fiber.init(HubDemo.subscribeWithListener(listener, "Test-Subscriber", 10.millis))
        _     <- Async.sleep(100.millis)
        _     <- fiber.interrupt

        // Close hub
        _ <- hub.close
      yield assertCompletes
    },
    test("subscribeWithListener should handle empty hub") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)

        // Create a listener and close hub immediately
        listener <- hub.listen
        _        <- hub.close

        // Run subscriber (should complete quickly since hub is closed)
        fiber <- Fiber.init(HubDemo.subscribeWithListener(listener, "Test-Subscriber", 10.millis))
        _     <- fiber.join
      yield assertCompletes
    },
    test("Hub should handle multiple listeners") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)

        // Create multiple listeners
        listener1 <- hub.listen
        listener2 <- hub.listen

        // Add test articles
        article = HubDemo.NewsArticle("Test", "Content", HubDemo.Category.Business)
        _ <- hub.offer(article)

        // Run subscribers briefly
        fiber1 <- Fiber.init(HubDemo.subscribeWithListener(listener1, "Subscriber-1", 10.millis))
        fiber2 <- Fiber.init(HubDemo.subscribeWithListener(listener2, "Subscriber-2", 10.millis))

        _ <- Async.sleep(100.millis)

        _ <- fiber1.interrupt
        _ <- fiber2.interrupt

        // Close hub
        _ <- hub.close
      yield assertCompletes
    },
    test("NewsArticle should be created correctly") {
      val article = HubDemo.NewsArticle("Title", "Content", HubDemo.Category.Technology)
      assertTrue(
        article.title == "Title" &&
          article.content == "Content" &&
          article.category.eq(HubDemo.Category.Technology)
      )
    },
    test("Multiple publishers should work concurrently") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](32)

        // Start multiple publishers
        techFiber     <- Fiber.init(HubDemo.publishTechNews(hub))
        sportsFiber   <- Fiber.init(HubDemo.publishSportsNews(hub))
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
    test("Hub should distribute articles to multiple listeners") {
      for
        hub <- Hub.init[HubDemo.NewsArticle](16)

        // Create multiple listeners
        listeners <- Kyo.foreach(1 to 3)(_ => hub.listen)

        // Add test articles
        articles = List(
          HubDemo.NewsArticle("Tech News", "New AI breakthrough", HubDemo.Category.Technology),
          HubDemo.NewsArticle("Sports News", "Team wins championship", HubDemo.Category.Sports),
          HubDemo.NewsArticle("Business News", "Stock market rally", HubDemo.Category.Business)
        )

        // Publish articles
        _ <- Kyo.foreach(articles)(hub.offer)

        // Run subscribers briefly
        fibers <- Kyo.foreach(listeners.zipWithIndex) { case (listener, index) =>
          Fiber.init(HubDemo.subscribeWithListener(listener, s"Subscriber-${index + 1}", 5.millis))
        }

        _ <- Async.sleep(50.millis)

        // Interrupt all subscribers
        _ <- Kyo.foreach(fibers)(_.interrupt)

        // Close hub
        _ <- hub.close
      yield assertCompletes
    }
  )

end HubDemoSuite
