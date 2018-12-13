#!/bin/bash

set -e
set -x

CMAKE_ARGS="$@"

function install_library {
    pushd build/deps/
    git clone https://github.com/$1.git $1
    cd $1

    if [ -n "$2" ]; then
        git checkout $2
    fi

    mkdir build
    cd build

    cmake -DCMAKE_INSTALL_PREFIX=$AWS_C_INSTALL $CMAKE_ARGS ../
    make install

    popd
}

mkdir -p `pwd`/build/deps/install
export AWS_C_INSTALL=`pwd`/build/deps/install

# Non-OSX unix needs s2n
if [ "$TRAVIS_OS_NAME" != "osx" ]; then
    install_library awslabs/s2n
fi

# build aws-c-* dependencies
./build-deps.sh $CMAKE_ARGS

# build java package
mvn compile
