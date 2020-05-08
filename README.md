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

## Debugging
Tests can be debugged via the built-in tooling in VSCode and Intellij. If you need to debug the native code, it's a bit trickier.

To debug with VSCode or CLion or any other IDE:
1. Find your mvn launch script(e.g. ```realpath $(which mvn)```) and pull the command line at the bottom from it. This changes between versions
   of maven, so it is difficult to give consistent directions.

   As an example, for Maven 3.6.0 on Linux:
   ```/path/to/java -classpath /usr/share/java/plexus-classworlds-2.5.2.jar -Dclassworlds.conf=/usr/share/maven/bin/m2.conf -Dmaven.home=/usr/share/maven -Dlibrary.jansi.path=/usr/share/maven/lib/jansi-native -Dmaven.multiModuleProjectDirectory=. org.codehaus.plexus.classworlds.launcher.Launcher test -DforkCount=0 -Ddebug.native -Dtest=HttpClientConnectionManager#testMaxParallelConnections```

   The important parts are:
    * -DforkCount=0 - prevents the mvn process from forking to run tests, so your debugger will be attached to the right process. You can ignore this if
      you configure your debugger to attach to child processes
    * -Ddebug.native - Makes cmake compile the JNI bindings and core libraries in debug. By default, we compile in release with symbols, which will help
      for call stacks, but less so for live debugging
2. Set the executable to launch to be your java binary (e.g. /usr/bin/java)
3. Set the parameters to be the ones used by the mvn script, as per above
4. Set the working directory to the aws-crt-java directory
5. On windows, you will need to manually load the PDB via the Modules window in Visual Studio, as it is not embedded in the JAR.
