val versions = {
  val properties = new java.util.Properties
  val input = new java.io.FileInputStream("../target/consumer.properties")
  try properties.load(input)
  finally input.close()
  properties
}

val scalapbLine = sys.props.getOrElse("scalapb.line", "0.11")
scalaVersion := versions.getProperty("scala")
publish / skip := true

libraryDependencies ++= Seq(
  "proto-google-common-protos",
  "proto-google-cloud-pubsub-v1",
  "proto-google-cloud-dataproc-v1",
  "pgv-proto"
).flatMap { artifact =>
  val dependency = "com.thesamet.scalapb.common-protos" %%
    s"$artifact-scalapb_$scalapbLine" % versions.getProperty(artifact)
  Seq(dependency, dependency % "protobuf")
}

libraryDependencies += "org.scalameta" %% "munit" % "1.2.0" % Test

Compile / PB.protocVersion := "3.25.8"
// Deliberately differ from the library's options. The packaged options must
// preserve the names and features of imported, already compiled messages.
Compile / PB.targets := Seq(
  scalapb.gen(flatPackage = true, lenses = false) -> (Compile / sourceManaged).value
)
