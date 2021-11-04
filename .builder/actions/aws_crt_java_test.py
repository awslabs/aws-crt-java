
import Builder
import os


class AWSCrtJavaTest(Builder.Action):

    def run(self, env):
        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = []

        if os.system("mvn -B test -DredirectTestOutputToFile=true -DforkCount=0 \
            -DrerunFailingTestsCount=5 -DskipAfterFailureCount=1 \
            -Daws.crt.memory.tracing=2 -Daws.crt.debugnative=true"):
            # Failed
            actions.append("exit 1")
        os.system("cat log.txt")
        actions.append(
            ['java',
             '-classpath',
             '\{source_dir\}/target/test-classes:\{source_dir\}/target/classes:~/.m2/repository/commons-cli/commons-cli/1.4/commons-cli-1.4.jar',
             'software.amazon.awssdk.crt.test.Elasticurl',
             '-v',
             'TRACE',
             '--http2',
             '-o',
             'elastigirl_h2.png',
             'https://d1cz66xoahf9cl.cloudfront.net/elastigirl.png'])

        return Builder.Script(actions, name='aws-crt-java-test')
