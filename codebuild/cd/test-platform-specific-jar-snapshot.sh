#!/bin/sh

set -ex

PLATFORM_ARRAY=("linux-armv6" "linux-armv7" "linux-aarch_64" "linux-x86_32" "linux-x86_64" "osx-aarch_64" "osx-x86_64" "windows-x86_32" "windows-x86_64")

# test uber jar
mvn -B dependency:get -DrepoUrl=https://aws.oss.sonatype.org/content/repositories/snapshots -Dartifact=software.amazon.awssdk.crt:aws-crt:${CRT_VERSION}-SNAPSHOT -Dtransitive=false

for str in ${PLATFORM_ARRAY[@]}; do
  # Test platform specific jar
  mvn -B dependency:get -DrepoUrl=https://aws.oss.sonatype.org/content/repositories/snapshots -Dartifact=software.amazon.awssdk.crt:aws-crt:${CRT_VERSION}-SNAPSHOT:jar:${str} -Dtransitive=false
done
