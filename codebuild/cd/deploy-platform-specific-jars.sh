#!/bin/sh

set -ex

if [[ $DEPLOY_VERSION = *-SNAPSHOT ]]; then
  # snapshot doesn't need to gpg sign the file to deploy
  DEPLOY_FILE_GOAL=deploy:deploy-file
  DEPLOY_REPOSITORY_URL=https://aws.oss.sonatype.org/content/repositories/snapshots
else
  # Need to sign the file to deploy to staging repo
  # pin to 3.1.0, somehow the latest 3.2.0 breaks the file deploy with 401.
  DEPLOY_FILE_GOAL=org.apache.maven.plugins:maven-gpg-plugin:3.1.0:sign-and-deploy-file
  DEPLOY_REPOSITORY_URL=https://aws.oss.sonatype.org:443/service/local/staging/deployByRepositoryId/${STAGING_REPO_ID}
fi

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
    -Dgpg.passphrase=$GPG_PASSPHRASE \
    -DgroupId=software.amazon.awssdk.crt -DartifactId=aws-crt -Dpackaging=jar \
    -Dversion=$DEPLOY_VERSION \
    -Dfile=./target/aws-crt-$DEPLOY_VERSION.jar \
    -Dfiles=$FILES \
    -Dclassifiers=$CLASSIFIERS \
    -Dtypes=$TYPES \
    -DpomFile=pom.xml \
    -DrepositoryId=ossrh -Durl=$DEPLOY_REPOSITORY_URL
