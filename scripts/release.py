"""Publish a validated library tag, or snapshots from master, using sbt-ci-release."""

import os
from pathlib import Path
import subprocess
import urllib.request


PROJECTS = {
    "proto-google-common-protos": "proto-google-common-protos",
    "proto-google-cloud-pubsub-v1": "proto-google-cloud-pubsub-v1",
    "proto-google-cloud-dataproc-v1": "proto-google-cloud-dataproc-v1",
    "pgv-proto": "pgv-java-stub",
}


def release_selection(ref, versions):
    if ref == "refs/heads/master":
        return None
    prefix = "refs/tags/"
    if not ref.startswith(prefix):
        raise ValueError("Publishing requires master or a library release tag")
    tag = ref[len(prefix):]
    artifact, separator, version = tag.partition("/")
    if not separator or artifact not in PROJECTS:
        raise ValueError(f"Unknown release tag: {tag}")
    if version != versions[artifact]:
        raise ValueError(f"Tag {tag} does not match build version {versions[artifact]}")
    return artifact


def common_dependency_urls(version):
    base = "https://repo.maven.apache.org/maven2/com/thesamet/scalapb/common-protos"
    return [
        f"{base}/{artifact}/{version}/{artifact}-{version}.pom"
        for line in ("0.11", "1.0")
        for scala in ("2.12", "2.13", "3")
        for artifact in (f"proto-google-common-protos-scalapb_{line}_{scala}",)
    ]


def main():
    os.chdir(Path(__file__).resolve().parent.parent)
    ref = os.environ.get("GITHUB_REF", "")
    if ref != "refs/heads/master" and not any(
        ref.startswith(f"refs/tags/{artifact}/") for artifact in PROJECTS
    ):
        raise ValueError("Publishing requires master or a known library release tag")
    for secret in ("PGP_SECRET", "SONATYPE_USERNAME", "SONATYPE_PASSWORD"):
        if not os.environ.get(secret):
            raise ValueError(f"Missing publishing credential: {secret}")
    env = os.environ.copy()
    env.pop("PUBLISH_ONLY", None)
    env.pop("RELEASE", None)
    if ref.startswith("refs/tags/"):
        env["RELEASE"] = "1"
    subprocess.run(["sbt", "writeConsumerConfig"], env=env, check=True)
    versions = dict(
        line.split("=", 1)
        for line in Path("target/consumer.properties").read_text().splitlines()
    )
    artifact = release_selection(ref, versions)
    if artifact:
        env["PUBLISH_ONLY"] = artifact
        env["RELEASE_TAG"] = ref[len("refs/tags/"):]
        if artifact in ("proto-google-cloud-pubsub-v1", "proto-google-cloud-dataproc-v1"):
            for url in common_dependency_urls(versions["proto-google-common-protos"]):
                with urllib.request.urlopen(url, timeout=30):
                    pass
        checks = [f"{PROJECTS[artifact]}{suffix}/validateRelease" for suffix in ("0_11", "1_0")]
        subprocess.run(["sbt", *checks, "ci-release"], env=env, check=True)
    else:
        subprocess.run(["sbt", "ci-release"], env=env, check=True)


if __name__ == "__main__":
    main()
