#!/usr/bin/env bash

set -ex

cd $(dirname $0)/../..

git submodule update --init

# Pry the builder version this CRT is using out of ci.yml
BUILDER_VERSION=$(cat .github/workflows/ci.yml | grep 'BUILDER_VERSION:' | sed 's/\s*BUILDER_VERSION:\s*\(.*\)/\1/')
echo "Using builder version ${BUILDER_VERSION}"

# aws s3 cp s3://aws-crt-builder/releases/${BUILDER_VERSION}/builder.pyz ./builder
aws s3 cp s3://aws-crt-builder/channels/fips/builder.pyz ./builder
chmod a+x builder

GIT_TAG=$(git describe --tags)

./builder build -p aws-crt-java run_tests=false --cmake-extra=-DCRT_FIPS=ON

JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.382.b05-1.amzn2.0.2.aarch64 mvn -B package -DskipTests -Dshared-lib.skip=true -Dcrt.classifier=linux-aarch_64-fips

# Upload the lib to S3
aws s3 cp --recursive --include "*.so" target/cmake-build/aws-crt-java/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
aws s3 cp target/ s3://aws-crt-java-pipeline/${GIT_TAG}/jar/ --recursive --exclude "*" --include "aws-crt*.jar"
