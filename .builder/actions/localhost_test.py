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
            -Daws.crt.log.level=Debug \
            -Daws.crt.localhost=true"):
            # Failed
            actions.append("exit 1")
    
        if os.system("mvn -Dtest=Http2RequestResponseTest test -DredirectTestOutputToFile=true -DforkCount=0 \
            -Daws.crt.memory.tracing=2 \
            -Daws.crt.debugnative=true \
            -Daws.crt.log.level=Debug \
            -Daws.crt.localhost=true"):
            # Failed
            actions.append("exit 1")

        if os.system("mvn -Dtest=HttpRequestResponseTest test -DredirectTestOutputToFile=true -DforkCount=0 \
            -Daws.crt.memory.tracing=2 \
            -Daws.crt.debugnative=true \
            -Daws.crt.log.level=Debug \
            -Daws.crt.localhost=true"):
            # Failed
            actions.append("exit 1")
