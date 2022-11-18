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

    if len(sys.argv) > 2:
        # Parsing the macos archtecture
        arch = sys.argv[1]
    else:
        # If the archtecture is not set, set from system call
        arch = os.uname().machine

    if re.match(r'^(aarch64|armv[6-8]|arm64)', arch):
        arch = "armv8"
        # The oldest version we can target on arm64 is 11.0
        supported_version = "11.0"

    otool_cmd = "otool -l target/classes/osx/{}/libaws-crt-jni.dylib | grep -E minos | cut -f2 -ds | tr -d '[:space:]'".format(arch)
    result = subprocess.check_output(otool_cmd, shell=True)
    if result.decode("utf-8") != supported_version:
        # Failed
        sys.exit(False)

    sys.exit(True)

if __name__ == "__main__":
    main()
