#!/usr/bin/env bash

set -ex

cd `dirname $0`/../..

git submodule update --init

AWS_CRT_HOST=`uname | tr '[:upper:]' '[:lower:]'`-`uname -m`

if [ -z "$AWS_CRT_TARGET" ]; then
    AWS_CRT_TARGET=$AWS_CRT_HOST
fi

SKIP_INSTALL=

if [[ "$AWS_CRT_TARGET" != "$AWS_CRT_HOST" ]]; then
    SKIP_INSTALL=--skip-install
fi

# Pry the builder version this CRT is using out of ci.yml
BUILDER_VERSION=$(cat .github/workflows/ci.yml | grep 'BUILDER_VERSION:' | sed 's/\s*BUILDER_VERSION:\s*\(.*\)/\1/')
echo "Using builder version ${BUILDER_VERSION}"

aws s3 cp s3://aws-crt-builder/releases/${BUILDER_VERSION}/builder.pyz ./builder
chmod a+x builder

# Upload the lib to S3
GIT_TAG=$(git describe --tags)

# use a fix deploy version for platform specific jar
mvn versions:set -DnewVersion=deploy
./builder build -p aws-crt-java --target=$AWS_CRT_TARGET run_tests=false
# Builder corss compile the shared lib to `target/cmake-build/aws-crt-java/`, move it to th expected path for mvn to generate jar.
mv target/cmake-build/aws-crt-java/* target/cmake-build/

JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 mvn -B install -DskipTests -P$AWS_CRT_TARGET -Dshared-lib.skip=true

aws s3 cp --recursive --include "*.so" target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
aws s3 cp target/ s3://aws-crt-java-pipeline/${GIT_TAG}/jar/ --recursive --exclude "*" --include "aws-crt*.jar"
