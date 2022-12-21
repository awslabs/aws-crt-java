#!/bin/sh

set -ex

cd `dirname $0`/../..

git submodule update --init

export GIT_TAG=$(git describe --tags)

# use a fix deploy version for platform specific jar
mvn versions:set -DnewVersion=deploy

mvn -B install -DskipTests -P mac-x64

# Copy artifacts to dist
mkdir -p ../dist
cp -rv target/cmake-build/lib ../dist/

aws s3 cp --recursive --exclude "*" --include "*.dylib" ./target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
aws s3 cp --recursive --exclude "*" --include "aws-crt*.jar" ./target s3://aws-crt-java-pipeline/${GIT_TAG}/jar
