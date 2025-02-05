lazy val scalikejdbcVersion = "4.0.0"

// published dependency version
lazy val defaultPlayVersion = play.core.PlayVersion.current

// internal only
lazy val h2Version = "2.2.224"
lazy val postgresqlVersion = "42.6.0"

lazy val commonSettings = Seq(
  scalaVersion := "2.13.12",
  crossScalaVersions := Seq("2.12.18", "2.13.12", "3.3.1"),
  libraryDependencySchemes += "org.scala-lang.modules" %% "scala-parser-combinators" % "always",
  Test / fork := true,
  javaOptions ++= {
    if (scala.util.Properties.isWin) {
      Seq("-Dfile.encoding=Windows-31J")
    } else {
      Nil
    }
  },
  scalacOptions ++= Seq("-deprecation", "-unchecked")
)

commonSettings

lazy val scala3settings = Def.settings(
  conflictWarning := {
    if (scalaBinaryVersion.value == "3") {
      // TODO
      ConflictWarning("warn", Level.Warn, false)
    } else {
      conflictWarning.value
    }
  },
  libraryDependencies := {
    val organizations = Set(
      "com.typesafe.play",
      "org.specs2",
      "org.flywaydb",
    )
    libraryDependencies.value.map { x =>
      if (
        organizations(x.organization) && x.crossVersion
          .isInstanceOf[CrossVersion.Binary]
      ) {
        x cross CrossVersion.for3Use2_13
      } else {
        x
      }
    }
  },
)

lazy val baseSettings = commonSettings ++ Seq(
  organization := "org.scalikejdbc",
  version := "2.8.1-scalikejdbc-4.0-SNAPSHOT",
  publishTo := (
    if (isSnapshot.value)
      None
    else
      Some(Opts.resolver.sonatypeStaging)
  ),
  publishMavenStyle := true,
  libraryDependencies += "org.specs2" %% "specs2-core" % "4.20.2" % "test",
  Global / transitiveClassifiers := Seq(Artifact.SourceClassifier),
  Test / publishArtifact := false,
  pomIncludeRepository := { x => false },
  pomExtra := _pomExtra
)

// scalikejdbc-play-initializer
lazy val scalikejdbcPlayInitializer = Project(
  id = "play-initializer",
  base = file("scalikejdbc-play-initializer")
).settings(
  baseSettings,
  name := "scalikejdbc-play-initializer",
  libraryDependencies ++= Seq(
    "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion % "provided",
    "org.scalikejdbc" %% "scalikejdbc-config" % scalikejdbcVersion % "provided",
    "com.typesafe.play" %% "play" % defaultPlayVersion % "provided",
    // play-jdbc is needed to test with DBApi
    "com.typesafe.play" %% "play-jdbc" % defaultPlayVersion % "test",
    "com.typesafe.play" %% "play-test" % defaultPlayVersion % "test",
    "com.h2database" % "h2" % h2Version % "test",
    guice % "test"
  ),
  scala3settings,
)

// scalikejdbc-play-dbapi-adapter
lazy val scalikejdbcPlayDBApiAdapter = Project(
  id = "play-dbapi-adapter",
  base = file("scalikejdbc-play-dbapi-adapter")
).settings(
  baseSettings,
  name := "scalikejdbc-play-dbapi-adapter",
  libraryDependencies ++= Seq(
    "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion % "provided",
    "org.scalikejdbc" %% "scalikejdbc-config" % scalikejdbcVersion % "provided",
    "com.typesafe.play" %% "play" % defaultPlayVersion % "provided",
    "com.typesafe.play" %% "play-jdbc" % defaultPlayVersion % "compile",
    "com.typesafe.play" %% "play-test" % defaultPlayVersion % "test",
    "com.h2database" % "h2" % h2Version % "test",
    guice % "test"
  ),
  scala3settings,
)

