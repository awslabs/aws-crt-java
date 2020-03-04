#!/bin/sh

set -e
set -x

cd `dirname $0`/../..

git submodule update --init

mvn -B compile
mvn test -DNETWORK_TESTS_DISABLED=1
# Upload the lib to S3
GIT_TAG=$(git describe --tags)
aws s3 cp --recursive target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
