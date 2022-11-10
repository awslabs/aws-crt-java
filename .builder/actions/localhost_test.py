import Builder
import sys
import os


class LocalhostTest(Builder.Action):

    def run(self, env):
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = []
        if os.system("mvn -Dtest=Http2ClientLocalHostTest test -DredirectTestOutputToFile=true -DforkCount=0 \
            -Daws.crt.memory.tracing=2 \
            -Daws.crt.debugnative=true \
            -Daws.crt.log.level=Trace \
            -Daws.crt.log.destination=File \
            -Daws.crt.log.filename=trace.log \
            -Daws.crt.localhost=true"):
            # Failed
            os.system("cat trace.log")
            actions.append("exit 1")

        return Builder.Script(actions, name='aws-crt-java-test')
