package com.example

import java.io.IOException
import kyo.*
import scala.concurrent.duration.*

object CombinatorDemo extends KyoApp:

  trait HelloService:
    def sayHelloTo(saluee: String): Unit < (Sync & Abort[Throwable])
  end HelloService

  object HelloService:
    val live = Layer(Live)

    object Live extends HelloService:
      override def sayHelloTo(saluee: String): Unit < (Sync & Abort[Throwable]) =
        Sync.defer:
          Abort.catching:
            println(s"Hello $saluee!")
    end Live
  end HelloService

  val keepTicking: Nothing < (Async & Emit[String]) =
    (Kyo.emit(".") *> Kyo.sleep(1.second)).forever

  val effect: Unit < (Async & Scope & Abort[Throwable] & Env[HelloService]) =
    for
      _           <- Console.printLine("Enter your name:")
      nameService <- Kyo.service[HelloService]
      _ <- keepTicking
        .foreachEmit(Console.print)
        .forkScoped
      saluee <- Console.readLine
      _      <- Kyo.sleep(2.seconds)
      _      <- nameService.sayHelloTo(saluee)
    yield ()
    end for
  end effect

  run {
    Scope.run:
      Memo.run:
        Abort.run:
          effect
            .recover(thr => Log.debug(s"Failed printing to console: ${thr}"))
            .provide(HelloService.live)
  }

end CombinatorDemo
