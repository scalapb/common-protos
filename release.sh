#!/usr/bin/env bash
set -e
if [ -z "${TRAVIS_TAG}" ]; then 
    echo "Travis tag is unset - will do a snapshot";
    sbt ci-release
else 
    echo "Travis tag is set to ${TRAVIS_TAG}. Making a release";
    PUBLISH_ONLY=${TRAVIS_TAG%%/*} RELEASE=1 sbt ci-release;
fi
