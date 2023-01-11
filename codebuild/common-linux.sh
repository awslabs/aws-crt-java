#!/bin/bash

set -e

if test -f "/tmp/setup_proxy_test_env.sh"; then
    source /tmp/setup_proxy_test_env.sh
fi

env

git submodule update --init

curl https://www.amazontrust.com/repository/AmazonRootCA1.pem --output /tmp/AmazonRootCA1.pem
cert=$(aws secretsmanager get-secret-value --secret-id "unit-test/certificate" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\") && echo -e "$cert" > /tmp/certificate.pem
key=$(aws secretsmanager get-secret-value --secret-id "unit-test/privatekey" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\") && echo -e "$key" > /tmp/privatekey.pem
ecc_cert=$(aws secretsmanager get-secret-value --secret-id "ecc-test/certificate" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\") && echo -e "$cert" > /tmp/ecc_certificate.pem
ecc_privatekey=$(aws secretsmanager get-secret-value --secret-id "ecc-test/privatekey" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\") && echo -e "$key" > /tmp/ecc_privatekey.pem
key_p8=$(aws secretsmanager get-secret-value --secret-id "unit-test/privatekey-p8" --query "SecretString" | cut -f2 -d":" | cut -f2 -d\") && echo -e "$key_p8" > /tmp/privatekey_p8.pem
ENDPOINT=$(aws secretsmanager get-secret-value --secret-id "unit-test/endpoint" --query "SecretString" | cut -f2 -d":" | sed -e 's/[\\\"\}]//g')

# Go to repository root directory
cd $CODEBUILD_SRC_DIR

# Build and run all the tests!
ulimit -c unlimited
mvn -B test $* \
    -DredirectTestOutputToFile=true \
    -DreuseForks=false \
    -Dendpoint=$ENDPOINT \
    -Dcertificate=/tmp/certificate.pem \
    -Dprivatekey=/tmp/privatekey.pem \
    -Decc_certificate=/tmp/ecc_certificate.pem \
    -Decc_privatekey=/tmp/ecc_privatekey.pem \
    -Drootca=/tmp/AmazonRootCA1.pem \
    -Dprivatekey_p8=/tmp/privatekey_p8.pem \
    -Daws.crt.debugnative=true \
    -Dcmake.s2nNoPqAsm=ON

# Run the MQTT5 tests again, but connecting to Codebuild Mosquitto
source ./utils/mqtt5_test_setup.sh s3://aws-crt-test-stuff/IotProdMQTT5EnvironmentVariables.txt us-east-1
mvn -B test -Dtest=Mqtt5ClientTest -Daws.crt.debugnative=true -DreuseForks=false -DredirectTestOutputToFile=true
source ./utils/mqtt5_test_setup.sh s3://aws-crt-test-stuff/IotProdMQTT5EnvironmentVariables.txt cleanup
