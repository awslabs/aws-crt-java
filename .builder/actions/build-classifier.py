
import Builder
import sys
import os
import tempfile
from builder.core.host import current_host

# This action is used by the musl-linux-build.sh script to build the binaries for release.
class BuildClassifier(Builder.Action):


    def run(self, env):
        crt_classifier = os.getenv("CRT_CLASSIFIER")
        if crt_classifier is not None:
            env.shell.exec("mvn", "-B", "install", "-DskipTests", "-Dshared-lib.skip=true", f"-Dcrt.classifier={crt_classifier}", check=True)
