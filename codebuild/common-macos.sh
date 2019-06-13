#!/bin/bash

# Until CodeBuild supports macOS, this script is just used by Travis.

set -e
set -x

# ensure each required package is installed, if not, make a bottle for brew in ./packages
# so it will be cached for future runs. If the cache is ever blown away, this will update
# the packages as well
# If the bottles are already in ./packages, then just install them
function install_from_brew {
    pushd ./packages
    # usually the existing package is too old for one of the others, so uninstall
    # and reinstall from the cache
    brew uninstall --ignore-dependencies $1 || true
    if [ ! -e $1*bottle*.tar.gz ]; then
        brew install -v --build-bottle $1
        brew bottle -v --json $1
        brew uninstall --ignore-dependencies $1
    fi
    brew install -v $1*bottle*tar.gz
    popd
}

install_from_brew sphinx-doc
install_from_brew cmake

mvn -B compile
