Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / scalaVersion := ProtosProject.Scala213
// Releases use library/version tags, rather than sbt-dynver's default v prefix.
ThisBuild / isSnapshot := ProtosProject.isSnapshot
// sonaRelease inspects the aggregate project's version string, not isSnapshot.
// The aggregate is never published; each library defines its own Maven version.
ThisBuild / version := (if (ProtosProject.isSnapshot) "0.0.0-SNAPSHOT"
                        else "0.0.0")
ThisBuild / crossScalaVersions := List(
  ProtosProject.Scala212,
  ProtosProject.Scala213,
  ProtosProject.Scala3
)
ThisBuild / javacOptions ++= List("--release", "8")

publish / skip := true

def commonProtos =
  ProtosProject(
    "com.google.api.grpc" % "proto-google-common-protos" % "2.78.0",
    grpc = true,
    protoPackage = "google",
    buildNumber = 1
  )
lazy val commonProtos11 = commonProtos.scalapb11
lazy val commonProtos1 = commonProtos.scalapb1

val cloudPubSub = ProtosProject(
  "com.google.api.grpc" % "proto-google-cloud-pubsub-v1" % "1.157.0",
  grpc = true,
  protoPackage = "google",
  buildNumber = 0
).dependsOn(commonProtos)
lazy val cloudPubSub11 = cloudPubSub.scalapb11
lazy val cloudPubSub1 = cloudPubSub.scalapb1

val cloudDataproc = ProtosProject(
  "com.google.api.grpc" % "proto-google-cloud-dataproc-v1" % "4.96.0",
  grpc = true,
  protoPackage = "google",
  buildNumber = 0
).dependsOn(commonProtos)
lazy val cloudDataproc11 = cloudDataproc.scalapb11
lazy val cloudDataproc1 = cloudDataproc.scalapb1

val pgvProto = ProtosProject(
  "build.buf.protoc-gen-validate" % "pgv-java-stub" % "1.3.3",
  grpc = false,
  protoPackage = "validate",
  packageName = Some("pgv-proto"),
  buildNumber = 1
)
lazy val pgvProto11 = pgvProto.scalapb11
lazy val pgvProto1 = pgvProto.scalapb1

val writeConsumerConfig =
  taskKey[Unit]("Writes versions for the independent consumer smoke tests")
writeConsumerConfig := {
  val versions = Seq(
    "scala" -> scalaVersion.value,
    "scalapb.0.11" -> ProtosProject.ScalaPB11,
    "scalapb.1.0" -> ProtosProject.ScalaPB1,
    "proto-google-common-protos" -> (commonProtos11 / version).value,
    "proto-google-cloud-pubsub-v1" -> (cloudPubSub11 / version).value,
    "proto-google-cloud-dataproc-v1" -> (cloudDataproc11 / version).value,
    "pgv-proto" -> (pgvProto11 / version).value
  )
  IO.write(
    target.value / "consumer.properties",
    versions.map { case (key, value) => s"$key=$value" }.mkString("\n")
  )
}
