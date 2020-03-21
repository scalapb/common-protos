#!/usr/bin/env bash
set -e
if [ -z "${TRAVIS_TAG}" ]; then 
    echo "Travis tag is unset - will do a snapshot";
    sbt ci-release
else 
    echo "Travis tag is set to ${TRAVIS_TAG}. Making a release";
    for tag in $(git tag --points-at HEAD);
      do CI_RELEASE=+${tag%%/*}/publishSigned sbt ci-release;
    done
fi
