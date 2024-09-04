#!/usr/bin/env bash

set -e

pushd $(dirname $0) >/dev/null

# clean
rm -rf docs/

# build
mvn clean javadoc:javadoc -Prelease

popd >/dev/null
