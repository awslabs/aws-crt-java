import Builder
import sys
import os


class LocalhostTest(Builder.Action):

    def run(self, env):
        # tests must run with leak detection turned on
        actions = ["echo thisShouldBeLocalTest"]

        return Builder.Script(actions, name='localhost-test')
