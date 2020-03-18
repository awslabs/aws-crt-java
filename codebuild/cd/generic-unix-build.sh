#!/bin/sh

set -ex

cd `dirname $0`/../..

git submodule update --init

AWS_CRT_HOST=`uname | tr '[:upper:]' '[:lower:]'`-`uname -m`

if [ -z "$AWS_CRT_TARGET" ]; then
    AWS_CRT_TARGET=$AWS_CRT_HOST
fi

SKIP_INSTALL=
if [[ "$AWS_CRT_TARGET" != "$AWS_CRT_HOST" ]]; then
    SKIP_INSTALL=--skip-install
fi

python3 -c "from urllib.request import urlretrieve; urlretrieve('https://d19elf31gohf1l.cloudfront.net/LATEST/builder.pyz?date=`date +%s`', 'builder')"
chmod a+x builder
./builder build -p aws-crt-java --target=$AWS_CRT_TARGET run_tests=0

# Upload the lib to S3
GIT_TAG=$(git describe --tags)
aws s3 cp --recursive target/cmake-build/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
