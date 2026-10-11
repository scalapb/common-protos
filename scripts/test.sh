#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
scala_version=${1:?Usage: bash scripts/test.sh SCALA_VERSION}
sbt "++$scala_version" compile verifyArtifacts publishLocal writeConsumerConfig
for scalapb_line in 0.11 1.0; do
  (cd consumer && sbt "-Dscalapb.line=$scalapb_line" clean test)
done
