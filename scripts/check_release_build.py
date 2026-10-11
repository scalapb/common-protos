"""Exercise the aggregate-version guard without signing or uploading artifacts."""

import os
from pathlib import Path
import subprocess


def main():
    os.chdir(Path(__file__).resolve().parent.parent)
    env = dict(os.environ, RELEASE="1", PUBLISH_ONLY="pgv-proto")
    subprocess.run(["sbt", "writeConsumerConfig"], env=env, check=True)
    versions = dict(
        line.split("=", 1)
        for line in Path("target/consumer.properties").read_text().splitlines()
    )
    env["RELEASE_TAG"] = f"pgv-proto/{versions['pgv-proto']}"
    subprocess.run(
        ["sbt", "pgv-java-stub0_11/validateRelease", "pgv-java-stub1_0/validateRelease"],
        env=env, check=True,
    )
    broken = subprocess.run(
        ["sbt", 'set version := "broken-SNAPSHOT"', "pgv-java-stub0_11/validateRelease"],
        env=env, text=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
    )
    expected = "sonaRelease requires a non-SNAPSHOT aggregate version"
    if broken.returncode == 0 or expected not in broken.stdout:
        raise RuntimeError(f"Aggregate snapshot regression was not caught:\n{broken.stdout}")
    print("Release validation accepts stable builds and rejects snapshot aggregates")


if __name__ == "__main__":
    main()
