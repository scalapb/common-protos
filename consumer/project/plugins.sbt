addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.8")

val versions = {
  val properties = new java.util.Properties
  val input = new java.io.FileInputStream("../target/consumer.properties")
  try properties.load(input)
  finally input.close()
  properties
}

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" %
  versions.getProperty("scalapb." + sys.props.getOrElse("scalapb.line", "0.11"))
