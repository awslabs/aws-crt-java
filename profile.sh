#!/bin/bash

set -e

if [ -z "$TAG" ]; then
    TAG=default
fi

if [ -z "$WARMUP" ]; then
    WARMUP=30
fi

if [ -z "$DURATION" ]; then
    DURATION=30
fi

# enable perf
sudo sysctl kernel.perf_event_paranoid=0
mvn test -Dlibcrypto.path=/opt/openssl -DforkCount=0 -Dtest="S3ClientTest#benchmarkS3Get" -Daws.crt.s3.benchmark=1 $* 2>&1 > benchmark.log &
pid=
while [ -z "$pid" ]; do
    sleep 1
    pid=$(ps ax | grep java | grep -v grep | sed -e 's/^ *//' | cut -f1 -d" ")
done

echo "Benchmark running as pid $pid"
sleep $WARMUP
timestamp=$(date +%s)
svg=java-$TAG-$timestamp.svg
~/async-profiler/profiler.sh -d $DURATION -f $svg $pid
aws s3 cp $svg s3://aws-crt-test-stuff/profiling/
echo "Waiting for benchmark to finish..."
wait
grep --after-context=5 "successful transfers" benchmark.log
aws s3 cp benchmark.log s3://aws-crt-test-stuff/profiling/java-$TAG-$timestamp.log
aws s3 cp samples.csv s3://aws-crt-test-stuff/profiling/java-$TAG-$timestamp.csv
