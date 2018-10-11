## AWS CRT Java

Java Bindings for the AWS Common Runtime

## License

This library is licensed under the Apache 2.0 License.

## Usage
### Building
aws-crt-java uses CMake for setting up build environments. This library depends on:
* aws-c-common
* aws-c-io
* s2n (on POSIX platforms)
* aws-c-mqtt
* aws-c-http

For example:

    git clone git@github.com:awslabs/aws-crt-java.git aws-crt-java
    mkdir aws-crt-java-build
    cd aws-crt-java-build
    cmake ../aws-crt-java
    make -j 12
    make test
    sudo make install

Keep in mind that CMake supports multiple build systems, so for each platform you can pass your own build system
as the `-G` option. For example:

    cmake -GNinja ../aws-crt-java
    ninja build
    ninja test
    sudo ninja install

Or on windows,

    cmake -G "Visual Studio 14 2015 Win64" ../aws-crt-java
    msbuild.exe ALL_BUILD.vcproj

### CMake Options
* -DCMAKE_CLANG_TIDY=/path/to/clang-tidy (or just clang-tidy or clang-tidy-7.0 if it is in your PATH) - Runs clang-tidy as part of your build.
* -DENABLE_SANITIZERS=ON - Enables gcc/clang sanitizers, by default this adds -fsanitizer=address,undefined to the compile flags for projects that call aws_add_sanitizers.
* -DCMAKE_INSTALL_PREFIX=/path/to/install - Standard way of installing to a user defined path. If specified when configuring aws-c-common, ensure the same prefix is specified when configuring other aws-c-* SDKs.

