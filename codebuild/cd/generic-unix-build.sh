#!/usr/bin/env bash

set -ex

cd `dirname $0`/../..

git submodule update --init

AWS_CRT_HOST=`uname | tr '[:upper:]' '[:lower:]'`-`uname -m`

if [ -z "$AWS_CRT_TARGET" ]; then
    AWS_CRT_TARGET=$AWS_CRT_HOST
fi

LIB_PATH=target/cmake-build/lib
SKIP_INSTALL=

# Cross compiles do not need local installs, and they have a different lib output path
if [[ "$AWS_CRT_TARGET" != "$AWS_CRT_HOST" ]]; then
    SKIP_INSTALL=--skip-install
    LIB_PATH=target/cmake-build/aws-crt-java/lib
fi

python3 -c "from urllib.request import urlretrieve; urlretrieve('https://d19elf31gohf1l.cloudfront.net/LATEST/builder.pyz?date=`date +%s`', 'builder')"
chmod a+x builder
./builder build -p aws-crt-java --target=$AWS_CRT_TARGET run_tests=false

# Upload the lib to S3
GIT_TAG=$(git describe --tags)
aws s3 cp --recursive $LIB_PATH s3://aws-crt-java-pipeline/${GIT_TAG}/lib
