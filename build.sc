import mill._
import mill.define.{Segment, Segments}
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}
import scalalib._

val devInfo = Developer("kag0", "Nathan Fischer", "https://github.com/kag0", Some("blackdoor"), Some("https://github.com/blackdoor"))

val `2.12` = "2.12.10"
val `2.13` = "2.13.2"

trait BaseModule extends CrossScalaModule {
  def scalacOptions = Seq("-Xfatal-warnings", "-feature", "-unchecked", "-deprecation")
  def publishVersion = T.input(T.ctx().env("PUBLISH_VERSION"))

  def artifactName = T(Segments(millModuleSegments.value.filterNot(_.isInstanceOf[Segment.Cross]):_*).parts.mkString("-"))
}

object jose extends Cross[JoseModule](`2.12`, `2.13`)
class JoseModule(val crossScalaVersion: String) extends BaseModule with PublishModule { root =>

  def ivyDeps = Agg(
    ivy"org.typelevel::cats-core:2.0.0",
    ivy"com.typesafe.scala-logging::scala-logging:3.9.2",
  )

  def pomSettings = PomSettings(
    description = "Extensible JOSE library for Scala.",
    organization = "black.door",
    url = "https://github.com/blackdoor/jose",
    licenses = List(License.Unlicense),
    versionControl = VersionControl.github("blackdoor", "jose"),
    developers = List(devInfo)
  )

  object test extends Tests {
    def moduleDeps = List(json.play(crossScalaVersion), json.circe(crossScalaVersion))
    def scalacOptions = T(super.scalacOptions().filterNot(Set("-Xfatal-warnings").contains))

    def ivyDeps = Agg(
      ivy"org.scalatest::scalatest:3.0.8",
      ivy"com.nimbusds:nimbus-jose-jwt:7.8"
    )

    def testFrameworks = List("org.scalatest.tools.Framework")
  }

  object json extends Module {

    object circe extends Cross[CirceModule](`2.12`, `2.13`)
    class CirceModule(val crossScalaVersion: String) extends BaseModule with PublishModule {

      lazy val circeVersion = "0.12.1"

      def moduleDeps = List(jose(crossScalaVersion))
      def ivyDeps = Agg(
        ivy"io.circe::circe-core:$circeVersion",
        ivy"io.circe::circe-generic:$circeVersion",
        ivy"io.circe::circe-parser:$circeVersion"
      )

      def pomSettings = root.pomSettings().copy(description = "Circe JSON support for blackdoor jose")
    }

    object play extends Cross[PlayModule](`2.12`, `2.13`)
    class PlayModule(val crossScalaVersion: String) extends BaseModule with PublishModule {

      def moduleDeps = List(jose(crossScalaVersion))
      def ivyDeps = Agg(ivy"com.typesafe.play::play-json:2.7.4")

      def pomSettings = root.pomSettings().copy(description = "Play JSON support for blackdoor jose")
    }
  }
}