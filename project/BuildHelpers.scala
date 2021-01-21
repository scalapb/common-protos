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

final case class ProtosProject(
    module: ModuleID,
    grpc: Boolean,
    buildNumber: Int = 0,
    packageName: Option[String] = None,
    dependencies: Seq[ProtosProject] = Seq.empty
) {
  import ProtosProject._
  def protoProject(scalapbVersion: String) = {
    val scalapbMajorMinor = scalapbVersion.split('.').take(2).mkString(".")

    val basePackageName = packageName.getOrElse(module.name)

    val projectName = module.name + scalapbMajorMinor.replace('.', '_')

    sbt
      .Project(projectName, new File(projectName))
      .settings(
        name := s"$basePackageName-scalapb_${scalapbMajorMinor}",
        moduleName := name.value,
        version := {
          if (isSnapshot) s"${module.revision}-SNAPSHOT"
          else s"${module.revision}-${buildNumber}"
        },
        crossScalaVersions := (scalapbMajorMinor match {
          case "0.9"  => List(Scala213, Scala212, Scala211)
          case "0.10" => List(Scala213, Scala212)
          case "0.11" => List(Scala3, Scala213, Scala212)
        }),
        versionTag := s"${basePackageName}/${module.revision}-${buildNumber}",
        libraryDependencies ++= (if (grpc)
                                   Seq(
                                     "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion
                                   )
                                 else Nil),
        libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf",
        libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion,
        libraryDependencies += module,
        libraryDependencies += module % "protobuf-src" intransitive (),
        libraryDependencies += "com.thesamet.scalapb" % "protoc-gen-scala" % scalapbVersion % "protobuf" artifacts (Artifact(
          "protoc-gen-scala",
          PB.ProtocPlugin,
          "sh",
          "unix"
        )),
        PB.targets in Compile := Seq(
          (
            PB.gens.plugin("scala"),
            Seq("java_conversions")
          ) -> (sourceManaged in Compile).value / "protobuf"
        ),
        createTags := createTagsImpl.value,
        publish / skip := (sys.env
          .getOrElse("PUBLISH_ONLY", basePackageName) != basePackageName),
        sonatypeBundleDirectory := (ThisBuild / baseDirectory).value / "target" / "sonatype-staging",
        publishArtifact in (Compile, packageDoc) := scalaVersion.value != Scala3
      )
  }

  def scalapb09: Project =
    protoProject("0.9.8").dependsOn(
      dependencies.map(d => ClasspathDependency(d.scalapb09, None)): _*
    )

  val scalapb10: Project =
    protoProject("0.10.10").dependsOn(
      dependencies.map(d => ClasspathDependency(d.scalapb10, None)): _*
    )

  val scalapb11: Project =
    protoProject("0.11.0-M3").dependsOn(
      dependencies.map(d => ClasspathDependency(d.scalapb11, None)): _*
    )

  def dependsOn(other: ProtosProject) =
    copy(dependencies = dependencies :+ other)
}

object ProtosProject {
  val Scala211 = "2.11.12"
  val Scala212 = "2.12.10"
  val Scala213 = "2.13.2"
  val Scala3 = "0.27.0-RC1"

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
}
