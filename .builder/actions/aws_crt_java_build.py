
import Builder
import os
import argparse


class AWSCrtJavaBuild(Builder.Action):

    def run(self, env):
        if os.getenv("CRT_FIPS") is not None:
            env.shell.exec("mvn", "-Dmaven.wagon.http.retryHandler.class=standard", "-Dmaven.wagon.http.retryHandler.count=3", "-Dmaven.wagon.http.pool=false", "-P", "continuous-integration", "-B", "compile",
                           "-Dcmake.crt_fips=ON", check=True)
        else:
            env.shell.exec("mvn", "-Dmaven.wagon.http.retryHandler.class=standard", "-Dmaven.wagon.http.retryHandler.count=3", "-Dmaven.wagon.http.pool=false", "-P", "continuous-integration",
                           "-B", "compile", check=True)

        parser = argparse.ArgumentParser()
        parser.add_argument('--classifier')
        args = parser.parse_known_args(env.args.args)[0]
        if args.classifier:
            env.shell.exec("mvn", "-B", "install", "-DskipTests", "-Dshared-lib.skip=true",
                           "-Dmaven.wagon.http.retryHandler.class=standard","-Dmaven.wagon.http.retryHandler.count=3", "-Dmaven.wagon.http.pool=false",
                           f"-Dcrt.classifier={args.classifier}", check=True)
