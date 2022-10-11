#!/bin/sh

set -ex

cd `dirname $0`/../..

AWS_CRT_TARGET=`uname | tr '[:upper:]' '[:lower:]'`-`uname -m`

chmod a+x builder
./builder build -p aws-crt-java run_tests=false

# Upload the lib to S3
GIT_TAG=$(git describe --tags)
aws s3 cp --recursive target/cmake-build/aws-crt-java/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