// scalikejdbc-play-fixture
lazy val scalikejdbcPlayFixture = Project(
  id = "play-fixture",
  base = file("scalikejdbc-play-fixture")
).settings(
  baseSettings,
  name := "scalikejdbc-play-fixture",
  libraryDependencies ++= Seq(
    "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion % "provided",
    "org.scalikejdbc" %% "scalikejdbc-config" % scalikejdbcVersion % "provided",
    "com.typesafe.play" %% "play" % defaultPlayVersion % "provided",
    "com.typesafe.play" %% "play-test" % defaultPlayVersion % "test",
    "com.h2database" % "h2" % h2Version % "test"
  ),
  Test / testOptions += Tests
    .Argument(TestFrameworks.Specs2, "sequential", "true"),
  scala3settings,
).dependsOn(scalikejdbcPlayInitializer)

// play plugin zentasks example
lazy val scalikejdbcPlayInitializerTestZentasks = {
  val appName = "play-initializer-test-zentasks"

  val appDependencies = Seq(
    "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion,
    "org.scalikejdbc" %% "scalikejdbc-config" % scalikejdbcVersion,
    "org.flywaydb" %% "flyway-play" % "7.41.0",
    "com.h2database" % "h2" % h2Version,
    "org.postgresql" % "postgresql" % postgresqlVersion
  )

  Project(appName, file("scalikejdbc-play-initializer/test/zentasks"))
    .enablePlugins(play.sbt.PlayScala, PlayNettyServer)
    .disablePlugins(PlayAkkaHttpServer)
    .settings(
      commonSettings,
      libraryDependencies ++= appDependencies,
      scala3settings,
    )
    .dependsOn(scalikejdbcPlayInitializer, scalikejdbcPlayFixture)
}

// play dbapi adapter zentasks example
lazy val scalikejdbcPlayDBApiAdapterTestZentasks = {
  val appName = "play-dbapi-adapter-test-zentasks"

  val appDependencies = Seq(
    "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion,
    "org.scalikejdbc" %% "scalikejdbc-config" % scalikejdbcVersion,
    "com.h2database" % "h2" % h2Version,
    "org.postgresql" % "postgresql" % postgresqlVersion
  )

  Project(appName, file("scalikejdbc-play-dbapi-adapter/test/zentasks"))
    .enablePlugins(play.sbt.PlayScala, PlayNettyServer)
    .disablePlugins(PlayAkkaHttpServer)
    .settings(
      commonSettings,
      libraryDependencies ++= appDependencies,
      scala3settings,
    )
    .dependsOn(scalikejdbcPlayDBApiAdapter, scalikejdbcPlayFixture)
}

val jdbcDriverDependenciesInTestScope = Seq(
  "com.h2database" % "h2" % h2Version % "test",
  "org.apache.derby" % "derby" % "10.10.2.+" % "test",
  "org.xerial" % "sqlite-jdbc" % "3.7.+" % "test",
  "org.hsqldb" % "hsqldb" % "2.3.+" % "test",
  "mysql" % "mysql-connector-java" % "5.1.+" % "test",
  "org.postgresql" % "postgresql" % postgresqlVersion % "test"
)
val _pomExtra = <url>http://scalikejdbc.org/</url>
    <licenses>
      <license>
        <name>Apache License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:scalikejdbc/scalikejdbc-play-support.git</url>
      <connection>scm:git:git@github.com:scalikejdbc/scalikejdbc-play-support.git</connection>
    </scm>
    <developers>
      <developer>
        <id>seratch</id>
        <name>Kazuhiro Sera</name>
        <url>http://git.io/sera</url>
      </developer>
      <developer>
        <id>xuwei-k</id>
        <name>Kenji Yoshida</name>
        <url>https://github.com/xuwei-k</url>
      </developer>
      <developer>
        <id>tkawachi</id>
        <name>Takashi Kawachi</name>
        <url>https://github.com/tkawachi</url>
      </developer>
      <developer>
        <id>tototoshi</id>
        <name>Toshiyuki Takahashi</name>
        <url>https://github.com/tototoshi</url>
      </developer>
    </developers>
