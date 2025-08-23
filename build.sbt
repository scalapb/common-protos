Global / onChangedBuildSource := ReloadOnSourceChanges

javacOptions ++= List("-target", "8", "-source", "8")

publish / skip := true

def commonProtos =
  ProtosProject(
    "com.google.api.grpc" % "proto-google-common-protos" % "2.61.0",
    grpc = true,
    protoPackage = "google",
    buildNumber = 0
  )
lazy val commonProtos10 = commonProtos.scalapb10
lazy val commonProtos11 = commonProtos.scalapb11
lazy val commonProtos1 = commonProtos.scalapb1

val cloudPubSub = ProtosProject(
  "com.google.api.grpc" % "proto-google-cloud-pubsub-v1" % "1.123.3",
  grpc = true,
  protoPackage = "google",
  buildNumber = 0
).dependsOn(commonProtos)
lazy val cloudPubSub10 = cloudPubSub.scalapb10
lazy val cloudPubSub11 = cloudPubSub.scalapb11
lazy val cloudPubSub1 = cloudPubSub.scalapb1

val pgvProto = ProtosProject(
  "build.buf.protoc-gen-validate" % "pgv-java-stub" % "0.10.1",
  grpc = false,
  protoPackage = "validate",
  packageName = Some("pgv-proto"),
  buildNumber = 0
)
lazy val pgvProto10 = pgvProto.scalapb10
lazy val pgvProto11 = pgvProto.scalapb11
lazy val pgvProto1 = pgvProto.scalapb1
