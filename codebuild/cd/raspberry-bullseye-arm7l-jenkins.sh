#!/bin/bash
#run build-wheels script in manylinux2014 docker image
set -ex


LIB_PATH=target/cmake-build/lib

# Pry the builder version this CRT is using out of ci.yml
BUILDER_VERSION=$(cat .github/workflows/ci.yml | grep 'BUILDER_VERSION:' | sed 's/\s*BUILDER_VERSION:\s*\(.*\)/\1/')
echo "Using builder version ${BUILDER_VERSION}"

aws s3 cp s3://aws-crt-builder/releases/${BUILDER_VERSION}/builder.pyz ./builder

DOCKER_IMAGE=123124136734.dkr.ecr.us-east-1.amazonaws.com/raspbian-bullseye-arm7l:latest

$(aws --region us-east-1 ecr get-login --no-include-email)

docker pull $DOCKER_IMAGE
docker run --rm --privileged multiarch/qemu-user-static --reset -p yes

docker run --rm \
    --privileged \
    --mount type=bind,source=`pwd`,target=/aws-crt-java \
    --workdir /aws-crt-java \
    --entrypoint /bin/bash \
    --platform linux/arm/v7 \
    $DOCKER_IMAGE \
    codebuild/cd/raspberry-bullseye-arm7l.sh
