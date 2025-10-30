val scala3Version = "3.7.2"

val kyoVersion = "1.0-RC1"
val zioVersion = "2.1.17"

lazy val root = project
  .in(file("."))
  .settings(
    name         := "kyo-playground",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "dev.zio"   %% "zio-test-sbt"    % zioVersion % Test,
      "io.getkyo" %% "kyo-zio-test"    % kyoVersion % Test,
      "io.getkyo" %% "kyo-prelude"     % kyoVersion,
      "io.getkyo" %% "kyo-core"        % kyoVersion,
      "io.getkyo" %% "kyo-data"        % kyoVersion,
      "io.getkyo" %% "kyo-combinators" % kyoVersion
    ),
    scalacOptions ++= Seq(
      "-Wvalue-discard",
      "-Wnonunit-statement",
      "-Wconf:msg=(unused.*value|discarded.*value|pure.*statement):error",
      "-language:strictEquality"
    ),
    Test / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
