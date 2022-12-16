#!/bin/sh

set -ex

cd `dirname $0`/../..

git submodule update --init
export GIT_TAG=$(git describe --tags)
export PKG_VERSION=$(git describe --tags | cut -f2 -dv)

mvn -B versions:set -DnewVersion=${PKG_VERSION}-SNAPSHOT
mvn -B install -DskipTests -P mac-arm64

# Copy artifacts to dist
mkdir -p ../dist
cp -rv target/cmake-build/lib ../dist/
cp target/*.jar ../dist/

aws s3 cp --recursive --exclude "*" --include "*.dll" ./target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
aws s3 cp --recursive --exclude "*" --include "*.jar" ./target s3://aws-crt-java-pipeline/${GIT_TAG}/jar
