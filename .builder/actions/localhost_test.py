import Builder
import sys
import os


class LocalhostTest(Builder.Action):

    def run(self, env):
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = ["mvn -Dtest=Http2ClientLocalHostTest test -DredirectTestOutputToFile=true -DforkCount=0 \
            -DrerunFailingTestsCount=5 \
            -Daws.crt.memory.tracing=2 \
            -Daws.crt.debugnative=true \
            -Daws.crt.log.level=Error \
            -Daws.crt.localhost=true"]
        return Builder.Script(actions, name='aws-crt-java-test')
