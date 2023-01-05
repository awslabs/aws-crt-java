#!/usr/bin/env bash

set -ex

IMAGE_NAME=$1
shift
CLASSIFIER=$1
shift

# Pry the builder version this CRT is using out of ci.yml
BUILDER_VERSION=$(cat .github/workflows/ci.yml | grep 'BUILDER_VERSION:' | sed 's/\s*BUILDER_VERSION:\s*\(.*\)/\1/')
echo "Using builder version ${BUILDER_VERSION}"

apt-get install jq
aws ecr get-login-password | docker login 123124136734.dkr.ecr.us-east-1.amazonaws.com -u AWS --password-stdin
export DOCKER_IMAGE=123124136734.dkr.ecr.us-east-1.amazonaws.com/${IMAGE_NAME}:${BUILDER_VERSION}

docker volume create --name=artifacts
export VOLUME_PATH=$(docker volume inspect artifacts | jq -r '.[0].Mountpoint')
docker run --mount type=volume,src=artifacts,dst=/root/aws-crt-java --env GITHUB_REF --env GITHUB_HEAD_REF --env AWS_ACCESS_KEY_ID --env AWS_SECRET_ACCESS_KEY --env AWS_DEFAULT_REGION --env CXXFLAGS --env AWS_CRT_ARCH $DOCKER_IMAGE --version=${BUILDER_VERSION} $@
mkdir -p /tmp/artifacts
cp -a ${VOLUME_PATH}/target /tmp/artifacts
docker container prune -f
docker volume rm artifacts

#./builder build -p aws-crt-java --target=$AWS_CRT_TARGET run_tests=false
# Builder corss-compiles the shared lib to `target/cmake-build/aws-crt-java/`, move it to the expected path for mvn to generate the jar.
#mv target/cmake-build/aws-crt-java/* target/cmake-build/

#JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 mvn -B install -DskipTests -Dshared-lib.skip=true -Dcrt.classifier=$CLASSIFIER

# Upload the lib to S3
#GIT_TAG=$(git describe --tags)
export GIT_TAG="musl-testing"

aws s3 cp --recursive --include "*.so" /tmp/artifacts/target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
#aws s3 cp /tmp/artifacts/target/ s3://aws-crt-java-pipeline/${GIT_TAG}/jar/ --recursive --exclude "*" --include "aws-crt*.jar"
