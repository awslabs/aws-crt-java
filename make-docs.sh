#!/usr/bin/env bash

set -e

pushd $(dirname $0) >/dev/null

# clean
rm -rf docs/

# build
mvn clean javadoc:javadoc -Prelease
# mvn generates the doc in apidocs/ subfolder, move it out
mv docs/apidocs/* docs/

popd >/dev/null
