
import Builder
import os


class AWSCrtJavaTest(Builder.Action):

    def run(self, env):
        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = []

        if os.system("mvn -B test -DredirectTestOutputToFile=true -DforkCount=0 \
            -DrerunFailingTestsCount=5 -DskipAfterFailureCount=1 \
            -Daws.crt.memory.tracing=2 -Daws.crt.debugnative=true -Daws.crt.log.level=Trace \
            -Daws.crt.log.destination=File -Daws.crt.log.filename=trace.txt"):
            # Failed
            actions.append("exit 1")
        os.system("cat log.txt")
        os.system("cat trace.txt")

        return Builder.Script(actions, name='aws-crt-java-test')
