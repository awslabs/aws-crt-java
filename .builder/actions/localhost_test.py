import Builder
import sys
import os


class LocalhostTest(Builder.Action):

    def run(self, env):
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')

        if not os.getenv('ENABLE_LOCALHOST_TESTS'):
            return

        if os.system("mvn test -DredirectTestOutputToFile=true -DforkCount=0 \
            -Daws.crt.memory.tracing=2 \
            -Daws.crt.debugnative=true \
            -Daws.crt.log.level=Debug \
            -Daws.crt.localhost=true"):
            # Failed
            exit(1)
