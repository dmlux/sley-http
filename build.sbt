ThisBuild / scalaVersion := "3.9.0"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = project
  .in(file("."))
  .settings(
    name := "sley-http",
    libraryDependencies += "org.scalameta" %% "munit" % "1.3.6" % Test,
    scalacOptions ++= Seq(
      "-release:21",
      "-deprecation",
      "-feature",
      "-unchecked"
    )
  )
