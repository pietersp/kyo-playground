package com.example

import kyo.*
import kyo.Hub.*
import kyo.debug.*

/** Demonstrates Hub broadcasting functionality in Kyo.
  *
  * Shows how to:
  *   - Create a Hub for multi-producer, multi-consumer communication
  *   - Use hub.listen to get a Listener for receiving broadcasted messages
  *   - Publish messages to multiple listeners simultaneously
  *   - Handle different consumer processing speeds
  *   - Apply backpressure when buffers are full
  *   - Gracefully shutdown the broadcasting system
  */
object HubDemo extends KyoApp:

  enum Category:
    case Technology, Sports, Business
  end Category

  case class NewsArticle(title: String, content: String, category: Category)

  run {
    for
      _   <- Console.printLine("Starting HubDemo - News Broadcasting System")
      hub <- Hub.init[NewsArticle](1)

      // Start multiple news publishers
      techPublisher     <- Fiber.init(publishTechNews(hub))
      sportsPublisher   <- Fiber.init(publishSportsNews(hub))
      businessPublisher <- Fiber.init(publishBusinessNews(hub))

      // Create listeners for each subscriber using hub.listen
      alice   <- hub.listen(0, _.category.eq(Category.Technology))
      bob     <- hub.listen(0, _.category.eq(Category.Sports))
      charles <- hub.listen(0, _.category.eq(Category.Business))

      // Start multiple subscribers with different processing speeds
      aliceF   <- Fiber.init(subscribeWithListener(alice, "Alice", 500.millis))
      bobF     <- Fiber.init(subscribeWithListener(bob, "Bob", 800.millis))
      charlesF <- Fiber.init(subscribeWithListener(charles, "Charles", 1000.millis))

      // Let the system run for a while
      _ <- Async.sleep(4.seconds)

      // Stop all publishers
      _ <- Console.printLine("🟦 Stopping all publishers...")
      _ <- techPublisher.interrupt
      _ <- sportsPublisher.interrupt
      _ <- businessPublisher.interrupt

      // Wait for hub to finish processing and close it
      // Check for the hub to be empty before closing. If not wait longer
      // Wait for hub to be empty before closing
      _ <- waitUntilEmpty(hub.empty, "Hub")
      _ <- waitUntilEmpty(alice.empty, "Alice's listener")
      _ <- waitUntilEmpty(bob.empty, "Bob's listener")
      _ <- waitUntilEmpty(charles.empty, "Charles' listener")

      // _ <- waitForListenersToBeEmpty(alice, bob, charles)
      _ <- hub.close

      // Wait for all subscribers to complete (they will automatically complete when hub closes)
      _ <- aliceF.join
      _ <- bobF.join
      _ <- charlesF.join

      _ <- Console.printLine("🟦 HubDemo completed successfully")
    yield ()
  }

  def waitUntilEmpty[S](eff: Boolean < S, name: String): Unit < (Async & S) =
    eff
      .delay(10.millis)
      .repeatUntil(_ == true)
      .andThen(Console.printLine(s"🚪 $name is now closed."))
      .unit

  def waitForListenersToBeEmpty(listeners: Listener[NewsArticle]*): Unit < (Abort[Closed] & Async) =
    Console.printLine("🟦 Checking if listeners are empty...")
      .delay(100.millis)
      .repeatUntil(_ => Kyo.foreach(listeners)(_.empty).map(_.forall(_ == true)))
      .andThen(Console.printLine("🟦 All listeners are now empty."))
  end waitForListenersToBeEmpty

  def publish(
      hub: Hub[NewsArticle],
      articles: List[NewsArticle],
      publisherName: String,
      delay: Duration
  ): Unit < (Async & Abort[Closed]) =
    def publishLoop(index: Int): Unit < (Async & Abort[Closed]) =
      if index < articles.size then
        val article = articles(index)
        for
          _ <- Console.printLine(s"$publisherName: ${article.title}")
          _ <- Abort.run(hub.put(article))
          _ <- Async.sleep(delay)
          _ <- publishLoop(index + 1)
        yield ()
        end for
      else
        // Continue cycling through articles
        Async.sleep(delay * 2) *> publishLoop(0)

    publishLoop(0)
  end publish

  def publishTechNews(hub: Hub[NewsArticle]): Unit < (Async & Abort[Closed]) =
    val articles = List(
      NewsArticle("AI Breakthrough", "New AI model achieves human-level performance", Category.Technology),
      NewsArticle("Quantum Computing", "IBM achieves 1000-qubit milestone", Category.Technology),
      NewsArticle("Space Tech", "SpaceX launches satellite constellation", Category.Technology),
      NewsArticle("Cybersecurity", "New zero-day vulnerability discovered", Category.Technology),
      NewsArticle("Mobile Tech", "Latest smartphone features revolutionary camera", Category.Technology)
    )
    publish(hub, articles, "🖥️ Tech Publisher", 40.millis)
  end publishTechNews

  def publishSportsNews(hub: Hub[NewsArticle]): Unit < (Async & Abort[Closed]) =
    val articles = List(
      NewsArticle("Championship Finals", "Team wins in overtime thriller", Category.Sports),
      NewsArticle("Championship Finals", "Team wins in overtime thriller", Category.Sports),
      NewsArticle("Championship Finals", "Team wins in overtime thriller", Category.Sports),
      NewsArticle("Championship Finals", "Team wins in overtime thriller", Category.Sports)
    )

    publish(hub, articles, "⚽ Sports Publisher", 30.millis)
  end publishSportsNews

  def publishBusinessNews(hub: Hub[NewsArticle]): Unit < (Async & Abort[Closed]) =
    val articles = List(
      NewsArticle("Market Rally", "Stock market reaches all-time high", Category.Business),
      NewsArticle("Market Rally", "Stock market reaches all-time high", Category.Business),
      NewsArticle("Market Rally", "Stock market reaches all-time high", Category.Business),
      NewsArticle("Market Rally", "Stock market reaches all-time high", Category.Business),
      NewsArticle("Market Rally", "Stock market reaches all-time high", Category.Business)
    )

    publish(hub, articles, "🏦 Business Publisher", 50.millis)
  end publishBusinessNews

  def subscribeWithListener(
      listener: Listener[NewsArticle],
      name: String,
      processingTime: Duration
  ): Unit < (Abort[Closed] & Async) =
    import Result.*

    Console.printLine(s"🔔 $name started listening for news") *>
      Abort.run(listener.take)
        .map(_.foldOrThrow(
          onSuccess = a =>
            Console.printLine(s"  📖 $name processing: [${a.category}] ${a.title}") *>
              Async.sleep(processingTime) *> // Simulate processing time
              Console.printLine(s"  ✅ $name finished: ${a.title}") *>
              true,
          onFailure = _ => false
        ))
        .repeatUntil(_ == false)
        .unit

  end subscribeWithListener
end HubDemo
