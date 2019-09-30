#!/bin/bash

set -e

env

curl https://www.amazontrust.com/repository/AmazonRootCA1.pem --output /tmp/AmazonRootCA1.pem
cert=$(aws --profile=crt secretsmanager get-secret-value --secret-id "unit-test/certificate" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\") && echo -e "$cert" > /tmp/certificate.pem
key=$(aws --profile=crt secretsmanager get-secret-value --secret-id "unit-test/privatekey" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\") && echo -e "$key" > /tmp/privatekey.pem
ENDPOINT=$(aws --profile=crt secretsmanager get-secret-value --secret-id "unit-test/endpoint" --query "SecretString" | cut -f2 -d":" | sed -e 's/[\\\"\}]//g')

# build java package
cd $CODEBUILD_SRC_DIR

ulimit -c unlimited
mvn -B test -DredirectTestOutputToFile=true -DreuseForks=false -Dendpoint=$ENDPOINT -Dcertificate=/tmp/certificate.pem -Dprivatekey=/tmp/privatekey.pem -Drootca=/tmp/AmazonRootCA1.pem -Daws.crt.debugnative=true -Daws.crt.log.destination=File -Daws.crt.log.filename=/tmp/log.txt -Daws.crt.log.level=Trace
