#!/usr/bin/env bash

set -e

pushd $(dirname $0) > /dev/null

# clean
rm -rf docs/

# build
javadoc @javadoc.options

# The existence of this file tells GitHub Pages to just host the HTML as-is
# https://github.blog/2009-12-29-bypassing-jekyll-on-github-pages/
touch docs/.nojekyll

popd > /dev/null
