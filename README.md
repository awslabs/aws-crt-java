## AWS CRT Java

Java Bindings for the AWS Common Runtime

## License

This library is licensed under the Apache 2.0 License.

## Building

### Linux/Unix
Requirements: 
* Clang 3.9+ or GCC 4.4+
* cmake 3.1+
* Java: Any JDK8 or above, ensure JAVA_HOME is set
* Maven

Building:
1) apt-get install cmake3 maven openjdk-8-jdk-headless -y
2) git clone https://github.com/awslabs/aws-crt-java.git
3) cd aws-crt-java
4) mvn compile

### OSX
Requirements:
* cmake 3.1
* ninja
* Java: Any JDK8 or above, ensure JAVA_HOME is set
* Maven
1) brew install maven cmake3 (if you have homebrew installed, otherwise install these manually)
2) git clone https://github.com/awslabs/aws-crt-java.git
3) cd aws-crt-java
4) mvn compile

### Windows
Requirements:
* Visual Studio 2015 or above
* CMake 3.1
* Java: Any JDK8 or above, ensure JAVA_HOME is set
* Maven
1) choco install maven (if you have chocolatey installed), otherwise install maven and the JDK manually
2) git clone https://github.com/awslabs/aws-crt-java.git
3) cd aws-crt-java
4) mvn compile

## Installing
From the aws-crt-java directory:
```mvn install```
From maven: (https://search.maven.org/artifact/software.amazon.awssdk.crt/aws-crt/)

## Testing
Once you've set up an IoT Thing [here](https://console.aws.amazon.com/iot), put the certificates into the 
src/test/resources/credentials directory. You should have:
* AmazonRootCA1.pem - Amazon Web Services root certificate
* <thingid>-certificate.pem.crt - Your Thing's cert
* <thingid>-private.pem.key - Your Thing's private key

If the certs are not found, then tests which connect to the IoT Core service will simply quietly be skipped
```mvn test``` will run all of the JUnit tests

If you are running the tests in a constrained environment where network access is not guaranteed/allowed,
the network tests can be disabled by setting the Java system property NETWORK_TESTS_DISABLED. You can do
this on the command line with ```mvn test -DargLine="-DNETWORK_TESTS_DISABLED=true"```.
