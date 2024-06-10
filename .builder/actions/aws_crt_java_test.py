import Builder
import sys
import os
import os.path


class AWSCrtJavaTest(Builder.Action):

    def _run_java_tests(self, *extra_args):
        if os.path.exists('log.txt'):
            os.remove('log.txt')

        profiles = 'continuous-integration'
        if os.getenv("AWS_GRAALVM_CI") is not None:
            profiles = 'graalvm-native'

        cmd_args = [
            "mvn", "-B",
            "-P", profiles,
            "-DredirectTestOutputToFile=true",
            "-DreuseForks=false",
            "-Daws.crt.memory.tracing=2",
            "-Daws.crt.debugnative=true",
            "-Daws.crt.aws_trace_log_per_test",
            "-Daws.crt.ci=true"
        ]
        cmd_args.extend(extra_args)
        if os.getenv("AWS_GRAALVM_CI") is not None:
            cmd_args.append("clean")
        cmd_args.append("test")

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

        self._run_java_tests("-DrerunFailingTestsCount=5")

        # run the ShutdownTest by itself
        env.shell.setenv('AWS_CRT_SHUTDOWN_TESTING', '1')
        self._run_java_tests("-Dtest=ShutdownTest")

        # run the InitTest by itself.  This creates an environment where the test itself is the one that
        # causes the CRT to be loaded and initialized.
        self._run_java_tests("-Dtest=InitTest")

        if os.getenv("AWS_GRAALVM_CI") is None:
            # Skip elasticurl for GRAALVM, as GraalVM generates binary instead of jar used by elasticurl.
            # run the elasticurl integration tests
            python = sys.executable
            env.shell.exec(python, 'crt/aws-c-http/integration-testing/http_client_test.py',
                           python, 'integration-testing/java_elasticurl_runner.py', check=True)

    def run(self, env):
        self.env = env

        return Builder.Script([
            Builder.SetupCrossCICrtEnvironment(),
            self.start_maven_tests  # Then run the Maven stuff
        ])
