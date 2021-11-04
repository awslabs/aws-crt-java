
import Builder
import os


class AWSCrtJavaTest(Builder.Action):

    def run(self, env):
        # tests must run with leak detection turned on
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = []

        # if os.system("mvn -B test -DredirectTestOutputToFile=true -DforkCount=0 \
        #     -DrerunFailingTestsCount=5 -DskipAfterFailureCount=1 \
        #     -Daws.crt.memory.tracing=2 -Daws.crt.debugnative=true"):
        #     # Failed
        #     actions.append("exit 1")
        # os.system("cat log.txt")
        # Three level up of the currect file path .builder/actions/__FILE__
        source_path = os.path.dirname(
            os.path.dirname((os.path.dirname(__file__))))
        home_dir = os.path.expanduser("~")
        print(source_path)
        actions.append(
            ['java',
             '-classpath',
             '{source}/target/test-classes:{source}/target/classes:{home}/.m2/repository/commons-cli/commons-cli/1.4/commons-cli-1.4.jar'.format(
                 source=source_path, home=home_dir),
             'software.amazon.awssdk.crt.test.Elasticurl',
             '-v',
             'ERROR',
             '--http2',
             '-o',
             'elastigirl_h2.png',
             'https://d1cz66xoahf9cl.cloudfront.net/elastigirl.png'])

        return Builder.Script(actions, name='aws-crt-java-test')
