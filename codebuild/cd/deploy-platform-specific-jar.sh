#!/bin/sh

set -ex

if [[ $DEPLOY_VERSION = *-SNAPSHOT ]]; then
  # snapshot doesn't need to gpg sign the file to deploy
  DEPLOY_FILE_GOAL=deploy:deploy-file
  # I got the URL from the deployment log and tests around.
  DEPLOY_REPOSITORY_URL=https://aws.oss.sonatype.org/content/repositories/snapshots

else
  # Need to sign the file to deploy to staging repo
  DEPLOY_FILE_GOAL=gpg:sign-and-deploy-file
  # I got the URL from the deployment log and tests around.
  DEPLOY_REPOSITORY_URL=https://aws.oss.sonatype.org:443/service/local/staging/deployByRepositoryId/${STAGING_REPO_ID}
fi

PLATFORM_ARRAY=("linux-armv6" "linux-armv7" "linux-armv8" "linux-x86_32" "linux-x86_64" "mac-arm64" "mac-x86_64" "windows-x86_32" "windows-x86_64")
for str in ${PLATFORM_ARRAY[@]}; do
  FILES="${FILES}target/aws-crt-deploy-$str.jar,"
  CLASSIFIER="${CLASSIFIER}${str},"
  TYPES="${TYPES}jar,"
done

# remove the last ","
FILES=${FILES::-1}
CLASSIFIER=${CLASSIFIER::-1}
TYPES=${TYPES::-1}


mvn -B -X $DEPLOY_FILE_GOAL \
    -Dgpg.passphrase=$GPG_PASSPHRASE \
    -DgroupId=software.amazon.awssdk.crt -DartifactId=aws-crt -Dpackaging=jar \
    -Dversion=$DEPLOY_VERSION \
    -Dfile=./target/aws-crt-$DEPLOY_VERSION.jar \
    -Dfiles=$FILES \
    -Dclassifiers=$CLASSIFIER \
    -Dtypes=$TYPES \
    -DpomFile=pom.xml \
    -DrepositoryId=ossrh -Durl=$DEPLOY_REPOSITORY_URL
