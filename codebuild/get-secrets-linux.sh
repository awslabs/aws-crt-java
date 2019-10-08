#!/bin/bash

set -ex

aws --version

curl https://www.amazontrust.com/repository/AmazonRootCA1.pem --output /tmp/AmazonRootCA1.pem
aws secretsmanager get-secret-value --secret-id "unit-test/certificate" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\" > /tmp/certificate.pem
aws secretsmanager get-secret-value --secret-id "unit-test/privatekey" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\" > /tmp/privatekey.pem
export ENDPOINT=$(aws secretsmanager get-secret-value --secret-id "unit-test/endpoint" --query "SecretString" | cut -f2 -d":" | sed -e 's/[\\\"\}]//g')

mvn -B test -DredirectTestOutputToFile=true -DreuseForks=false -Dendpoint=$ENDPOINT -Dcertificate=/tmp/certificate.pem -Dprivatekey=/tmp/privatekey.pem -Drootca=/tmp/AmazonRootCA1.pem -Daws.crt.debugnative=true
