Global / onChangedBuildSource := ReloadOnSourceChanges

javacOptions ++= List("-target", "8", "-source", "8")

sonatypeProfileName := "com.thesamet"

sonatypeBundleDirectory := (ThisBuild / baseDirectory).value / "target" / "sonatype-staging"

publish / skip := true

/* Skipping common protos due to https://github.com/googleapis/api-common-protos/issues/83
def commonProtos =
  ProtosProject(
    "com.google.api.grpc" % "proto-google-common-protos" % "1.18.1",
    grpc = true,
    protoPackage = "google",
    buildNumber = 2
  )
lazy val commonProtos09 = commonProtos.scalapb09
lazy val commonProtos10 = commonProtos.scalapb10
lazy val commonProtos11 = commonProtos.scalapb11

val cloudPubSub = ProtosProject(
  "com.google.api.grpc" % "proto-google-cloud-pubsub-v1" % "1.93.4",
  grpc = true,
  protoPackage = "google",
  buildNumber = 0
).dependsOn(commonProtos)
lazy val cloudPubSub09 = cloudPubSub.scalapb09
lazy val cloudPubSub10 = cloudPubSub.scalapb10
lazy val cloudPubSub11 = cloudPubSub.scalapb11
 */

val pgvProto = ProtosProject(
  "io.envoyproxy.protoc-gen-validate" % "pgv-java-stub" % "0.6.1",
  grpc = false,
  protoPackage = "validate",
  packageName = Some("pgv-proto"),
  buildNumber = 6
)
lazy val pgvProto09 = pgvProto.scalapb09
lazy val pgvProto10 = pgvProto.scalapb10
lazy val pgvProto11 = pgvProto.scalapb11
