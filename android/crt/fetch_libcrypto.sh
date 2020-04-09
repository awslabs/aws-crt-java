#!/usr/bin/env bash

set -ex

if [ $# -eq 0 ]; then
    $0 armeabi-v7a
    $0 arm64-v8a
    exit 0
fi

# Ensure 1 argument, the ABI
[ $# -eq 1 ]

pushd $(dirname $0)

cmake_binary_dir=../../target/cmake-build
android_abi=$1

# Map android ABI -> the ABI name we use for prebuilt libs
AWS_ANDROID_ABI=armv7
if [ $android_abi == 'arm64-v8a' ]; then
    AWS_ANDROID_ABI=arm64
elif [ $android_abi == 'x86' ]; then
    AWS_ANDROID_ABI=x86
elif [ $android_abi == 'x86_64' ]; then
    AWS_ANDROID_ABI=x64
fi

echo "Installing libcrypto for Android ${AWS_ANDROID_ABI}"

mkdir -p ${cmake_binary_dir}/deps/${android_abi}/libcrypto
if [ ! -e ${cmake_binary_dir}/deps/${android_abi}/libcrypto/libcrypto-1.1.1-android-${AWS_ANDROID_ABI}.tar.gz ]; then
    curl -sSL --retry 3 "https://d19elf31gohf1l.cloudfront.net/_binaries/libcrypto/libcrypto-1.1.1-android-${AWS_ANDROID_ABI}.tar.gz" \
        -o "${cmake_binary_dir}/deps/${android_abi}/libcrypto/libcrypto-1.1.1-android-${AWS_ANDROID_ABI}.tar.gz"
fi

tar xzf "${cmake_binary_dir}/deps/${android_abi}/libcrypto/libcrypto-1.1.1-android-${AWS_ANDROID_ABI}.tar.gz" -C ${cmake_binary_dir}/deps/${android_abi}/libcrypto

popd
