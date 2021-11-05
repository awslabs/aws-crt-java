import sys
import os

# Runner for elasticurl integration tests
elasticurl_args = sys.argv[1:]

java_command = "mvn -e exec:java -Dexec.classpathScope=\"test\" -Dexec.mainClass=\"software.amazon.awssdk.crt.test.Elasticurl\" -Dexec.args=\"{args}\"".format(
    args=" ".join(elasticurl_args))

os.system(java_command)
