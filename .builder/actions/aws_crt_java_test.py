
import Builder
import os


class AWSCrtJavaTest(Builder.Action):

    def run(self, env):
        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = []

        if os.system("mvn -B test -DredirectTestOutputToFile=true \
            -DrerunFailingTestsCount=5 -DskipAfterFailureCount=1 -DreuseForks=false \
            -Daws.crt.memory.tracing=2 -Daws.crt.debugnative=true"):
            # Failed
            actions.append("exit 1")
        os.system("cat log.txt")

        return Builder.Script(actions, name='aws-crt-java-test')
