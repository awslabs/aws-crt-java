#!/bin/sh

set -ex

DEPLOY_FILE_GOAL=org.apache.maven.plugins:maven-gpg-plugin:sign-and-deploy-file
DEPLOY_DIR_URL=file://${BUNDLE_DIR}


CLASSIFIERS_ARRAY=("linux-armv6" "linux-armv7" "linux-aarch_64" "linux-x86_32" "linux-x86_64" "osx-aarch_64" "osx-x86_64" "windows-x86_32" "windows-x86_64" "linux-x86_64-musl" "linux-armv7-musl" "linux-aarch_64-musl" "fips-where-available")

for str in ${CLASSIFIERS_ARRAY[@]}; do
  FILES="${FILES}target/aws-crt-1.0.0-SNAPSHOT-$str.jar,"
  CLASSIFIERS="${CLASSIFIERS}${str},"
  TYPES="${TYPES}jar,"
done

# remove the last ","
FILES=${FILES::-1}
CLASSIFIERS=${CLASSIFIERS::-1}
TYPES=${TYPES::-1}


mvn -B -X $DEPLOY_FILE_GOAL \
    -DgroupId=software.amazon.awssdk.crt -DartifactId=aws-crt -Dpackaging=jar \
    -Dversion=$DEPLOY_VERSION \
    -Dfile=./target/aws-crt-$DEPLOY_VERSION.jar \
    -Dfiles=$FILES \
    -Dclassifiers=$CLASSIFIERS \
    -Dtypes=$TYPES \
    -DpomFile=pom.xml \
    -Durl=$DEPLOY_DIR_URL
