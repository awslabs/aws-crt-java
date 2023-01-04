
import Builder
import sys
import os
import tempfile


class AWSCrtJavaTest(Builder.Action):

    def _write_secret_to_temp_file(self, env, secret_name):
        secret_value = env.shell.get_secret(secret_name)

        temp_file = tempfile.NamedTemporaryFile()
        temp_file.write(str.encode(secret_value))
        temp_file.flush()

        return temp_file

    def run(self, env):
        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = []

        endpoint = env.shell.get_secret("unit-test/endpoint")

        with self._write_secret_to_temp_file(env, "unit-test/rootca") as root_ca_file, self._write_secret_to_temp_file(env, "unit-test/certificate") as cert_file, \
                self._write_secret_to_temp_file(env, "unit-test/privatekey") as key_file, self._write_secret_to_temp_file(env, "ecc-test/certificate") as ecc_cert_file, \
                self._write_secret_to_temp_file(env, "ecc-test/privatekey") as ecc_key_file:

            test_command = "mvn -P continuous-integration -B test -DredirectTestOutputToFile=true -DreuseForks=false " \
                "-DrerunFailingTestsCount=5 -Daws.crt.memory.tracing=2 -Daws.crt.debugnative=true -Daws.crt.ci=true " \
                "-Daws.crt.aws_trace_log_per_test=true " \
                "-Dendpoint={} -Dcertificate={} -Dprivatekey={} -Drootca={} -Decc_certificate={} -Decc_privatekey={}".format(endpoint,
                                                                                                                             cert_file.name, key_file.name, root_ca_file.name, ecc_cert_file.name, ecc_key_file.name)

            all_test_result = os.system(test_command)

        env.shell.setenv('AWS_CRT_SHUTDOWN_TESTING', '1')
        shutdown_test_result = os.system("mvn -P continuous-integration -B test -DredirectTestOutputToFile=true -DreuseForks=false \
            -Daws.crt.memory.tracing=2 -Daws.crt.debugnative=true -Dtest=ShutdownTest")

        if shutdown_test_result or all_test_result:
            # Failed
            actions.append("exit 1")
        os.system("cat log.txt")
        python = sys.executable
        actions.append(
            [python, 'crt/aws-c-http/integration-testing/http_client_test.py',
                python, 'integration-testing/java_elasticurl_runner.py'])

        return Builder.Script(actions, name='aws-crt-java-test')
