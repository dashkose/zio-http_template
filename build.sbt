val tapirVersion = "1.0.6"
val zioVersion = "2.0.2"
val zioLoggingVersion = "2.1.0"

lazy val dockerSettings = Seq(
  name := "zio_http",
  dockerExposedPorts ++= Seq(sys.env.getOrElse("HTTP_PORT", "8080").toInt),
  Docker / packageName := name.value,
  Docker / version := version.value,
  dockerBaseImage := "adoptopenjdk/openjdk16",
  dockerAliases ++= Seq(dockerAlias.value.withTag(Option("latest")))
)

lazy val rootProject = (project in file("."))
  .settings(
    Seq(
      name := "zio_http",
      version := "0.1.0-SNAPSHOT",
      organization := "com.accenture",
      scalaVersion := "3.2.0",
      libraryDependencies ++= Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-prometheus-metrics" % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
        "com.softwaremill.sttp.tapir" %% "tapir-json-zio" % tapirVersion,
        "dev.zio" %% "zio" % zioVersion,
        "dev.zio" %% "zio-logging" % zioLoggingVersion,
        "ch.qos.logback" % "logback-classic" % "1.4.0",
        "dev.zio" %% "zio-logging-slf4j" % zioLoggingVersion,
        "dev.zio" %% "zio-config" % "3.0.2",
        "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
        "dev.zio" %% "zio-test" % zioVersion % Test,
        "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
        "com.softwaremill.sttp.client3" %% "zio-json" % "3.7.6" % Test
      ),
      testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
    ) ++ dockerSettings
  )
  .enablePlugins(JavaAppPackaging, DockerPlugin)

addCommandAlias("fmtAll", ";scalafmtSbt;scalafmtAll")
addCommandAlias("sanity", ";scalafmtCheckAll;test")
