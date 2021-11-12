import sys
import os
import subprocess
import shlex
import subprocess

TIMEOUT = 100
# Runner for elasticurl integration tests

mvn_args = " ".join(map(shlex.quote, sys.argv[1:]))

java_command = ['mvn', '-e', 'exec:java', '-Dexec.classpathScope=\"test\"',
                '-Dexec.mainClass=\"software.amazon.awssdk.crt.test.Elasticurl\"', '-Dexec.args=\"{}\"'.format(mvn_args)]
print(java_command)
if os.name == 'nt':
    java_command[0] = 'mvn.cmd'
command_string = " ".join(java_command)


def run_command(args_str):
    print(args_str)
    # gather all stderr and stdout to a single string that we print only if things go wrong
    process = subprocess.Popen(
        args_str, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=True)
    timedout = False
    try:
        output = process.communicate(timeout=TIMEOUT)[0]
    except subprocess.TimeoutExpired:
        timedout = True
        process.kill()
        output = process.communicate()[0]
    finally:
        if process.returncode != 0 or timedout:
            print(args_str)
            for line in output.splitlines():
                print(line.decode())
            if timedout:
                raise RuntimeError("Timeout happened after {secs} secs from: {cmd}".format(
                    secs=TIMEOUT, cmd=args_str))
            else:
                raise RuntimeError("Return code {code} from: {cmd}".format(
                    code=process.returncode, cmd=args_str))


run_command(command_string)
