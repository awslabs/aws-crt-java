## AWS CRT Java

Java Bindings for the AWS Common Runtime

## License

This library is licensed under the Apache 2.0 License.

*__Jump To:__*

* [Platform Specific Building](#platform)
    * [Linux/Unix](#linuxunix)
    * [OSX](#osx)
    * [Windows](#windows)
    * [Android](./android/ANDROID.md)
* [Java CRT Documentation](https://awslabs.github.io/aws-crt-java/)
* [Installing](#installing)
* [Platform-Specific JARs](#platform-specific-jars)
* [FIPS Compliance](#fips-compliance)
* [GraalVM support](#graalvm-support)
* [System Properties](#system-properties)
* [TLS Behavior](#tls-behavior)
* [Testing](#testing)
* [IDEs](#ides)
* [Debugging](#debugging)


## Platform

### Linux/Unix
Requirements:
* Clang 3.9+ or GCC 4.4+
* cmake 3.9+
* Java: Any JDK8 or above, ensure `JAVA_HOME` is set
* Maven

Building:
1) `apt-get install cmake3 maven openjdk-8-jdk-headless -y`
2) `git clone https://github.com/awslabs/aws-crt-java.git`
3) `cd aws-crt-java`
4) `git submodule update --init --recursive`
5) `mvn compile`

### OSX
Requirements:
* cmake 3.9+
* ninja
* Java: Any JDK8 or above, ensure `JAVA_HOME` is set
* Maven

Building:
1) `brew install maven cmake` (if you have homebrew installed, otherwise install these manually)
2) `git clone https://github.com/awslabs/aws-crt-java.git`
3) `cd aws-crt-java`
4) `git submodule update --init --recursive`
5) `mvn compile`

### Windows
Requirements:
* Visual Studio 2015 or above
* CMake 3.9+
* Java: Any JDK8 or above, ensure `JAVA_HOME` is set
* Maven

Building:
1) `choco install maven` (if you have chocolatey installed), otherwise install maven and the JDK manually
2) `git clone https://github.com/awslabs/aws-crt-java.git`
3) `cd aws-crt-java`
4) `git submodule update --init --recursive`
5) `mvn compile`

NOTE: Make sure you run this from a VS Command Prompt or have run `VCVARSALL.BAT` in your current shell so
CMake can find Visual Studio.

