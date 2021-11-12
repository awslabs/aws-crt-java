#!/usr/bin/env bash
export JAVA_PROGRAM_ARGS=`echo "$@"`
mvn -e exec:java -Dexec.classpathScope="test" -Dexec.mainClass="software.amazon.awssdk.crt.test.Elasticurl" -Dexec.args="$JAVA_PROGRAM_ARGS"
