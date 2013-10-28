name := "site"

version := "0.0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "2.3.2",
  "org.webjars" % "angularjs" % "1.1.5-1",
  "org.webjars" % "angular-ui" % "0.4.0-1",
  "org.webjars" % "angular-ui-bootstrap" % "0.6.0-1"
)     

play.Project.playScalaSettings

