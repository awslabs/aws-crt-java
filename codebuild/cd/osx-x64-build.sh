#!/bin/sh

set -ex

cd `dirname $0`/../..

mvn -B compile

mkdir -p ../dist
cp -rv target/cmake-build/lib ../dist/
