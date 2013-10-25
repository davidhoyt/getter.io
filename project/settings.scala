import sbt._

object Settings {
  val project            = "getter.io"

  val company            = "davidhoyt"

  val organization       = "org.github.davidhoyt"

  val homepage           = "https://github.com/davidhoyt/getter.io"

  val vcsSpecification   = "git@github.com:davidhoyt/getter.io.git"

  val licenses           = Seq(
    License(
      name  = "MIT License",
      url   = "http://www.opensource.org/licenses/mit-license.php"
    )
  )

  val developers         = Seq(
    Developer(
        id              = "David Hoyt"
      , name            = "David Hoyt"
      , email           = "dhoyt@hoytsoft.org"
      , url             = "http://www.hoytsoft.org/"
      , organization    = "HoytSoft"
      , organizationUri = "http://www.hoytsoft.org/"
      , roles           = Seq("architect", "developer")
    )
  )

  val scalaVersion       = "2.10.3"

  val scalacOptions      = Seq("-deprecation", "-unchecked", "-feature", "-Xelide-below", "900")
  val javacOptions       = Seq("-Xlint:unchecked")

  val prompt             = GitPrompt.build
}

