
import Builder
import os
import argparse

# This action is used by the musl-linux-build.sh script to build the binaries for release.


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
        parser.add_argument('--calssifier')
        args = parser.parse_known_args(env.args.args)[0]
        if args.calssifier:
            env.shell.exec("mvn", "-B", "install", "-DskipTests", "-Dshared-lib.skip=true",
                           f"-Dcrt.classifier={args.calssifier}", check=True)
