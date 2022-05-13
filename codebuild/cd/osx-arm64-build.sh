#!/bin/sh

set -ex

cd `dirname $0`/../..

git submodule update --init

mvn -B compile -P mac-arm-make

# Copy artifacts to dist
# CI please work
mkdir -p ../dist
cp -rv target/cmake-build/lib ../dist/
