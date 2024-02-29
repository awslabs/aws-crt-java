
import Builder
import os
import argparse

# This action is used by the musl-linux-build.sh script to build the binaries for release.


class AWSCrtJavaBuild(Builder.Action):

    def run(self, env):
        # allow custom python to be used
        parser = argparse.ArgumentParser()
        parser.add_argument('--fips')
        args = parser.parse_known_args(env.args.args)[0]
        fips_build = True if args.fips else False
        if fips_build:
            env.shell.exec("mvn", "-P", "continuous-integration", "-B", "compile",
                           "-Dcrt.classifier=ON", check=True)
        else:
            env.shell.exec("mvn", "-P", "continuous-integration",
                           "-B", "compile", check=True)
