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

if os.name == 'nt':
    java_command[0] = 'mvn.cmd'

def run_command(args):
    # gather all stderr and stdout to a single string that we print only if things go wrong
    process = subprocess.Popen(
        args, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    timedout = False
    try:
        output = process.communicate(timeout=TIMEOUT)[0]
    except subprocess.TimeoutExpired:
        timedout = True
        process.kill()
        output = process.communicate()[0]
    finally:
        if process.returncode != 0 or timedout:
            args_str = subprocess.list2cmdline(args)
            for line in output.splitlines():
                print(line.decode())
            if timedout:
                raise RuntimeError("Timeout happened after {secs} secs from: {cmd}".format(
                    secs=TIMEOUT, cmd=args_str))
            else:
                raise RuntimeError("Return code {code} from: {cmd}".format(
                    code=process.returncode, cmd=args_str))


run_command(java_command)
