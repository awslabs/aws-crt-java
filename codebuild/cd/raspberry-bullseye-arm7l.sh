#!/bin/sh

set -ex

cd `dirname $0`/../..

chmod a+x builder
./builder build -p aws-crt-java run_tests=false
