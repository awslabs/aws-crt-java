#!/bin/bash

set -e

curl https://www.amazontrust.com/repository/AmazonRootCA1.pem --output /tmp/AmazonRootCA1.pem
cert=$(aws secretsmanager get-secret-value --secret-id "unit-test/certificate" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\") && echo -e "$cert" > /tmp/certificate.pem
key=$(aws secretsmanager get-secret-value --secret-id "unit-test/privatekey" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\") && echo -e "$key" > /tmp/privatekey.pem
ENDPOINT=$(aws secretsmanager get-secret-value --secret-id "unit-test/endpoint" --query "SecretString" | cut -f2 -d":" | sed -e 's/[\\\"\}]//g')

# build java package
cd $CODEBUILD_SRC_DIR

ulimit -c unlimited
mvn -B test -DredirectTestOutputToFile=true -DreuseForks=false -Dendpoint=$ENDPOINT -Dcertificate=/tmp/certificate.pem -Dprivatekey=/tmp/privatekey.pem -Drootca=/tmp/AmazonRootCA1.pem
