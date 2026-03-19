#!/usr/bin/env bash

set -e

pushd $(dirname $0) > /dev/null

# clean
rm -rf docs/

# build
mvn javadoc:javadoc -Dmaven.javadoc.failOnWarnings=true

# copy to docs/
cp -r target/site/apidocs/ docs/

popd > /dev/null
