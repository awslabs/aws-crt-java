
import Builder
import sys
import os
import tempfile
from builder.core.host import current_host

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

        # We need to force the alpine profile during test so that the right surefire version gets downloaded
        additional_profiles = ''
        if current_host() == 'alpine':
            additional_profiles = '-P alpine '

        with self._write_secret_to_temp_file(env, "unit-test/rootca") as root_ca_file, self._write_secret_to_temp_file(env, "unit-test/certificate") as cert_file, \
        self._write_secret_to_temp_file(env, "unit-test/privatekey") as key_file, self._write_secret_to_temp_file(env, "ecc-test/certificate") as ecc_cert_file, \
        self._write_secret_to_temp_file(env, "ecc-test/privatekey") as ecc_key_file:

            test_command = f"mvn -P continuous-integration {additional_profiles} -B test -DredirectTestOutputToFile=true -DreuseForks=false " \
                f"-DrerunFailingTestsCount=5 -Daws.crt.memory.tracing=2 -Daws.crt.debugnative=true -Daws.crt.ci=true " \
                f"-Dendpoint={endpoint} -Dcertificate={cert_file.name} -Dprivatekey={key_file.name} -Drootca={root_ca_file.name} -Decc_certificate={ecc_cert_file.name} -Decc_privatekey={ecc_key_file.name}"

            all_test_result = os.system(test_command)

        env.shell.setenv('AWS_CRT_SHUTDOWN_TESTING', '1')
        shutdown_test_result = os.system(f"mvn -P continuous-integration {additional_profiles} -B test -DredirectTestOutputToFile=true -DreuseForks=false \
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
