import sbt._
import Dependencies.allDependencies

name := "mentor-space"
version := "0.1"
scalaVersion := "2.13.1"

//resolvers += Resolver.jcenterRepo
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val domain = project
  .settings(libraryDependencies ++= allDependencies)

lazy val serialization = project
  .dependsOn(domain)

lazy val service = project
  .dependsOn(serialization)

lazy val api = project
  .dependsOn(service)

lazy val boot = project
  .dependsOn(api)

lazy val `mentor-space` = Project("root", file("."))
  .aggregate(domain, serialization, service, api, boot)
  .settings(moduleName := "mentor-space")
  .settings(name := "mentor-space")



