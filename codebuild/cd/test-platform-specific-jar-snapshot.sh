#!/bin/sh

set -ex

PLATFORM_ARRAY=("linux-armv6" "linux-armv7" "linux-aarch_64" "linux-x86_32" "linux-x86_64" "osx-aarch_64" "osx-x86_64" "windows-x86_32" "windows-x86_64")

# test uber jar
mvn -B dependency:get -DrepoUrl=https://aws.oss.sonatype.org/content/repositories/snapshots -Dartifact=software.amazon.awssdk.crt:aws-crt:${CRT_VERSION}-SNAPSHOT -Dtransitive=false

for str in ${PLATFORM_ARRAY[@]}; do
  # Test platform specific jar
  mvn -B dependency:get -DrepoUrl=https://aws.oss.sonatype.org/content/repositories/snapshots -Dartifact=software.amazon.awssdk.crt:aws-crt:${CRT_VERSION}-SNAPSHOT:jar:${str} -Dtransitive=false
done




        # <!-- 32-bit Unix -->
        # <profile>
        #     <id>classifiser-windows-x86_64</id>
        #     <properties>
        #         <crt.classifier>windows-x86_64</crt.classifier>
        #     </properties>
        # </profile>
        # <profile>
        #     <id>classifiser-windows-x86_32</id>
        #     <properties>
        #         <crt.classifier>windows-x86_32</crt.classifier>
        #     </properties>
        # </profile>
        # <profile>
        #     <id>classifiser-linux-armv6</id>
        #     <properties>
        #         <crt.classifier>linux-armv6</crt.classifier>
        #     </properties>
        # </profile>
        # <profile>
        #     <id>classifiser-linux-armv7</id>
        #     <properties>
        #         <crt.classifier>linux-armv7</crt.classifier>
        #     </properties>
        # </profile>
        # <profile>
        #     <id>classifiser-linux-aarch_64</id>
        #     <properties>
        #         <crt.classifier>linux-aarch_64</crt.classifier>
        #     </properties>
        # </profile>
        # <profile>
        #     <id>classifiser-linux-x86_64</id>
        #     <properties>
        #         <crt.classifier>linux-x86_64</crt.classifier>
        #     </properties>
        # </profile>
        # <profile>
        #     <id>classifiser-linux-x86_32</id>
        #     <properties>
        #         <crt.classifier>linux-x86_32</crt.classifier>
        #     </properties>
        # </profile>
        # <profile>
        #     <id>classifiser-osx-x86_64</id>
        #     <properties>
        #         <crt.classifier>osx-x86_64</crt.classifier>
        #     </properties>
        # </profile>
        # <profile>
        #     <id>classifiser-osx-aarch_64</id>
        #     <properties>
        #         <crt.classifier>osx-aarch_64</crt.classifier>
        #     </properties>
        # </profile>