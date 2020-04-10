import BuildHelpers._

Global / onChangedBuildSource := ReloadOnSourceChanges

javacOptions ++= List("-target", "8", "-source", "8")

sonatypeProfileName := "com.thesamet"

sonatypeBundleDirectory := (ThisBuild / baseDirectory).value / "target" / "sonatype-staging"

skip in publish := true

lazy val `proto-google-common-protos` = protoProject(
  "com.google.api.grpc" % "proto-google-common-protos" % "1.17.0",
  grpc = true
).settings(
  buildNumber := 0
)

lazy val `proto-google-cloud-pubsub-v1` = protoProject(
  "com.google.api.grpc" % "proto-google-cloud-pubsub-v1" % "1.86.1",
  grpc = true
).dependsOn(`proto-google-common-protos`)
  .settings(
    buildNumber := 0
  )
