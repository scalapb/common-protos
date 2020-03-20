ThisBuild / crossScalaVersions := Seq("2.12.10", "2.13.1")

javacOptions ++= List("-target", "8", "-source", "8")

organization := "com.thesamet.scalapb"

val grpcVersion = "1.28.0"

def settings(name: String, grpc: Boolean) = Seq(
      libraryDependencies ++= (if (grpc) Seq(
          "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
      ) else Nil),
      PB.protoSources in Compile += target.value / "protobuf_external",
      excludeFilter in PB.generate := new sbt.io.SimpleFileFilter({ f: File => 
          f.toPath.startsWith((target.value / "protobuf_external" / "google" / "protobuf").toPath)
      }),
      PB.targets in Compile := Seq(
        scalapb.gen(javaConversions=true) -> (sourceManaged in Compile).value / "protobuf"
      )
    )

lazy val `proto-google-common-protos` = 
    project.settings(
        settings("proto-google-common-protos", grpc=true),
        libraryDependencies ++= Seq(
          "com.google.api.grpc" % "proto-google-common-protos" % "1.17.0",
          "com.google.api.grpc" % "proto-google-common-protos" % "1.17.0" % "protobuf",
        )
    )

lazy val `grpc-google-cloud-pubsub-v1` = 
    project.settings(
        settings("grpc-google-cloud-pubsub-v1", grpc=true),
        libraryDependencies ++= Seq(
          "com.google.api.grpc" % "proto-google-cloud-pubsub-v1" % "1.85.1",
          "com.google.api.grpc" % "proto-google-cloud-pubsub-v1" % "1.85.1" % "protobuf",
        )
    )
