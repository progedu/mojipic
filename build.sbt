name := """mojipic"""
organization := "jp.ed.nnn"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.7"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
libraryDependencies += ehcache
libraryDependencies += "org.pac4j" %% "play-pac4j" % "8.0.0"
libraryDependencies += "org.pac4j" % "pac4j-oauth" % "3.7.0"
libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += "org.scalikejdbc" %% "scalikejdbc"  % "3.0.2"
libraryDependencies += "org.scalikejdbc" %% "scalikejdbc-config" % "3.0.2"
libraryDependencies += "org.scalikejdbc" %% "scalikejdbc-play-dbapi-adapter" % "2.6.0-scalikejdbc-3.0"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.36"
libraryDependencies += "net.debasishg" %% "redisclient" % "3.4"
libraryDependencies += "org.im4java" % "im4java" % "1.4.0"
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.3.1"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "jp.ed.nnn.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "jp.ed.nnn.binders._"
