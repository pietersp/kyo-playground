package com.example

import kyo.*
import munit.FunSuite

/** Tests for the LoopDemo Fibonacci implementation. */
class LoopDemoSuite extends FunSuite:

  test("fib(0) should return 0") {
    val result = LoopDemo.fib(0).eval
    assertEquals(result, BigInt(0))
  }

  test("fib(1) should return 1") {
    val result = LoopDemo.fib(1).eval
    assertEquals(result, BigInt(1))
  }

  test("fib(10) should return 55") {
    val result = LoopDemo.fib(10).eval
    assertEquals(result, BigInt(55))
  }

  test("fib(20) should return 6765") {
    val result = LoopDemo.fib(20).eval
    assertEquals(result, BigInt(6765))
  }

  test("fib handles large numbers") {
    val result = LoopDemo.fib(100).eval
    // Fib(100) = 354224848179261915075
    assertEquals(result, BigInt("354224848179261915075"))
  }
end LoopDemoSuite
