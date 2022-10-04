
import Builder
import sys
import os


class AWSCrtJavaTest(Builder.Action):

    def run(self, env):
        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = []

        all_test_result = os.system("mvn -P continuous-integration -B test -DredirectTestOutputToFile=true -DreuseForks=false \
            -DrerunFailingTestsCount=5 -Daws.crt.memory.tracing=2 -Daws.crt.debugnative=true -Daws.crt.aws_trace_log_per_test=true")

        env.shell.setenv('AWS_CRT_SHUTDOWN_TESTING', '1')
        shutdown_test_result = os.system("mvn -P continuous-integration -B test -DredirectTestOutputToFile=true -DreuseForks=false \
            -Daws.crt.memory.tracing=2 -Daws.crt.debugnative=true -Dtest=ShutdownTest")

        if shutdown_test_result or all_test_result:
            # Failed
            actions.append("exit 1")
        os.system("cat log.txt")
        os.system("cat trace.txt")
        python = sys.executable
        actions.append(
            [python, 'crt/aws-c-http/integration-testing/http_client_test.py',
                python, 'integration-testing/java_elasticurl_runner.py'])

        return Builder.Script(actions, name='aws-crt-java-test')
