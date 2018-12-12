#!/bin/bash

set -e
set -x

CMAKE_ARGS="$@"

function install_library {
    pushd build/deps/
    git clone https://github.com/awslabs/$1.git
    cd $1

    if [ -n "$2" ]; then
        git checkout $2
    fi

    mkdir build
    cd build

    cmake -DCMAKE_INSTALL_PREFIX=$AWS_C_INSTALL -DENABLE_SANITIZERS=ON $CMAKE_ARGS ../
    make install

    popd
}

mkdir -p `pwd`/build/deps/install
export AWS_C_INSTALL=`pwd`/build/deps/install

# Non-OSX unix needs s2n
if [ "$TRAVIS_OS_NAME" != "osx" ]; then
    sudo apt-get install libssl-dev -y
    install_library s2n 7c9069618e68214802ac7fbf45705d5f8b53135f
fi

# build aws-c-* dependencies
./build-deps.sh

# build java package
mvn compile
