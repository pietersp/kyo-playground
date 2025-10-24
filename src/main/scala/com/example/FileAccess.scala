package com.example
import java.io.IOException
import java.nio.file.Path
import kyo.*

trait FileAccess:
  def read(path: Path): Array[Byte] < (Sync & Abort[IOException])

  def write(path: Path, bytes: Array[Byte]): Unit < (Sync & Abort[IOException])
end FileAccess

object FileAccess:

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

val program =
  Env.get[FileAccess]
    .map(_.read(Path.of("README.md")))
    .map(new String(_))

def copy(from: String, to: String) =
  for
    fa <- Env.get[FileAccess]
    bx <- fa.read(Path.of(from))
    _  <- fa.write(Path.of(to), bx)
  yield ()

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
