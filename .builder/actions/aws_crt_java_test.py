
from genericpath import exists
import Builder
import os


class AWSCrtJavaTest(Builder.Action):

    def run(self, env):
        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = []

        for i in range(10):
            if os.system("mvn -B test -DredirectTestOutputToFile=true -DforkCount=0 \
                -DrerunFailingTestsCount=5 -DskipAfterFailureCount=1 \
                -Daws.crt.memory.tracing=2 -Daws.crt.debugnative=true"):
                # Failed
                actions.append("exit 1")
                os.system("cat log.txt")
                exists

        return Builder.Script(actions, name='aws-crt-java-test')
