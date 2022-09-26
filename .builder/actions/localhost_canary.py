import Builder
import sys
import os


class LocalhostCanary(Builder.Action):

    def run(self, env):
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = [
            "mvn install -DskipTests",
            "cd ./samples/HttpClientCanary && mvn install",
            "cd ./samples/HttpClientCanary && mvn exec:exec@netty",
            "cd ./samples/HttpClientCanary && mvn exec:exec@crt"
        ]

        return Builder.Script(actions, name='aws-crt-java-test')
