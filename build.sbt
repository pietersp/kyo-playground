val scala3Version = "3.7.3"

val kyoVersion = "1.0-RC1"

lazy val root = project
  .in(file("."))
  .settings(
    name         := "kyo-playground",
    version      := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit"           % "1.0.0" % Test,
      "io.getkyo"     %% "kyo-prelude"     % kyoVersion,
      "io.getkyo"     %% "kyo-core"        % kyoVersion,
      "io.getkyo"     %% "kyo-data"        % kyoVersion,
      "io.getkyo"     %% "kyo-combinators" % kyoVersion
    ),
    scalacOptions ++= Seq(
      "-Wvalue-discard",
      "-Wnonunit-statement",
      "-Wconf:msg=(unused.*value|discarded.*value|pure.*statement):error",
      "-language:strictEquality"
    )
  )
