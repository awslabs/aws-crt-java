#!/usr/bin/env bash

set -e

pushd $(dirname $0) > /dev/null

# clean
rm -rf docs/

# print version
javadoc --version

# build
javadoc @javadoc.options

popd > /dev/null
