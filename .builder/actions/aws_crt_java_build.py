
import Builder
import os
import argparse


class AWSCrtJavaBuild(Builder.Action):

    def run(self, env):
        if os.getenv("CRT_FIPS") is not None:
            env.shell.exec("mvn", "-P", "continuous-integration", "-B", "compile",
                           "-Dcmake.crt_fips=ON", check=True)
        else:
            env.shell.exec("mvn", "-P", "continuous-integration",
                           "-B", "compile", check=True)

        # allow custom python to be used
        parser = argparse.ArgumentParser()
        parser.add_argument('--classifier')
        args = parser.parse_known_args(env.args.args)[0]
        if args.classifier:
            env.shell.exec("mvn", "-B", "install", "-DskipTests", "-Dshared-lib.skip=true",
                           f"-Dcrt.classifier={args.classifier}", check=True)
