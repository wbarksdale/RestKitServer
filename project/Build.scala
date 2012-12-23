import sbt._
import Keys._
import spray.revolver.RevolverPlugin._

object RestKitServerBuild extends Build {

   lazy val generalSettings = Seq(
      name          :=  "RestKitServer",
      version       :=  "0.1",
      scalacOptions  :=  Seq("-unchecked", "-deprecation", "-encoding", "utf8", "-Ydependent-method-types"))

   lazy val deps = Seq(
      "io.spray"                %   "spray-can"      % "1.0-M7",
      "io.spray"                %   "spray-routing"  % "1.0-M7",
      "io.spray"                %   "spray-testkit"  % "1.0-M7",
      "com.typesafe.akka"       %   "akka-actor"     % "2.0.4",
      "io.spray" %%  "spray-json" % "1.2.3" cross CrossVersion.full)

   lazy val resolutionRepos = Seq(
      "sonatype releases"  at "https://oss.sonatype.org/content/repositories/releases/",
      "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      "typesafe repo"      at "http://repo.typesafe.com/typesafe/releases/",
      "spray repo"         at "http://repo.spray.io/",
      Resolver.url("artifactory", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns))

    lazy val mojulo = Project(id = "mojulo", base = file("."))
      .settings(Revolver.settings: _*)
      .settings(generalSettings: _*)
      .settings(libraryDependencies ++= deps)
      .settings(resolvers ++= resolutionRepos)
}