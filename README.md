## AWS CRT Java

Java Bindings for the AWS Common Runtime

## License

This library is licensed under the Apache 2.0 License.

## Building

### Linux/Unix
Requirements: 
* Clang 3.9+ or GCC 4.4+
* libssl-dev (on POSIX platforms)
* cmake 3.1+
* ninja
* Java: Any JDK8 or above, ensure JAVA_HOME is set
* Maven

Building:
1) apt-get install cmake3 libssl-dev ninja-build maven openjdk-8-jdk-headless -y
2) git clone https://github.com/awslabs/aws-crt-java.git
3) cd aws-crt-java
4) mvn compile

### OSX
Requirements:
* cmake 3.1
* ninja
* Java: Any JDK8 or above, ensure JAVA_HOME is set
* Maven
1) brew install maven cmake3 ninja (if you have homebrew installed, otherwise install these manually)
2) git clone https://github.com/awslabs/aws-crt-java.git
3) cd aws-crt-java
4) mvn compile

### Windows
Requirements:
* Visual Studio 2015 or above
* CMake 3
* Java: Any JDK8 or above, ensure JAVA_HOME is set
* Maven
1) choco install maven (if you have chocolatey installed), otherwise install maven manually
2) git clone https://github.com/awslabs/aws-crt-java.git
3) cd aws-crt-java
4) mvn compile

## Installing
From the aws-crt-java directory:
```mvn install:install-file -Dfile=target/aws-crt-java-1.0.jar -DpomFile=pom.xml```

## Testing
Once you've set up an IoT Thing [here](https://console.aws.amazon.com/iot), put the certificates into the 
src/test/resources/credentials directory. You should have:
* AmazonRootCA1.pem - Amazon Web Services root certificate
* <thingid>-certificate.pem.crt - Your Thing's cert
* <thingid>-private.pem.key - Your Thing's private key

If the certs are not found, then tests which connect to the IoT Core service will simply quietly be skipped
```mvn test``` will run all of the JUnit tests
