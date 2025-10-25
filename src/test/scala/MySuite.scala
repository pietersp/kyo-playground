package com.example

import kyo.test.KyoSpecDefault
import zio.test.*

object MySuite extends KyoSpecDefault:
  def spec = suite("MySuite")(
    test("example test that succeeds") {
      val obtained = 42
      val expected = 42
      assertTrue(obtained == expected)
    }
  )
end MySuite
