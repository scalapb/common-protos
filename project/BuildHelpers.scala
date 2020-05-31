import sbt._
import Keys._
import sbtprotoc.ProtocPlugin.autoImport.PB
import scala.sys.process.{ProcessLogger, Process}
import xerial.sbt.Sonatype.autoImport.sonatypeBundleDirectory

object NoProcessLogger extends ProcessLogger {
  def info(s: => String) = ()
  def out(s: => String) = ()
  def error(s: => String) = ()
  def err(s: => String) = ()
  def buffer[T](f: => T) = f
}

object BuildHelpers {
  val Scala211 = "2.11.12"
  val Scala212 = "2.12.10"
  val Scala213 = "2.13.1"

  val grpcVersion = "1.28.0"

  val scalapbVersion = scalapb.compiler.Version.scalapbVersion

  val scalapbMajorMinor = scalapbVersion.split('.').take(2).mkString(".")

  val basePackageName = settingKey[String]("Base name for module")

  val buildNumber = settingKey[Int]("Build number for version")

  val versionTag = settingKey[String]("Version tag to use in git")

  val createTags = taskKey[Unit]("Creates git tags for all current builds")

  def isDirty(): Boolean = {
    false // Process(Seq("git", "status", "-s")).!!.nonEmpty
  }

  val isSnapshot: Boolean = sys.env.getOrElse("RELEASE", "").isEmpty()

  val createTagsImpl = Def.task {
    if (isDirty()) {
      throw new RuntimeException("Working tree is dirty. Not creating tags.")
    }
    val tag: String = versionTag.value
    val log = streams.value.log.info(_: String)
    val tagExists = (Process(Seq("git", "rev-parse", "-q", tag, "--"))
      .!(NoProcessLogger)) == 0
    if (!tagExists) {
      log(s"Creating tag $tag")
      Process(Seq("git", "tag", "-a", tag, "-m", tag)).!
    } else {
      log(s"Tag $tag exists. Skipping")
    }
  }

  def protoProject(module: ModuleID, grpc: Boolean) =
    sbt
      .Project(module.name, new File(module.name))
      .settings(
        basePackageName := module.name,
        name := basePackageName.value + s"-scalapb_${scalapbMajorMinor}",
        moduleName := name.value,
        version := {
          if (isSnapshot) s"${module.revision}-SNAPSHOT"
          else s"${module.revision}-${buildNumber.value}"
        },
        crossScalaVersions := (scalapbMajorMinor match {
          case "0.9"  => List(Scala213, Scala212, Scala211)
          case "0.10" => List(Scala213, Scala212)
        }),
        versionTag := s"${basePackageName.value}/${module.revision}-${buildNumber.value}",
        libraryDependencies ++= (if (grpc)
                                   Seq(
                                     "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion
                                   )
                                 else Nil),
        libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf",
        libraryDependencies += module,
        libraryDependencies += module % "protobuf-src" intransitive (),
        PB.targets in Compile := Seq(
          scalapb
            .gen(javaConversions =
              true
            ) -> (sourceManaged in Compile).value / "protobuf"
        ),
        createTags := createTagsImpl.value,
        publish / skip := (sys.env
          .getOrElse("PUBLISH_ONLY", module.name) != module.name),
        sonatypeBundleDirectory := (ThisBuild / baseDirectory).value / "target" / "sonatype-staging"
      )
}
