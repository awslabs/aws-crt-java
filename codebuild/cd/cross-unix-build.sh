#!/bin/sh

set -ex

[ $# -eq 1 ]

cd `dirname $0`/../..

GIT_TAG=$(git describe --tags)
# use a fix deploy version for platform specific jar
mvn versions:set -DnewVersion=deploy

python3 -c "from urllib.request import urlretrieve; urlretrieve('https://d19elf31gohf1l.cloudfront.net/LATEST/builder.pyz?date=`date +%s`', 'builder')"
chmod a+x builder
./builder build -p aws-crt-java $*

# Upload the lib to S3
aws s3 cp --recursive target/cmake-build/aws-crt-java/lib s3://aws-crt-java-pipeline/${GIT_TAG}/lib
aws s3 cp --exclude "*" --include "*.jar" target/ s3://aws-crt-java-pipeline/${GIT_TAG}/jar
