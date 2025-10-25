package com.example

import kyo.*
import kyo.test.KyoSpecDefault
import zio.test.*

/** Tests for the LoopDemo Fibonacci implementation. */
object LoopDemoSuite extends KyoSpecDefault:

  def spec = suite("LoopDemoSuite")(
    test("fib(0) should return 0") {
      for
        result <- LoopDemo.fib(0)
      yield assertTrue(result == BigInt(0))
    },
    test("fib(1) should return 1") {
      for
        result <- LoopDemo.fib(1)
      yield assertTrue(result == BigInt(1))
    },
    test("fib(10) should return 55") {
      for
        result <- LoopDemo.fib(10)
      yield assertTrue(result == BigInt(55))
    },
    test("fib(20) should return 6765") {
      for
        result <- LoopDemo.fib(20)
      yield assertTrue(result == BigInt(6765))
    },
    test("fib handles large numbers") {
      for
        result <- LoopDemo.fib(100)
      yield assertTrue(result == BigInt("354224848179261915075"))
    }
  )
end LoopDemoSuite
