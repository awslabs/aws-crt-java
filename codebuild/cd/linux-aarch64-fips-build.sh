#!/usr/bin/env bash

set -ex

cd $(dirname $0)/../..

git submodule update --init
# double check aws-lc is the FIPS approved branch.
bash ./codebuild/cd/test-fips-branch.sh

# Pry the builder version this CRT is using out of ci.yml
BUILDER_VERSION=$(cat .github/workflows/ci.yml | grep 'BUILDER_VERSION:' | sed 's/\s*BUILDER_VERSION:\s*\(.*\)/\1/')
echo "Using builder version ${BUILDER_VERSION}"

aws s3 cp s3://aws-crt-builder/releases/${BUILDER_VERSION}/builder.pyz ./builder
chmod a+x builder

GIT_TAG=$(git describe --tags)

./builder build -p aws-crt-java run_tests=false --target=linux-arm64 --cmake-extra=-DCRT_FIPS=ON

# When cross-compiling with builder, the shared lib gets an extra "/aws-crt-java/" in its path.
# Move it to expected location.
if [ -d target/cmake-build/aws-crt-java/lib ]; then
    mv target/cmake-build/aws-crt-java/lib target/cmake-build/lib
fi

# Double check that shared lib is where we expect
if ! find target/cmake-build/lib -type f -name "*.so" | grep -q .; then
  echo "No .so files found"
  exit 1
fi

JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 mvn -B package -DskipTests -Dshared-lib.skip=true -Dcrt.classifier=linux-aarch_64-fips

aws s3 cp --recursive --exclude "*" --include "*.so" target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/fips_lib
