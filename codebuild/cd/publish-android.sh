#!/bin/bash

set -ex
set -o pipefail # Make sure one process in pipe fail gets bubble up

git submodule update --init
cd ./android

GPG_KEY=$(cat /tmp/aws-sdk-common-runtime.key.asc)
# Publish and release
# As May30th, 2025, the Sonatype OSSRH has been deprecated and replaced with Central Publisher and the new API does't support `findSonatypeStagingRepository`.
# the release will need to be invoked within the same call.
# https://github.com/gradle-nexus/publish-plugin/issues/379
../gradlew -PsigningKey=$"$GPG_KEY" -PsigningPassword=$MAVEN_GPG_PASSPHRASE -PsonatypeUsername=$ST_USERNAME -PsonatypePassword=$ST_PASSWORD publishToSonatype closeAndReleaseSonatypeStagingRepository
