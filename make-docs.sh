#!/usr/bin/env bash

set -e

pushd $(dirname $0) >/dev/null

# clean
rm -rf docs/

# build
mvn clean javadoc:javadoc -Prelease
# mvn generates the doc in target/site/apidocs/ by default, move it to our common doc folder
cp -r target/site/apidocs/ docs/

popd >/dev/null