## Documentation
[Java CRT Documentation](https://awslabs.github.io/aws-crt-java/)

## Installing
From the aws-crt-java directory:
```mvn install```
From maven: (https://search.maven.org/artifact/software.amazon.awssdk.crt/aws-crt/)

## Platform-Specific JARs

The `aws-crt` JAR in Maven Central is a large "uber" jar that contains compiled C libraries for many different platforms (Windows, Linux, etc). If size is an issue, you can pick a smaller platform-specific JAR by setting the `<classifier>`.

Sample to use classifier from aws-crt:
``` xml
        <!-- Platform-specific Linux x86_64 JAR -->
        <dependency>
            <groupId>software.amazon.awssdk.crt</groupId>
            <artifactId>aws-crt</artifactId>
            <version>0.20.5</version>
            <classifier>linux-x86_64</classifier>
        </dependency>
```

``` xml
        <!-- "Uber" JAR that works on all platforms -->
        <dependency>
            <groupId>software.amazon.awssdk.crt</groupId>
            <artifactId>aws-crt</artifactId>
            <version>0.20.5</version>
        </dependency>
```

### Available classifiers

- linux-armv6 (no auto-detect)
- linux-armv7 (no auto-detect)
- linux-aarch_64
- linux-x86_32
- linux-x86_64
- linux-x86_64-musl (no auto-detect)
- linux-armv7-musl (no auto-detect)
- linux-aarch_64-musl (no auto-detect)
- osx-aarch_64
- osx-x86_64
- windows-x86_32
- windows-x86_64
- fips-where-available (no auto-detect)

### Auto-detect

The [os-maven-plugin](https://github.com/trustin/os-maven-plugin) can automatically detect your platform's classifier at build time.

**NOTES**: The auto-detected `linux-arm_32` platform classifier is not supported, you must specify `linux-armv6` or `linux-armv7`.
Additionally, musl vs glibc detection is not supported either.  If you are deploying to a musl-based system and wish to use
a classifier-based jar, you must specify the classifier name yourself.

``` xml
<build>
        <extensions>
            <!-- Generate os.detected.classifier property -->
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.7.0</version>
            </extension>
        </extensions>
 </build>

 <dependencies>
        <dependency>
            <groupId>software.amazon.awssdk.crt</groupId>
            <artifactId>aws-crt</artifactId>
            <version>0.20.5</version>
            <classifier>${os.detected.classifier}</classifier>
        </dependency>
  <dependencies>
```

## FIPS Compliance

Currently the classifier `fips-where-available` provides an "uber" jar with FIPS compliance on *some platforms*.

Platforms without FIPS compliance are also included in this jar, for compatibility's sake. Check `CRT.isFIPS()` at runtime to ensure you are on a FIPS compliant platform. The current breakdown is:
* **FIPS compliant**: linux-aarch_64, linux-x86_64
* **NOT compliant**: linux-armv6, linux-armv7, linux-armv7-musl, linux-aarch_64-musl, linux-x86_32, linux-x86_64-musl, osx-aarch_64, osx-x86_64, windows-x86_32, windows-x86_64

> [!WARNING]
> The classifier, and platforms with FIPS compliance are subject to change in the future.

Platforms with FIPS compliance use [AWS-LC](https://github.com/aws/aws-lc) as their cryptographic module ([NIST Certificate #4816](https://csrc.nist.gov/projects/cryptographic-module-validation-program/certificate/4816)).

## GraalVM support

Since version v0.29.20, GraalVM native image was supported. You can compile your application with AWS CRT in a GraalVM native image project without any additional configuration.

Since version v0.31.1, GraalVM support was updated. Instead of packaging the JNI shared libraries with native image as resources, the corresponding shared lib will be written to the same directory as the native image.
In this way, it reduces the native image size around 30% (142 MB to 101 MB for a sample application), and avoids the extra loading time needed for extracting the JNI lib to the temporary path for load. No additional configuration needed.
> [!NOTE]
> The JNI shared lib must be in the same directory as the GraalVM native image. If you move the native image, you must move this file too. It is `aws-crt-jni.dll` on Windows, `libaws-crt-jni.dylib` on macOS, and `libaws-crt-jni.so` on Unix.

## System Properties

- To enable logging, set `aws.crt.log.destination` or `aws.crt.log.level`:
    - `aws.crt.log.level` - Log level. May be: "None", "Fatal", "Error", "Warn" (default), "Info", "Debug", "Trace".
    - `aws.crt.log.destination` - Log destination. May be: "Stderr" (default), "Stdout", "File", "None".
    - `aws.crt.log.filename` - File to use when `aws.crt.log.destination` is "File".
- `aws.crt.libc` - (Linux only) Set to "musl" or "glibc" if CRT cannot properly detect which to use.
- `aws.crt.lib.dir` - Set directory where CRT may extract its native library (by default, `java.io.tmpdir` is used)
- `aws.crt.memory.tracing` - May be: "0" (default, no tracing), "1" (track bytes), "2" (more detail).
    Allows the CRT.nativeMemory() and CRT.dumpNativeMemory() functions to report native memory usage.

## TLS Behavior

The CRT uses native libraries for TLS, rather than Java's typical
Secure Socket Extension (JSSE), KeyStore, and TrustStore.
On [Windows](https://learn.microsoft.com/en-us/windows/win32/security) and
[Apple](https://developer.apple.com/documentation/security) devices,
the built-in OS libraries are used.
On Linux/Unix/etc [s2n-tls](https://github.com/aws/s2n-tls) is used.

If you need to add certificates to the trust store, add them to your OS trust store.
The CRT does not use the Java TrustStore. For more customization options, see
[TlsContextOptions](https://awslabs.github.io/aws-crt-java/software/amazon/awssdk/crt/io/TlsContextOptions.html) and
[TlsConnectionOptions](https://awslabs.github.io/aws-crt-java/software/amazon/awssdk/crt/io/TlsConnectionOptions.html).

### Mac-Only TLS Behavior

Please note that on Mac, once a private key is used with a certificate, that certificate-key pair is imported into the Mac Keychain. All subsequent uses of that certificate will use the stored private key and ignore anything passed in programmatically.  Beginning in v0.6.6, when a stored private key from the Keychain is used, the following will be logged at the "info" log level:

```
static: certificate has an existing certificate-key pair that was previously imported into the Keychain.  Using key from Keychain instead of the one provided.
```

## Testing

Some tests require pre-configured resources and proper environment variables to be set to run properly.
These tests will be quietly skipped if the environment variables they require are not set.

### IoT tests

Many IoT related tests require that you have [set up](https://console.aws.amazon.com/iot) an AWS IoT Thing.

* Some required environment variables
  * `AWS_TEST_MQTT311_IOT_CORE_HOST`: AWS IoT service endpoint hostname for MQTT3
  * `AWS_TEST_MQTT311_IOT_CORE_RSA_CERT`: Path to the IoT thing certificate for MQTT3
  * `AWS_TEST_MQTT311_IOT_CORE_RSA_KEY`: Path to the IoT thing private key for MQTT3
  * `AWS_TEST_MQTT311_IOT_CORE_ECC_CERT`: Path to the IoT thing with EC-based certificate for MQTT3
  * `AWS_TEST_MQTT311_IOT_CORE_ECC_KEY`: Path to the IoT thing with ECC private key for MQTT3 (The ECC key file should only contains the ECC Private Key section to working on MacOS.)
  * `AWS_TEST_MQTT311_ROOT_CA`: Path to the root certificate

Other Environment Variables that can be set can be found in the `SetupTestProperties()` function in [CrtTestFixture.java](https://github.com/awslabs/aws-crt-java/blob/main/src/test/java/software/amazon/awssdk/crt/test/CrtTestFixture.java)

These can be set persistently via Maven settings (usually in `~/.m2/settings.xml`):
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

### Proxy Tests

Most of proxy related tests need pre-configured Proxy host to run the tests properly.

* Required environment variables:
  * `AWS_TEST_HTTP_PROXY_HOST`: Hostname of proxy
  * `AWS_TEST_HTTP_PROXY_PORT`: Port of proxy
  * `NETWORK_TESTS_DISABLED`: Set this if tests are running in a constrained environment where network access is not guaranteed/allowed.

### S3 Tests

Most of S3 related tests require AWS credentials and a set of pre-configured S3 buckets. There is a helper script from aws-c-s3 that can be used to set up the test environment, [here](https://github.com/awslabs/aws-c-s3/tree/main/tests/test_helper).

Example to use the helper and run the S3 tests:

``` sh
cd aws-crt-java
python3 -m pip install boto3
export CRT_S3_TEST_BUCKET_NAME=<bucket_name>
python3 crt/aws-c-s3/tests/test_helper/test_helper.py init
# Run S3ClientTest. eg: mvn -Dtest=S3ClientTest test
```

more details about the helper can be found from [here](https://github.com/awslabs/aws-c-s3/blob/main/tests/test_helper/README.md).

* Required environment variable:
  * `CRT_S3_TEST_BUCKET_NAME`: The basic bucket name for S3 tests.

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
4. Set the working directory to the `aws-crt-java` directory
5. On windows, you will need to manually load the PDB via the Modules window in Visual Studio, as it is not embedded in the JAR. It will be in the ```target/cmake-build/lib/windows/<arch>``` folder.
