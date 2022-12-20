mvn -X gpg:sign-and-deploy-file --settings /local/home/dengket/.m2/settings.xml -DstagingRepositoryId=softwareamazon-4516 \
    -Dgpg.passphrase=$GPG_PASSPHRASE \
    -DgroupId=software.amazon.awssdk.crt -DartifactId=aws-crt -Dpackaging=jar \
    -Dversion=0.20.3-5-g90472fb642 \
    -Dfile=./target/aws-crt-0.20.3-5-g90472fb642.jar \
    -Dfiles=./target/aws-crt-0.20.3-5-g90472fb6-SNAPSHOT-mac-x86_64.jar \
    -Dclassifiers=mac-x86_64 \
    -Dtypes=jar \
    -DpomFile=pom.xml \
    -DrepositoryId=ossrh -Durl=https://aws.oss.sonatype.org:443/service/local/staging/deployByRepositoryId/softwareamazon-4516




mvn -X deploy:deploy-file -e --settings /local/home/dengket/.m2/settings.xml \
    -Dfile=./target/aws-crt-0.20.3-5-g90472fb642.jar \
    -DgroupId=software.amazon.awssdk.crt -DartifactId=aws-crt \
    -Dversion=0.20.3-5-g90472fb642-SNAPSHOT \
    -Dfiles=./target/aws-crt-0.20.3-5-g90472fb6-SNAPSHOT-mac-x86_64.jar \
    -Dclassifiers=mac-x86_64 \
    -Dtypes=jar \
    -DrepositoryId=ossrh -Durl=https://aws.oss.sonatype.org/content/repositories/snapshots | tee snapshots_failed.log
