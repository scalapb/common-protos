# Common protos for ScalaPB

The goal of this project is to provide pre-compiled [ScalaPB](http://scalapb.github.io) classes for commonly-used third-party protobuf libraries.

## [Documentation](http://scalapb.github.io/common-protos.html)

## [List of all libraries and versions](https://repo1.maven.org/maven2/com/thesamet/scalapb/common-protos/).

## Adding a new libary
Not seeing a library you are looking for? It is easy to add more. Just send us a PR that adds your library to [build.sbt](https://github.com/scalapb/common-protos/blob/master/build.sbt) ([edit](https://github.com/scalapb/common-protos/edit/master/build.sbt)) and `README.md` in this project. Once your commit is merged, there is going to be a snapshot published to [Sonatype Snapshots](https://oss.sonatype.org/content/repositories/snapshots/com/thesamet/scalapb/common-protos/). You can use those snapshots by adding to your build.sbt:

```
ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")
```
