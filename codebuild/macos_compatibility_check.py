import sys
import os
import re


if sys.platform != 'darwin':
    print("WARNING: Not running on macos. Skip the compatibility validation.")
    sys.exit(True)

supported_version = "10.9"
arch = "x86_64"
machine_id = os.uname()[4]
m = re.match(r'^(aarch64)', machine_id.strip())
if m == 'aarch64':
    # The oldest version we can target on arm64 is 11
    supported_version = "11.0"
    arch = "armv8"

if os.system("otool -l target/classes/osx/{}/libaws-crt-jni.dylib | grep -E minos | cut -f2 -ds".format(arch), capture_output=True) != supported_version:
    # Failed
    sys.exit(False)

sys.exit(True)
