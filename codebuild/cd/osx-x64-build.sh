#!/bin/sh

set -ex

cd `dirname $0`/../..

git submodule update --init

mvn -B compile

# Copy artifacts to dist
mkdir -p ../dist
cp -rv target/cmake-build/lib ../dist/
