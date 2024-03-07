
import Builder
import os

# This action is used by the musl-linux-build.sh script to build the binaries for release.


class AWSCrtJavaBuild(Builder.Action):

    def run(self, env):
        if os.getenv("CRT_FIPS") is not None:
            env.shell.exec("mvn", "-P", "continuous-integration", "-B", "compile",
                           "-Dcmake.crt_fips=ON", check=True)
        else:
            env.shell.exec("mvn", "-P", "continuous-integration",
                           "-B", "compile", check=True)
