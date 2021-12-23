import scalariform.formatter.preferences._

name := """try-auth0-playframework"""
organization := "com.github.ishikawawawa"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.7"

val scalikejdbcVersion = "3.5.0"

libraryDependencies ++= Seq(
  guice,
  ws,
  "mysql" % "mysql-connector-java" % "8.0.27",
  "org.scalikejdbc" %% "scalikejdbc" % scalikejdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-config" % scalikejdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-joda-time" % scalikejdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.8.0-scalikejdbc-3.5",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
)

// plugin settings

enablePlugins(ScalikejdbcPlugin)

scalariformPreferences := scalariformPreferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentConstructorArguments, true)
  .setPreference(DanglingCloseParenthesis, Preserve)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.github.ishikawawawa.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.github.ishikawawawa.binders._"
