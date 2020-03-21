import sbt._
import Keys._
import sbtprotoc.ProtocPlugin.autoImport.PB
import scala.sys.process.{ProcessLogger, Process}

object NoProcessLogger extends ProcessLogger {
  def info(s: => String) = ()
  def out(s: => String) = ()
  def error(s: => String) = ()
  def err(s: => String) = ()
  def buffer[T](f: => T) = f
}

object BuildHelpers {
  val grpcVersion = "1.28.0"

  val scalapbVersion = "0.10"

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
        name := basePackageName.value + s"-scalapb_${scalapbVersion}",
        version := {
          if (isSnapshot) s"${module.revision}-SNAPSHOT"
          else s"${module.revision}-${buildNumber.value}"
        },
        versionTag := s"${basePackageName.value}/${module.revision}-${buildNumber.value}",
        libraryDependencies ++= (if (grpc)
                                   Seq(
                                     "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
                                   )
                                 else Nil),
        libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
        libraryDependencies += module,
        libraryDependencies += module % "protobuf-src" intransitive (),
        PB.targets in Compile := Seq(
          scalapb
            .gen(javaConversions = true) -> (sourceManaged in Compile).value / "protobuf"
        ),
        createTags := createTagsImpl.value
      )
}
