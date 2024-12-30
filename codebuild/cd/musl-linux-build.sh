#!/usr/bin/env bash

set -ex

IMAGE_NAME=$1
shift
CLASSIFIER=$1
shift
PLATFORM=$1
shift

# Pry the builder version this CRT is using out of ci.yml
BUILDER_VERSION=$(cat .github/workflows/ci.yml | grep 'BUILDER_VERSION:' | sed 's/\s*BUILDER_VERSION:\s*\(.*\)/\1/')
echo "Using builder version ${BUILDER_VERSION}"

aws ecr get-login-password | docker login 123124136734.dkr.ecr.us-east-1.amazonaws.com -u AWS --password-stdin
export DOCKER_IMAGE=123124136734.dkr.ecr.us-east-1.amazonaws.com/${IMAGE_NAME}:${BUILDER_VERSION}
#export QEMU_IMAGE=123124136734.dkr.ecr.us-east-1.amazonaws.com/multiarch-qemu-user-static:latest
#docker run --rm --privileged ${QEMU_IMAGE} --reset -p yes
docker run --rm --privileged multiarch/qemu-user-static --reset -p yes

export BRANCH_TAG=$(git describe --tags)
docker run --mount type=bind,src=$(pwd),dst=/root/aws-crt-java --env AWS_DEFAULT_REGION --env CXXFLAGS --env AWS_CRT_ARCH --platform=${PLATFORM} $DOCKER_IMAGE --version=${BUILDER_VERSION} build -p aws-crt-java --classifier ${CLASSIFIER} --branch ${BRANCH_TAG} run_tests=false
docker container prune -f

# Upload the artifacts to S3
export GIT_TAG=$(git describe --tags)

# Double check that shared lib is where we expect
if ! find target/cmake-build/lib -type f -name "*.so" | grep -q .; then
  echo "No .so files found"
  exit 1
fi

aws s3 cp --recursive --exclude "*" --include "*.so" target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
aws s3 cp target/ s3://aws-crt-java-pipeline/${GIT_TAG}/jar/ --recursive --exclude "*" --include "aws-crt*.jar"
