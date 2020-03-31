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
2) git clone --recursive https://github.com/awslabs/aws-crt-java.git
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

NOTE: Make sure you run this from a VS Command Prompt or have run VCVARSALL.BAT in your current shell so
CMake can find Visual Studio.

## Installing
From the aws-crt-java directory:
```mvn install```
From maven: (https://search.maven.org/artifact/software.amazon.awssdk.crt/aws-crt/)

## Testing
Many tests require custom arguments. These tests will be quietly skipped if their arguments are not set.
Arguments can be passed like so:
```
mvn test -Dcertificate=path/to/cert -Dprivatekey=path/to/key ...
```
Many tests require that you have [set up](https://console.aws.amazon.com/iot) an AWS IoT Thing.

Full list of test arguments:
- endpoint: AWS IoT service endpoint hostname
- certificate: Path to the IoT thing certificate
- privatekey: Path to the IoT thing private key
- rootca: Path to the root certificate
- proxyhost: Hostname of proxy
- proxyport: Port of proxy
- NETWORK_TESTS_DISABLED: Set this if tests are running in a constrained environment where network access is not guaranteed/allowed.
