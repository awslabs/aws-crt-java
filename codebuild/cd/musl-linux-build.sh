#!/usr/bin/env bash

set -ex

IMAGE_NAME=$1
shift
CLASSIFIER=$1
shift

# Pry the builder version this CRT is using out of ci.yml
BUILDER_VERSION=$(cat .github/workflows/ci.yml | grep 'BUILDER_VERSION:' | sed 's/\s*BUILDER_VERSION:\s*\(.*\)/\1/')
echo "Using builder version ${BUILDER_VERSION}"

aws ecr get-login-password | docker login 123124136734.dkr.ecr.us-east-1.amazonaws.com -u AWS --password-stdin
export DOCKER_IMAGE=123124136734.dkr.ecr.us-east-1.amazonaws.com/${IMAGE_NAME}:${BUILDER_VERSION}
export QEMU_IMAGE=123124136734.dkr.ecr.us-east-1.amazonaws.com/multiarch-qemu-user-static:latest
docker run --rm --privileged ${QEMU_IMAGE} --reset -p yes

export BRANCH_TAG=$(git describe --tags)
docker run --mount type=bind,src=$(pwd),dst=/root/aws-crt-java --env AWS_ACCESS_KEY_ID --env AWS_SECRET_ACCESS_KEY --env AWS_DEFAULT_REGION --env CXXFLAGS --env AWS_CRT_ARCH --env CRT_CLASSIFIER $DOCKER_IMAGE --version=${BUILDER_VERSION} build -p aws-crt-java --branch ${BRANCH_TAG} run_tests=false --classifier ${CLASSIFIER}
docker container prune -f

# Upload the artifacts to S3
export GIT_TAG=$(git describe --tags)

aws s3 cp --recursive --include "*.so" target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
aws s3 cp target/ s3://aws-crt-java-pipeline/${GIT_TAG}/jar/ --recursive --exclude "*" --include "aws-crt*.jar"
# the FIPS subset contains FIPS build for linux-armv8 and linux-x64, and NON-FIPS build for rest of the linux platforms.
aws s3 cp --recursive --include "*.so" target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/fips_lib
