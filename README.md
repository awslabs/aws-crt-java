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

## Documentation
[Java CRT Documentation](https://awslabs.github.io/aws-crt-java/)

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

These can be set persistently via Maven settings (usually in ~/.m2/settings.xml):
```xml
<settings>
    ...
  <profiles>
    <profile>
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
        <properties>
            <crt.test.endpoint>XXXXXXXXXX-ats.iot.us-east-1.amazonaws.com</crt.test.endpoint>
            <crt.test.certificate>/path/to/XXXXXXXX-certificate.pem.crt</crt.test.certificate>
            <crt.test.privatekey>/path/to/XXXXXXXX-private.pem.key</crt.test.privatekey>
            <crt.test.rootca>/path/to/AmazonRootCA1.pem</crt.test.rootca>
            ... etc ...
        </properties>
    </profile>
  </profiles>
</settings>% 
```

## IDEs
* CMake is configured to export a compilation database at target/cmake-build/compile_commands.json
* CLion: Build once with maven, then import the project as a [Compilation Database Project](https://www.jetbrains.com/help/clion/compilation-database.html)
* VSCode: will detect that this is both a java project and if you have the CMake extension, you can point that at CMakeLists.txt and the compilation database

## Debugging
Tests can be debugged in Java/Kotlin via the built-in tooling in VSCode and IntelliJ. If you need to debug the native code, it's a bit trickier.

To debug native code with VSCode or CLion or any other IDE:
1. Find your ```mvn``` launch script(e.g. ```realpath $(which mvn)```) and pull the command line at the bottom from it. This changes between versions
   of maven, so it is difficult to give consistent directions.

   As an example, for Maven 3.6.0 on Linux:
   ```/path/to/java -classpath /usr/share/java/plexus-classworlds-2.5.2.jar -Dclassworlds.conf=/usr/share/maven/bin/m2.conf -Dmaven.home=/usr/share/maven -Dlibrary.jansi.path=/usr/share/maven/lib/jansi-native -Dmaven.multiModuleProjectDirectory=. org.codehaus.plexus.classworlds.launcher.Launcher test -DforkCount=0 -Ddebug.native -Dtest=HttpClientConnectionManager#testMaxParallelConnections```

   The important parts are:
    * ```-DforkCount=0``` - prevents the Maven process from forking to run tests, so your debugger will be attached to the right process. You can ignore this if
      you configure your debugger to attach to child processes.
    * ```-Ddebug.native``` - Makes CMake compile the JNI bindings and core libraries in debug. By default, we compile in release with symbols, which will help
      for call stacks, but less so for live debugging.
2. Set the executable to launch to be your java binary (e.g. ```/usr/bin/java```)
3. Set the parameters to be the ones used by the ```mvn``` script, as per above
4. Set the working directory to the aws-crt-java directory
5. On windows, you will need to manually load the PDB via the Modules window in Visual Studio, as it is not embedded in the JAR. It will be in the ```target/cmake-build/lib/windows/<arch>``` folder.
