import Builder
import sys
import os


class LocalhostCanary(Builder.Action):

    def run(self, env):
        env.shell.setenv('AWS_CRT_MEMORY_TRACING', '2')
        actions = [
            "mvn install -DskipTests",
            "cd ./samples/HttpClientCanary && mvn install",
            "cd ./samples/HttpClientCanary && mvn exec:java -Dexec.mainClass=com.canary.SDKNettyClientCanary -Daws.crt.http.canary.uri=https://localhost:8443/echo",
            "cd ./samples/HttpClientCanary && mvn exec:java -Dexec.mainClass=com.canary.Http2StreamManagerCanary -Daws.crt.http.canary.uri=https://localhost:8443/echo",
            "cd ./samples/HttpClientCanary && mvn exec:java -Dexec.mainClass=com.canary.SDKNettyClientCanary "
            "-Daws.crt.http.canary.uri=https://localhost:8443/uploadTest "
            "-Daws.crt.http.canary.bodyLength=10 "
            "-Daws.crt.http.canary.nettyResultPath=netty_upload_body",
            "cd ./samples/HttpClientCanary && mvn exec:java -Dexec.mainClass=com.canary.Http2StreamManagerCanary -Daws.crt.http.canary.uri=https://localhost:8443/uploadTest -Daws.crt.http.canary.bodyLength=10 -Daws.crt.http.canary.nettyResultPath=netty_upload_body",
        ]

        return Builder.Script(actions, name='aws-crt-java-test')
