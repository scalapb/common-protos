# Common protos for ScalaPB

Precompiled ScalaPB messages and gRPC bindings for commonly used third-party protobuf libraries, published to Maven Central.

## Supported libraries

Each library is built for Scala **2.12.21**, **2.13.18**, and **3.3.8**, with separate artifacts for ScalaPB **0.11.21** (`scalapb_0.11`) and **1.0.0-alpha.6** (`scalapb_1.0`). The 1.0 line is experimental. These are JVM artifacts.

| Library | Artifact prefix | Version built on master |
| --- | --- | --- |
| Google common protos | `proto-google-common-protos` | `2.78.0-0` |
| Google Cloud Pub/Sub | `proto-google-cloud-pubsub-v1` | `1.157.0-0` |
| Google Cloud Dataproc | `proto-google-cloud-dataproc-v1` | `4.96.0-0` |
| protoc-gen-validate | `pgv-proto` | `1.3.3-0` |

Check [Maven Central](https://repo.maven.apache.org/maven2/com/thesamet/scalapb/common-protos/) for released versions. Master snapshots replace the `-0` suffix with `-SNAPSHOT`.

ScalaPB 0.10 artifacts remain available at their existing published versions. That line is frozen: current Google schemas use proto3 optional fields, which its generator cannot handle. Scala 3 consumers need 3.3.8 or later for the current artifacts.

PGV is maintained here for existing consumers. Its upstream replacement, [Protovalidate](https://github.com/bufbuild/protovalidate), uses different schemas and is not a drop-in replacement for these artifacts.

## Usage

Match the artifact's ScalaPB line to your generator and runtime. For example:

```scala
libraryDependencies ++= Seq(
  "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % "2.78.0-0",
  "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_0.11" % "2.78.0-0" % "protobuf"
)
```

The normal dependency provides compiled classes. The `protobuf` dependency provides imports and generator options when your own `.proto` files import these libraries. Omit it if you do not generate protos. Pub/Sub and Dataproc depend on the matching common-protos artifact.

Current Google artifacts bring in protobuf-java 4.33.6. Although ScalaPB 0.11 itself declares protobuf-java 3.25.8, this project's consumer matrix tests the resolved protobuf-java 4 runtime. Do not force protobuf-java 3 onto these Google Java classes. The ScalaPB 1.0 line resolves protobuf-java 4.35.0. Serialization and Java conversions are tested for both lines.

For snapshots, use sbt 1.11 or later:

```scala
resolvers += Resolver.sonatypeCentralSnapshots
```

See the [ScalaPB common-protos guide](https://scalapb.github.io/docs/common-protos/) for background. The version table above describes this repository's current build.

## Development and checks

Use JDK 17 and sbt (or `nix-shell`). The sbt launcher uses `project/build.properties`.
The root `.jvmopts` allows a 4 GB heap for generated API documentation and exits immediately on memory exhaustion.

```sh
sbt scalafmtSbtCheck
python3 -m unittest discover -s scripts -p 'test_*.py'
bash scripts/test.sh 2.12.21
bash scripts/test.sh 2.13.18
bash scripts/test.sh 3.3.8
```

Each test script compiles all libraries, verifies the JAR manifests and packaged ScalaPB options, publishes to local Ivy, and runs the independent `consumer/` build against each ScalaPB line. The consumer generates a proto importing all four libraries with different generator defaults, checks PGV descriptors, and tests wire serialization, Java conversions, and unknown fields. Run the scripts sequentially because they share the consumer build and version manifest.

GitHub Actions runs this matrix on PRs, merge groups, master, and release tags. The `ci-passed` job gates publishing and dependency auto-merges. Publishing credentials are used only in the publish job, after tests pass.

## Releases

Artifact versions have the form `<upstream-version>-<build-number>`. Increase `buildNumber` when regenerating an already released upstream version, including generator or packaging changes. Never reuse an existing Maven version or move an existing release tag.

1. Update the upstream dependencies together where their published POMs require it. Update generator and runtime versions together in `project/BuildHelpers.scala`.
2. Run the full test matrix, commit the changes, and merge to master. The successful master workflow publishes snapshots.
3. From a clean checkout of that commit, create and push the common-protos tag first:

   ```sh
   sbt 'proto-google-common-protos0_11/createTags'
   git push origin proto-google-common-protos/2.78.0-0
   ```

4. Wait for its workflow to finish and all six common-protos POMs to appear on Maven Central. Then create and push one remaining library tag at a time:

   ```sh
   sbt 'proto-google-cloud-pubsub-v10_11/createTags'
   git push origin proto-google-cloud-pubsub-v1/1.157.0-0
   sbt 'proto-google-cloud-dataproc-v10_11/createTags'
   git push origin proto-google-cloud-dataproc-v1/4.96.0-0
   sbt 'pgv-java-stub0_11/createTags'
   git push origin pgv-proto/1.3.3-0
   ```

A single library tag publishes both ScalaPB lines for all three Scala versions. `scripts/release.py` rejects unknown or mismatched tags, sets `PUBLISH_ONLY`, and verifies that common-protos is available before releasing Pub/Sub or Dataproc. Publishing is serialized; wait for each run before pushing the next tag. The plugin imports the signing key itself.

The ScalaPB organization's Actions secrets must supply `PGP_SECRET` (base64-encoded signing key), `PGP_PASSPHRASE`, and Central Portal user-token credentials in `SONATYPE_USERNAME` and `SONATYPE_PASSWORD`. Legacy OSSRH account credentials do not work with Central Portal.

## Adding a library

Add a `ProtosProject` in `build.sbt`, with both supported ScalaPB variants and any common-protos dependency. Add its versions to `writeConsumerConfig`, its artifact/project mapping to `scripts/release.py`, and its tag pattern to the workflow. Extend the independent consumer fixture and this README. Every included proto must have corresponding Java classes because these artifacts provide Java conversions.
