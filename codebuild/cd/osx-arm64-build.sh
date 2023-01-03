#!/bin/sh

set -ex

cd `dirname $0`/../..

git submodule update --init
export GIT_TAG=$(git describe --tags)

mvn -B install -DskipTests -P mac-arm64 -Dcrt.classifier=osx-aarch_64

# Copy artifacts to dist
mkdir -p ../dist
# Note: as we ran mvn install, the dist has unexpected files, using S3 bucket to transfer artifact instead
cp -rv target/cmake-build/lib ../dist/

aws s3 cp --recursive --exclude "*" --include "*.dylib" ./target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
aws s3 cp --recursive --exclude "*" --include "aws-crt*.jar" ./target s3://aws-crt-java-pipeline/${GIT_TAG}/jar
