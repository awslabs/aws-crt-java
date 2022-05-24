import Builder
import sys
import os


class LocalhostTest(Builder.Action):

    def run(self, env):
        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = []
        if os.system("mvn -Dtest=Http2ClientLocalHostTest test -DredirectTestOutputToFile=true -DforkCount=0 \
            -DrerunFailingTestsCount=5 \
            -Daws.crt.memory.tracing=2 \
            -Daws.crt.debugnative=true \
            -Daws.crt.localhost=true"):
            # Failed
            actions.append("exit 1")
        python = sys.executable
        actions.append(
            [python, 'crt/aws-c-http/integration-testing/http_client_test.py',
                python, 'integration-testing/java_elasticurl_runner.py'])

        return Builder.Script(actions, name='aws-crt-java-test')
