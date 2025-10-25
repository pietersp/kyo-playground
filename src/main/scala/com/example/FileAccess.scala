package com.example

import java.io.IOException
import java.nio.file.Path
import kyo.*

/** Service trait for file system operations using Kyo effects.
  *
  * Demonstrates how to define effectful service interfaces that can be provided via dependency injection using Kyo's `Env` and `Layer`
  * system.
  */
trait FileAccess:
  def read(path: Path): Array[Byte] < (Sync & Abort[IOException])

  def write(path: Path, bytes: Array[Byte]): Unit < (Sync & Abort[IOException])
end FileAccess

object FileAccess:
  /** Live implementation of FileAccess using actual file system operations.
    *
    * This Layer provides a concrete implementation that can be injected into programs requiring FileAccess capabilities.
    */
  val live: Layer[FileAccess, Any] = Layer {
    new FileAccess:

      override def write(path: Path, bytes: Array[Byte]): Unit < (Sync & Abort[IOException]) =
        Sync.defer(java.nio.file.Files.write(path, bytes)).unit

      override def read(path: Path): Array[Byte] < (Sync & Abort[IOException]) =
        Abort.catching[IOException] {
          Sync.defer(java.nio.file.Files.readAllBytes(path))
        }

  }
end FileAccess

/** Example program that reads the README.md file using FileAccess service. */
val program =
  Env.get[FileAccess]
    .map(_.read(Path.of("README.md")))
    .map(new String(_))

/** Copies a file from source to destination using FileAccess service.
  *
  * @param from
  *   source file path
  * @param to
  *   destination file path
  * @return
  *   unit effect with FileAccess dependency and potential IOException
  */
def copy(from: String, to: String) =
  for
    fa <- Env.get[FileAccess]
    bx <- fa.read(Path.of(from))
    _  <- fa.write(Path.of(to), bx)
  yield ()

/** Demonstrates file operations with proper error handling and dependency injection.
  *
  * Shows how to:
  *   - Use Env.runLayer to provide dependencies
  *   - Handle IOExceptions with Abort.run
  *   - Use Memo for caching/memoization
  */
object FileAccessTest extends KyoApp:
  run {
    Abort.run[IOException] {
      Memo.run(
        Env.runLayer(FileAccess.live)(copy("README.md", "README_copy.md"))
          .map(Console.printLine)
      )
    }
  }
end FileAccessTest
