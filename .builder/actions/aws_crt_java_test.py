
import Builder
import argparse
import os
import sys


class AWSCrtJavaTest(Builder.Action):

    def run(self, env):
        # allow custom python to be used
        parser = argparse.ArgumentParser()
        parser.add_argument('--python')
        args = parser.parse_known_args(env.args.args)[0]
        python = args.python if args.python else sys.executable

        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')

        actions = [
            ['mvn', '-B', 'test', '-DforkCount=0', '-DrerunFailingTestsCount=5'],
            ['cat', 'log.txt']
        ]

        return Builder.Script(actions, name='aws-crt-java-test')
