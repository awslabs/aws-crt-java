import sys
import os
import re


if sys.platform != 'darwin':
    print("WARNING: Not running on macos. Skip the compatibility validation.")
    sys.exit(True)

# Default target macos version setup in pom.xml > ${cmake.min_osx_version}
supported_version = "10.9"
arch = "x86_64"

machine_id = os.uname().machine
m = re.match(r'^(aarch64|armv[6-8]|arm64)', machine_id.strip())
if m:
    # The oldest version we can target on arm64 is 11.0
    supported_version = "11.0"
    arch = m.group(1)
    if arch == 'aarch64' or arch == 'arm64':
        arch = "armv8"

if os.system("otool -l target/classes/osx/{}/libaws-crt-jni.dylib | grep -E minos | cut -f2 -ds".format(arch)) != supported_version:
    # Failed
    sys.exit(False)

sys.exit(True)
