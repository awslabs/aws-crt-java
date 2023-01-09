
import Builder
import sys
import os
import tempfile
from builder.core.host import current_host

class BuildClassifier(Builder.Action):


    def run(self, env):

        actions = []
        
        crt_classifier = os.getenv("CRT_CLASSIFIER")
        if crt_classifier is not None and os.system(f"mvn -B install -DskipTests -Dshared-lib.skip=true -Dcrt.classifier={crt_classifier}"):

            # Failed
            actions.append("exit 1")

        return Builder.Script(actions, name='build-classifier')
