package com.example.choice

import kyo.*
import kyo.Ansi.*

/** Basic demonstration of the Choice effect.
  *
  * Shows how to:
  *   - Evaluate multiple choices with Choice.evalSeq
  *   - Filter choices with Choice.dropIf
  *   - Collect all valid combinations
  */
object ChoiceDemo extends KyoApp:

  val program =
    for
      a <- Choice.evalSeq(1 to 26)
      b <- Choice.evalSeq('a' to 'z')
      _ <- Sync.defer(42)
      r <- Choice.dropIf(a % 2 == 0 || b == 'a').map(_ => (a, b))
    yield (a, b, r)

  val d: Seq[(Int, Char, (Int, Char))] < Sync =
    Choice.run(program)

  run {
    d.map(Console.printLine(_))
  }

end ChoiceDemo
