
import Builder
import sys
import os


class AWSCrtJavaTest(Builder.Action):

    def run(self, env):
        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = []
        if os.system("mvn -B test -DredirectTestOutputToFile=true -DforkCount=0 \
            -DrerunFailingTestsCount=5 -DskipAfterFailureCount=1 \
            -Daws.crt.memory.tracing=2 -Daws.crt.debugnative=true"):
            # Failed
            actions.append("exit 1")
        os.system("cat log.txt")
        python = sys.executable
        actions.append(
            [python, 'crt/aws-c-http/integration-testing/http_client_test.py',
                'sh', 'integration-testing/java_elasticurl_runner.sh'])

        return Builder.Script(actions, name='aws-crt-java-test')
