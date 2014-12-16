import sbt._
import sbt.Keys._

object RxScalazBuild extends Build {

  lazy val rxscalaz = Project(
    id = "rxscalaz",
    base = file("."),
    settings = Defaults.coreDefaultSettings ++ Seq(
      name := "rxscalaz",
      organization := "everpeace.github.io",
      version := "0.1-SNAPSHOT",

      scalaVersion := "2.11.2",
      crossScalaVersions := Seq("2.10.4", "2.11.2"),

      scalacOptions ++= Seq(
        "-deprecation",
        "-encoding", "UTF-8",
        "-feature",
        "-language:implicitConversions",
        "-language:higherKinds",
        "-language:existentials",
        "-language:postfixOps",
        "-unchecked"
      ),

      fork in test := true,
      javaOptions in test += "-Xmx8G",

      libraryDependencies := Seq(
        "org.scalaz" %% "scalaz-core" % "7.1.0" withSources(),
        "io.reactivex" %% "rxscala" % "0.23.0" withSources(),

        "org.specs2" %% "specs2" % "2.4" % "test" withSources(),
        "org.scalacheck" %% "scalacheck" % "1.11.4" % "test" withSources(),
        "org.typelevel" %% "scalaz-specs2" % "0.3.0" % "test" withSources(),
        "org.scalaz" %% "scalaz-scalacheck-binding" % "7.1.0" % "test" withSources()
      )
    )
  )
}
