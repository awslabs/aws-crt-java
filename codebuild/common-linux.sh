#!/bin/bash

set -e

env

# Enable squid-based integration tests
# These steps are very specific to ubuntu 14.
sudo apt-get -y install squid
squid3 -YC -f /etc/squid3/squid.conf || sudo service squid restart

PROXY_HOST="127.0.0.1"
PROXY_PORT="3128"

curl https://www.amazontrust.com/repository/AmazonRootCA1.pem --output /tmp/AmazonRootCA1.pem
cert=$(aws secretsmanager get-secret-value --secret-id "unit-test/certificate" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\") && echo -e "$cert" > /tmp/certificate.pem
key=$(aws secretsmanager get-secret-value --secret-id "unit-test/privatekey" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\") && echo -e "$key" > /tmp/privatekey.pem
ENDPOINT=$(aws secretsmanager get-secret-value --secret-id "unit-test/endpoint" --query "SecretString" | cut -f2 -d":" | sed -e 's/[\\\"\}]//g')

# build java package
cd $CODEBUILD_SRC_DIR

ulimit -c unlimited
mvn -B test $* -DredirectTestOutputToFile=true -DreuseForks=false -Dendpoint=$ENDPOINT -Dcertificate=/tmp/certificate.pem -Dprivatekey=/tmp/privatekey.pem -Drootca=/tmp/AmazonRootCA1.pem -Daws.crt.debugnative=true -Dproxyhost=$PROXY_HOST -Dproxyport=$PROXY_PORT
