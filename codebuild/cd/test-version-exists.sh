#!/usr/bin/env bash
set -e
set -x
#force a failure if there's no tag
git describe --abbrev=0
#now get the tag
CURRENT_TAG_VERSION=$(git describe --abbrev=0)
PUBLISHED_TAG_VERSION=$(curl -s "https://repo.maven.apache.org/maven2/software/amazon/awssdk/crt/aws-crt-java/maven-metadata.xml" | grep "<version>" | tail -1 | cut -f2 -d ">" | cut -f1 -d "<")
if [[ $PUBLISHED_TAG_VERSION==$CURRENT_TAG_VERSION ]]; then
    echo "$CURRENT_TAG_VERSION is already in Sonatype, cut a new tag if you want to upload another version."
    exit 1
fi

echo "$CURRENT_TAG_VERSION currently does not exist in Sonatype, allowing pipeline to continue."
exit 0
