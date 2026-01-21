# AWS CRT Android

This document provides information about building and using the AWS CRT Java with Android.

If you have any issues or feature requests, please file an issue or pull request.

API documentation: https://awslabs.github.io/aws-crt-java/

This SDK is built on the AWS Common Runtime, a collection of libraries
([aws-c-common](https://github.com/awslabs/aws-c-common),
[aws-c-io](https://github.com/awslabs/aws-c-io),
[aws-c-mqtt](https://github.com/awslabs/aws-c-mqtt),
[aws-c-http](https://github.com/awslabs/aws-c-http),
[aws-c-cal](https://github.com/awslabs/aws-c-cal),
[aws-c-auth](https://github.com/awslabs/aws-c-auth),
[s2n](https://github.com/awslabs/s2n)...) written in C to be
cross-platform, high-performance, secure, and reliable.

*__Jump To:__*

* [Installation](#installation)
  * [Minimum requirements](#minimum-requirements)
  * [Build and install CRT from source](#build-and-install-crt-from-source)
* [Consuming AWS CRT Android](#consuming-aws-crt-android)
  * [Consuming from Maven](#consuming-from-maven)
  * [Consuming from locally installed](#consuming-from-locally-installed)

## Installation

### Minimum requirements
* Java 17+ ([Download and Install Java](https://www.java.com/en/download/help/download_options.html))
  * [Set JAVA_HOME](#set-java_home)
* Gradle 8.5.1+ ([Download and Install Gradle](https://gradle.org/install/))
* Android SDK 24 ([Doanload SDK Manager](https://developer.android.com/tools/releases/platform-tools#downloads))
  * [Set ANDROID_HOME](#set-android_home)
* Android NDK 28+ ([Download and install Android NDK](https://developer.android.com/ndk/downloads))

### Build and install CRT from source
Supports API 24 or newer.

``` sh
# Create a workspace directory to hold all the SDK files
mkdir sdk-workspace
cd sdk-workspace
# Clone the CRT repository
# (Use the latest version of the CRT here instead of "v0.27.6)
git clone --branch v0.27.6 --recurse-submodules https://github.com/awslabs/aws-crt-java.git
# Compile and install the CRT for Android
./gradlew :android:crt:build
# Install CRT locally
./gradlew :android:crt:publishToMavenLocal
```

## Consuming AWS CRT Android

### Consuming from Maven
Consuming this CRT via Maven is the preferred method of consuming it and using it within your application. To consume
AWS CRT Android in your application, add the following to your `build.gradle` repositories and dependencies:

``` groovy
repositories {
    mavenCentral()
}

dependencies {
    api 'software.amazon.awssdk.crt:aws-crt-android:0.27.6'
}
```
Replace `0.27.6` in `software.amazon.awssdk.crt:aws-crt-android:0.27.6` with the latest release version of the CRT library.
Look up the latest SDK version here: https://github.com/awslabs/aws-crt-java/releases

### Consuming from locally installed
You may also consume AWS CRT Android in your application using a locally installed version by adding the
following to your `build.gradle` repositories and depenencies:
``` groovy
repositories {
    mavenLocal()
}

dependencies {
    api 'software.amazon.awssdk.crt:aws-crt-android:0.27.6'
}
```
Replace `0.27.6` in `software.amazon.awssdk.crt:aws-crt-android:0.27.6` with the latest release version for the SDK
or replace with `1.0.0-SNAPSHOT` to use the CRT built and installed from source.


## Set JAVA_HOME

Below are instructions on how you can set `JAVA_HOME`, which varies from depending on whether you are on Windows or are on MacOS/Linux:

### Windows
1. Open "Edit the system environment variable"
2. Click "New" to create new environment variable
   - variable name: `JAVA_HOME`
   - variable value: `<jdk_install_path>` (example: `C:\Program Files\Java\jdk-17.0.2`)
3. Press "Ok" to save the changes
4. re-open the command prompt for the environment variables to apply

### MacOS and Linux
Run the following command to set the JAVA_PATH
``` sh
# (example: "/Library/Java/JavaVirtualMachines/jdk-10.jdk/Contents/Home")
export JAVA_HOME=<jdk_install_path>
```

## Set ANDROID_HOME
Below are instructions on how you can set `ANDROID_HOME`, which varies from depending on whether you are on Windows or are on MacOS/Linux:

### Windows
1. Open "Edit the system environment variable"
2. Click "New" to create new environment variable
   - variable name: `ANDROID_HOME`
   - variable value: `<android_sdk_path>` (example: `C:\Users\YourUsername\AppData\Local\Android\Sdk`)
3. Press "Ok" to save the changes
4. re-open the command prompt for the environment variables to apply

### MacOS and Linux
Run the following command to set the JAVA_PATH
``` sh
# (example: "/Users/YourUsername/Library/Android/sdk")
export ANDROID_HOME=<android_sdk_path>
```