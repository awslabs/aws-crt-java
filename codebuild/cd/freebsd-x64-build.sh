#!/bin/sh

set -e
set -x

cd `dirname $0`/../..

mvn -B compile

# Upload the lib to S3
GIT_TAG=$(git describe --abbrev=0)
aws s3 cp --recursive mvn-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
