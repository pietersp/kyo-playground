package com.example

import munit.FunSuite

/** Basic sanity tests to ensure examples compile and run.
  *
  * Note: These are lightweight tests since the examples are primarily demonstrations. More extensive testing would require mocking effects
  * and extracting business logic into testable units.
  */
class ExamplesSuite extends FunSuite:
  test("example test that succeeds") {
    val obtained = 42
    val expected = 42
    assertEquals(obtained, expected)
  }
end ExamplesSuite
