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
    protoPackage: String,
    buildNumber: Int = 0,
    packageName: Option[String] = None,
    dependencies: Seq[ProtosProject] = Seq.empty
) {
  import ProtosProject._
  def protoProject(scalapbVersion: String) = {
    val scalapbMajorMinor = scalapbVersion.split('.').take(2).mkString(".")

    val basePackageName = packageName.getOrElse(module.name)

    val projectName = module.name + scalapbMajorMinor.replace('.', '_')

    val optionsFile = protoPackage.replace('.', '/') + "/scalapb-options.proto"

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
        Compile / PB.targets := Seq(
          (
            PB.gens.plugin("scala"),
            Seq("java_conversions")
          ) -> (Compile / sourceManaged).value / "protobuf"
        ),
        createTags := createTagsImpl.value,
        publish / skip := (sys.env
          .getOrElse("PUBLISH_ONLY", basePackageName) != basePackageName),
        sonatypeBundleDirectory := (ThisBuild / baseDirectory).value / "target" / "sonatype-staging",
        Compile / resourceGenerators += Def.task {

          val packageOptionsFile =
            (Compile / resourceManaged).value / optionsFile
          IO.write(
            packageOptionsFile,
            s"""|// This file is generated by ScalaPB/common-protos project/BuildHelpers.scala.
                |// It is added as an input in user's builds to ensure the protos shipped with
                |// this jar are interpreted with the original generator options, and not influenced
                |// by generator options in the user's build.
                |
                |syntax = "proto3";
                |package $protoPackage;
                |
                |import "scalapb/scalapb.proto";
                |option (scalapb.options) = {
                |  scope: PACKAGE
                |  flat_package: false
                |  lenses: true
                |  // java_conversions as a file-level option is only introduced in 0.10.11
                |  // so it is not yet included here for backwards-compatiblity.
                |  // java_conversions: true
                |  preserve_unknown_fields: true
                |};
            """.stripMargin
          )
          Seq(packageOptionsFile)
        }.taskValue,
        Compile / packageBin / packageOptions += {
          Package.ManifestAttributes("ScalaPB-Options-Proto" -> optionsFile)
        }
      )
  }

  def scalapb09: Project =
    protoProject("0.9.8").dependsOn(
      dependencies.map(d => ClasspathDependency(d.scalapb09, None)): _*
    )

  val scalapb10: Project =
    protoProject("0.10.11").dependsOn(
      dependencies.map(d => ClasspathDependency(d.scalapb10, None)): _*
    )

  val scalapb11: Project =
    protoProject("0.11.5").dependsOn(
      dependencies.map(d => ClasspathDependency(d.scalapb11, None)): _*
    )

  def dependsOn(other: ProtosProject) =
    copy(dependencies = dependencies :+ other)
}

object ProtosProject {
  val Scala211 = "2.11.12"
  val Scala212 = "2.12.17"
  val Scala213 = "2.13.8"
  val Scala3 = "3.1.0"

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
