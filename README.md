## AWS CRT Java

Java Bindings for the AWS Common Runtime

## License

This library is licensed under the Apache 2.0 License.

## Usage
### Building
#### Linux
Requirements: 
* Must have a C compiler, clang3.9+ or gcc4+
* libssl-dev
* Build tools: cmake3, ninja
* Java: Any JDK8 or above, ensure JAVA_HOME is set
* Maven
1) apt-get install cmake3 libssl-dev ninja-build maven openjdk-8-jdk-headless -y
2) git clone https://github.com/awslabs/aws-crt-java.git
3) git clone https://github.com/awslabs/s2n.git
4) mkdir s2n-build && cd s2n-build
5) cmake -GNinja -DCMAKE_INSTALL_PREFIX=../aws-crt-java/build/deps/install
6) ninja && ninja install
7) cd ../aws-crt-java
8) ./build-deps.sh (insert any CMake flags here, such as -DCMAKE_BUILD_TYPE=Debug)
9) mvn compile

#### OSX
Requirements:
* Build tools: cmake3, ninja
* Java: Any JDK8 or above, ensure JAVA_HOME is set
* Maven
1) brew install maven cmake3 ninja
2) git clone https://github.com/awslabs/aws-crt-java.git
3) cd aws-crt-java
4) ./build-deps.sh (insert any CMake flags here, such as -DCMAKE_BUILD_TYPE=Debug)
5) mvn compile

#### Windows
Requirements:
* Visual Studio 2015 or above
* CMake 3
* Java: Any JDK8 or above, ensure JAVA_HOME is set
* Maven
1) choco install maven (if you have chocolatey installed)
2) git clone https://github.com/awslabs/aws-crt-java.git
3) cd aws-crt-java
4) build_deps.bat
5) mvn compile

### Testing
Once you've set up an IoT Thing [here](https://console.aws.amazon.com/iot), put the certificates into the src/test/resources/credentials directory. You should have:
* AmazonRootCA1.pem - Amazon Web Services root certificate
* <thingid>-certificate.pem.crt - Your Thing's cert
* <thingid>-private.pem.key - Your Thing's private key

If the certs are not found, then tests which connect to the IoT Core service will simply quietly be skipped
```mvn test``` will run all of the JUnit tests
