cd ./android

GPG_KEY=$(cat /tmp/aws-sdk-common-runtime.key.asc)
echo $GPG_KEY

./gradlew -PsigningKey=$"$GPG_KEY" -PsigningPassword=$GPG_PASSPHRASE -PsonatypeUsername='aws-sdk-common-runtime' -PsonatypePassword=$ST_PASSWORD publishToAwsNexus