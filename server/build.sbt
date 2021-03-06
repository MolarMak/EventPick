val scalaSettings = Seq(
  version := "0.1",
  scalaVersion := "2.12.6",
  addCompilerPlugin(
    "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
  )
)

val circeV = "0.9.3"
val circeDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-java8"
).map(_ % circeV)

val akkaDeps = Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.2",
  "com.typesafe.akka" %% "akka-stream" % "2.5.12",
  "de.heikoseeberger" %% "akka-http-circe" % "1.21.0"
)

lazy val model =
  Project(id = "model", base = file("model"))
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= circeDeps
    )

lazy val dataTables =
  Project(id = "datatables", base = file("datatables"))
    .dependsOn(model)
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "com.typesafe.slick" %% "slick" % "3.2.3",
        "org.slf4j" % "slf4j-nop" % "1.6.4",
        "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",
        "org.postgresql" % "postgresql" % "42.2.2"
      )
    )

lazy val application =
  Project(id = "application", base = file("application"))
    .dependsOn(dataTables)
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= akkaDeps
    )

lazy val root =
  Project("eventpick", file("."))
    .aggregate(application)
