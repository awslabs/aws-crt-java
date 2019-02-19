#!/bin/sh

set -e
set -x

echo Build started on `date`
cd `dirname $0`/../..

mvn -B compile

mkdir -p ../dist
cp -rv mvn-build/lib ../dist/

echo Build completed on `date`
