sonatypeProfileName := "com.thesame.scalapb"

publishMavenStyle := true

licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

// Where is the source code hosted: GitHub or GitLab?
import xerial.sbt.Sonatype._

sonatypeProjectHosting := Some(
  GitHubHosting("scalapb", "common-protos", "thesamet@gmail.com")
)

developers := List(
  Developer(
    id = "ScalaPB",
    name = "Nadav Samet",
    email = "thesamet@gmail.com",
    url = url("http://scalapb.github.io")
  )
)
