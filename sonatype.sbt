sonatypeProfileName := "com.thesamet"

// Where is the source code hosted: GitHub or GitLab?
import xerial.sbt.Sonatype._

inThisBuild(
  List(
    organization := "com.thesamet.scalapb.common-protos",
    developers := List(
      Developer(
        id = "ScalaPB",
        name = "Nadav Samet",
        email = "thesamet@gmail.com",
        url = url("http://scalapb.github.io")
      )
    ),
    licenses := Seq(
      "APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")
    ),
    homepage := Some(url("https://github.com/scalapb/common-protos")),
    sonatypeProjectHosting := Some(
      GitHubHosting("scalapb", "common-protos", "thesamet@gmail.com")
    )
  )
)
