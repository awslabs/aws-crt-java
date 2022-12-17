#!/usr/bin/env bash

set -ex

cd `dirname $0`/../..

git submodule update --init

AWS_CRT_HOST=`uname | tr '[:upper:]' '[:lower:]'`-`uname -m`

if [ -z "$AWS_CRT_TARGET" ]; then
    AWS_CRT_TARGET=$AWS_CRT_HOST
fi

LIB_PATH=target/cmake-build/lib
SKIP_INSTALL=

# Cross compiles do not need local installs, and they have a different lib output path
if [[ "$AWS_CRT_TARGET" != "$AWS_CRT_HOST" ]]; then
    SKIP_INSTALL=--skip-install
    LIB_PATH=target/cmake-build/aws-crt-java/lib
fi

# Pry the builder version this CRT is using out of ci.yml
BUILDER_VERSION=$(cat .github/workflows/ci.yml | grep 'BUILDER_VERSION:' | sed 's/\s*BUILDER_VERSION:\s*\(.*\)/\1/')
echo "Using builder version ${BUILDER_VERSION}"

aws s3 cp s3://aws-crt-builder/releases/${BUILDER_VERSION}/builder.pyz ./builder
chmod a+x builder

# Upload the lib to S3
GIT_TAG=$(git describe --tags)
PKG_VERSION=$(git describe --tags | cut -f2 -dv)

mvn versions:set -DnewVersion=${PKG_VERSION}
./builder build -p aws-crt-java --target=$AWS_CRT_TARGET run_tests=false

aws s3 cp --recursive $LIB_PATH s3://aws-crt-java-pipeline/${GIT_TAG}/lib
aws s3 cp --exclude "*" --include "*.jar" target/ s3://aws-crt-java-pipeline/${GIT_TAG}/jar
