package com.example

import kyo.*
import kyo.Hub.*

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

  case class NewsArticle(title: String, content: String, category: String)

  run {
    for
      _   <- Console.printLine("Starting HubDemo - News Broadcasting System")
      hub <- Hub.init[NewsArticle](16)

      // Start multiple news publishers
      techPublisher     <- Fiber.init(publishTechNews(hub))
      sportsPublisher   <- Fiber.init(publishSportsNews(hub))
      businessPublisher <- Fiber.init(publishBusinessNews(hub))

      // Create listeners for each subscriber using hub.listen
      techListener     <- hub.listen
      sportsListener   <- hub.listen
      businessListener <- hub.listen

      // Start multiple subscribers with different processing speeds
      techSubscriber     <- Fiber.init(subscribeWithListener(techListener, "Tech-Subscriber", 100.millis))
      sportsSubscriber   <- Fiber.init(subscribeWithListener(sportsListener, "Sports-Subscriber", 200.millis))
      businessSubscriber <- Fiber.init(subscribeWithListener(businessListener, "Business-Subscriber", 300.millis))

      // Let the system run for a while
      _ <- Async.sleep(4.seconds)

      // Stop all publishers
      _ <- techPublisher.interrupt
      _ <- sportsPublisher.interrupt
      _ <- businessPublisher.interrupt

      // Wait for hub to finish processing and close it
      _ <- hub.close
      _ <- Console.printLine("Hub closed, waiting for subscribers...")

      // Wait for all subscribers to complete (they will automatically complete when hub closes)
      _ <- techSubscriber.join
      _ <- sportsSubscriber.join
      _ <- businessSubscriber.join

      _ <- Console.printLine("HubDemo completed successfully")
    yield ()
  }

  def publishTechNews(hub: Hub[NewsArticle]): Unit < (Async & Abort[Closed]) =
    val articles = List(
      NewsArticle("AI Breakthrough", "New AI model achieves human-level performance", "technology"),
      NewsArticle("Quantum Computing", "IBM achieves 1000-qubit milestone", "technology"),
      NewsArticle("Space Tech", "SpaceX launches satellite constellation", "technology"),
      NewsArticle("Cybersecurity", "New zero-day vulnerability discovered", "technology"),
      NewsArticle("Mobile Tech", "Latest smartphone features revolutionary camera", "technology")
    )

    def publishLoop(index: Int): Unit < (Async & Abort[Closed]) =
      if index < articles.size then
        val article = articles(index)
        for
          _ <- Console.printLine(s"📰 Tech Publisher: ${article.title}")
          _ <- hub.offer(article)
          _ <- Async.sleep(400.millis)
          _ <- publishLoop(index + 1)
        yield ()
        end for
      else
        // Continue cycling through articles
        Async.sleep(800.millis) *> publishLoop(0)

    publishLoop(0)
  end publishTechNews

  def publishSportsNews(hub: Hub[NewsArticle]): Unit < (Async & Abort[Closed]) =
    val articles = List(
      NewsArticle("Championship Finals", "Team wins in overtime thriller", "sports"),
      NewsArticle("Olympic Records", "New world records set in swimming", "sports"),
      NewsArticle("Transfer News", "Star player signs multi-year deal", "sports"),
      NewsArticle("Tournament Update", "Underdog team advances to semifinals", "sports")
    )

    def publishLoop(index: Int): Unit < (Async & Abort[Closed]) =
      if index < articles.size then
        val article = articles(index)
        for
          _ <- Console.printLine(s"⚽ Sports Publisher: ${article.title}")
          _ <- hub.offer(article)
          _ <- Async.sleep(600.millis)
          _ <- publishLoop(index + 1)
        yield ()
        end for
      else
        Async.sleep(1000.millis) *> publishLoop(0)

    publishLoop(0)
  end publishSportsNews

  def publishBusinessNews(hub: Hub[NewsArticle]): Unit < (Async & Abort[Closed]) =
    val articles = List(
      NewsArticle("Market Rally", "Stock market reaches all-time high", "business"),
      NewsArticle("Tech IPO", "Tech company goes public with strong debut", "business"),
      NewsArticle("Economic Data", "GDP growth exceeds expectations", "business"),
      NewsArticle("Corporate Merger", "Major acquisition announced", "business"),
      NewsArticle("Startup Funding", "Startup raises Series B funding", "business")
    )

    def publishLoop(index: Int): Unit < (Async & Abort[Closed]) =
      if index < articles.size then
        val article = articles(index)
        for
          _ <- Console.printLine(s"💼 Business Publisher: ${article.title}")
          _ <- hub.offer(article)
          _ <- Async.sleep(500.millis)
          _ <- publishLoop(index + 1)
        yield ()
        end for
      else
        Async.sleep(900.millis) *> publishLoop(0)

    publishLoop(0)
  end publishBusinessNews

  def subscribeWithListener(listener: Listener[NewsArticle], name: String, processingTime: Duration): Unit < Async =
    for
      _ <- Console.printLine(s"🔔 $name started listening for news")
      // Use the listener to get a stream of articles
      stream = listener.stream()
      _ <- stream.foreach { article =>
        for
          _ <- Console.printLine(s"📖 $name processing: [${article.category}] ${article.title}")
          _ <- Async.sleep(processingTime) // Simulate processing time
          _ <- Console.printLine(s"✅ $name finished: ${article.title}")
        yield ()
      }
      _ <- Console.printLine(s"🔚 $name: News stream ended")
    yield ()
  end subscribeWithListener
end HubDemo
