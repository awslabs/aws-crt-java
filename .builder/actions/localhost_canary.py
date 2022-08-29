import Builder
import sys
import os


class LocalhostCanary(Builder.Action):

    def run(self, env):
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = [
            # "mvn install -DskipTests",
            "cd ./samples/HttpClientCanary",
            "ls"
            # "mvn install",
            # "mvn exec:exec@netty exec:exec@crt"
        ]

        return Builder.Script(actions, name='aws-crt-java-test')
