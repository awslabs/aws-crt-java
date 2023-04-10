import Builder
import sys
import os
import os.path
import tempfile


class AWSCrtJavaTest(Builder.Action):

    def _run_java_tests(self, *cmd_args):
        if os.path.exists('log.txt'):
            os.remove('log.txt')

        result = self.env.shell.exec(*cmd_args, check=False)
        if result.returncode:
            if os.path.exists('log.txt'):
                print("--- CRT logs from failing test ---")
                with open('log.txt', 'r') as log:
                    print(log.read())
                print("----------------------------------")
            sys.exit(f"Tests failed")

    def start_maven_tests(self, env):
        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')

        self._run_java_tests(
            "mvn", "-P", "continuous-integration", "-B", "test",
            "-DredirectTestOutputToFile=true",
            "-DreuseForks=false",
            "-DrerunFailingTestsCount=5",
            "-Daws.crt.memory.tracing=2",
            "-Daws.crt.debugnative=true",
            "-Daws.crt.aws_trace_log_per_test",
            "-Daws.crt.ci=true"
        )

        # run the ShutdownTest by itself
        env.shell.setenv('AWS_CRT_SHUTDOWN_TESTING', '1')
        self._run_java_tests(
            "mvn", "-P", "continuous-integration", "-B", "test",
            "-DredirectTestOutputToFile=true",
            "-DreuseForks=false",
            "-Daws.crt.memory.tracing=2",
            "-Daws.crt.debugnative=true",
            "-Daws.crt.aws_trace_log_per_test",
            "-Daws.crt.ci=true",
            "-Dtest=ShutdownTest",
        )

        # run the elasticurl integration tests
        python = sys.executable
        env.shell.exec(python, 'crt/aws-c-http/integration-testing/http_client_test.py',
                       python, 'integration-testing/java_elasticurl_runner.py', check=True)

    def run(self, env):
        self.env = env

        return Builder.Script([
            Builder.SetupCIFromJSON(), # setup CI environment variables
            self.start_maven_tests # Then run the Maven stuff
            ])
