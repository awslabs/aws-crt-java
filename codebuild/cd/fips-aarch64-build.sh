#!/usr/bin/env bash

set -ex

cd $(dirname $0)/../..

# git submodule update --init

# Pry the builder version this CRT is using out of ci.yml
BUILDER_VERSION=$(cat .github/workflows/ci.yml | grep 'BUILDER_VERSION:' | sed 's/\s*BUILDER_VERSION:\s*\(.*\)/\1/')
echo "Using builder version ${BUILDER_VERSION}"

# aws s3 cp s3://aws-crt-builder/releases/${BUILDER_VERSION}/builder.pyz ./builder
aws s3 cp s3://aws-crt-builder/channels/fips/builder.pyz ./builder
chmod a+x builder

GIT_TAG=$(git describe --tags)

./builder build -p aws-crt-java run_tests=false --target=linux-arm64 --cmake-extra=-DCRT_FIPS=ON
mv target/cmake-build/aws-crt-java/* target/cmake-build/

JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 mvn -B package -DskipTests -Dshared-lib.skip=true -Dcrt.classifier=linux-aarch_64-fips

# the FIPS subset contains FIPS build for linux-armv8 and linux-x64, and NON-FIPS build for rest of the linux platforms.
aws s3 cp --recursive --include "*.so" target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/fips_lib
