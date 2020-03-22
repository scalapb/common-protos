# Common protos for ScalaPB

The goal of this project is to provide pre-compiled [ScalaPB](http://scalapb.github.io) classes for commonly-used third-party protobuf libraries.

We currently have:

- `proto-google-common-protos`
- `proto-google-cloud-pubsub-v1`

## Using

We cross-build for ScalaPB 0.9.x and ScalaPB 0.10.x and for all Scala versions supported by these ScalaPB versions. The ScalaPB major and minor versions are appended to the artifact name. The maven is the same as the upstream maven version of the proto library, followed by a `-` and a build number.

For example, for ScalaPB 0.9.x:

```
libraryDependencies += "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.9" % "1.17.0-0"
```

See [list of all libraries and versions](https://repo1.maven.org/maven2/com/thesamet/scalapb/common-protos/).

## Adding a new libary
Not seeing a library you are looking for? It is easy to add more. Just send us a PR that adds your library to [build.sbt](https://github.com/scalapb/common-protos/blob/master/build.sbt) ([edit](https://github.com/scalapb/common-protos/edit/master/build.sbt)) and `README.md` in this project. Once your commit is merged, there is going to be a snapshot published to [Sonatype Snapshots](https://oss.sonatype.org/content/repositories/snapshots/com/thesamet/scalapb/common-protos/). You can use those snapshots by adding to your build.sbt:

```
ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")
```
