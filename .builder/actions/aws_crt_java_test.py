
import Builder
import os


class AWSCrtJavaTest(Builder.Action):

    def run(self, env):
        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        os.system( "mvn -B test -DredirectTestOutputToFile=true -DforkCount=0 -DrerunFailingTestsCount=5 && cat log.txt")

        actions = []

        return Builder.Script(actions, name='aws-crt-java-test')
