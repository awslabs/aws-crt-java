#!/bin/sh

set -ex

# mvn_property() {
#   local property="$1"
#   mvn exec:exec -q -N -Dexec.executable='echo' -Dexec.args="\${${property}}"
# }


# RELEASE_VERSION=`mvn_property "project.version"`

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

myArray=("cat" "dog" "mouse" "frog")
for str in ${myArray[@]}; do
  FILES="${FILES}test-$str.jar,"
  CLASSIFIER="${CLASSIFIER}${str},"
done

FILES=${FILES::-1}
CLASSIFIER=${CLASSIFIER::-1}

# VAR1="Hello,"
# VAR2=" World"
# VAR3="$VAR1$VAR2"
# echo "$VAR3"


# mvn $DEPLOY_FILE_GOAL --settings /local/home/dengket/.m2/settings.xml \
#     -Dgpg.passphrase=$GPG_PASSPHRASE \
#     -DgroupId=software.amazon.awssdk.crt -DartifactId=aws-crt -Dpackaging=jar \
#     -Dversion=$DEPLOY_VERSION \
#     -Dfile=./target/aws-crt-$DEPLOY_VERSION.jar \
#     -Dfiles=./target/aws-crt-0.20.3-5-g90472fb6-SNAPSHOT-mac-x86_64.jar \
#     -Dclassifiers=mac-x86_64 \
#     -Dtypes=jar \
#     -DpomFile=pom.xml \
#     -DrepositoryId=ossrh -Durl=$DEPLOY_REPOSITORY_URL




# mvn -X deploy:deploy-file -e --settings /local/home/dengket/.m2/settings.xml \
#     -Dfile=./target/aws-crt-0.20.3-5-g90472fb642.jar \
#     -DgroupId=software.amazon.awssdk.crt -DartifactId=aws-crt \
#     -Dversion=0.20.3-5-g90472fb642-SNAPSHOT \
#     -Dfiles=./target/aws-crt-0.20.3-5-g90472fb6-SNAPSHOT-mac-x86_64.jar \
#     -Dclassifiers=mac-x86_64 \
#     -Dtypes=jar \
#     -DrepositoryId=ossrh -Durl=https://aws.oss.sonatype.org/content/repositories/snapshots | tee snapshots_failed.log
