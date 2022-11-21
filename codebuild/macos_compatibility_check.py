import sys
import subprocess
import os
import re


def main():
    if sys.platform != 'darwin':
        print("WARNING: Not running on macos. Skip the compatibility validation.")
        sys.exit(True)

    # Default target macos version setup in pom.xml > ${cmake.min_osx_version}
    supported_version = "10.9"
    arch = "x86_64"


    # otool result has a different format between arm and x64
    # for arm: we check for "minos"
    # for x64: The format will be:
    #
    # Load command 8
    # cmd LC_VERSION_MIN_MACOSX
    #   cmdsize 16
    #   version 10.9
    #       sdk 12.1
    # Load command 9
    otool_cmd = "otool -l target/cmake-build/lib/osx/{}/libaws-crt-jni.dylib | grep -A3 \'LC_VERSION_MIN_MACOSX\' | grep -E version | tr -s ' ' | cut -f3 -d' '".format(arch)

    if len(sys.argv) > 1:
        # Parsing the macos archtecture
        arch = sys.argv[1]
    else:
        # If the archtecture is not set, set from system call
        arch = os.uname().machine
        print("uname result {}".format(arch))

    if re.match(r'^(aarch64|armv[6-8]|arm64)', arch):
        arch = "armv8"
        # The oldest version we can target on arm64 is 11.0
        supported_version = "11.0"
        otool_cmd = "otool -l target/cmake-build/lib/osx/{}/libaws-crt-jni.dylib | grep -E minos | tr -s ' ' | cut -f3 -d' '".format(arch)

    print("Start to validate the build binary for MacOS with architecture {}, expected min os version: {}".format(arch,supported_version))

    result = subprocess.check_output(otool_cmd, shell=True).decode("utf-8")
    if result != supported_version:
        # Failed
        print("Failed the compatibility validation on MacOS architecture {}, expected {} and built {}".format(arch, supported_version, result))
        sys.exit(1)

    print("Pass the compatibility validation on MacOS architecture {} with min supported os version '{}'".format(arch,result))
    sys.exit(0)

if __name__ == "__main__":
    main()
