
import Builder
import sys
import os
import os.path
import tempfile
from builder.core.host import current_host


class AWSCrtJavaTest(Builder.Action):

    def _write_secret_to_temp_file(self, secret_name):
        secret_value = self.env.shell.get_secret(secret_name)

        temp_file = tempfile.NamedTemporaryFile()
        temp_file.write(str.encode(secret_value))
        temp_file.flush()

        return temp_file

    def _run_java_tests(self, *extra_args):
        if os.path.exists('log.txt'):
            os.remove('log.txt')

        profiles = 'continuous-integration'
        if current_host() == 'alpine':
            profiles += ',alpine'

        cmd_args = [
            "mvn", "-B",
            "-P", profiles,
            "-DredirectTestOutputToFile=true",
            "-DreuseForks=false",
            "-Daws.crt.memory.tracing=2",
            "-Daws.crt.debugnative=true",
            "-Daws.crt.aws_trace_log_per_test",
            "-Daws.crt.ci=true",
        ]
        cmd_args.extend(extra_args)
        cmd_args.append("test")

        result = self.env.shell.exec(*cmd_args, check=False)
        if result.returncode:
            if os.path.exists('log.txt'):
                print("--- CRT logs from failing test ---")
                with open('log.txt', 'r') as log:
                    print(log.read())
                print("----------------------------------")

            sys.exit(f"Tests failed")

    def run(self, env):
        self.env = env

        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')

        endpoint = env.shell.get_secret("unit-test/endpoint")

        with self._write_secret_to_temp_file("unit-test/rootca") as root_ca_file, \
                self._write_secret_to_temp_file("unit-test/certificate") as cert_file, \
                self._write_secret_to_temp_file("unit-test/privatekey") as key_file, \
                self._write_secret_to_temp_file("ecc-test/certificate") as ecc_cert_file, \
                self._write_secret_to_temp_file("ecc-test/privatekey") as ecc_key_file:

            self._run_java_tests(
                "-DrerunFailingTestsCount=5",
                f"-Dendpoint={endpoint}",
                f"-Drootca={root_ca_file.name}",
                f"-Dcertificate={cert_file.name}",
                f"-Dprivatekey={key_file.name}",
                f"-Decc_certificate={ecc_cert_file.name}",
                f"-Decc_privatekey={ecc_key_file.name}",
            )

        # run the ShutdownTest by itself
        env.shell.setenv('AWS_CRT_SHUTDOWN_TESTING', '1')

        self._run_java_tests("-Dtest=ShutdownTest")

        # run the elasticurl integration tests
        python = sys.executable
        env.shell.exec(python, 'crt/aws-c-http/integration-testing/http_client_test.py',
                       python, 'integration-testing/java_elasticurl_runner.py', check=True)
