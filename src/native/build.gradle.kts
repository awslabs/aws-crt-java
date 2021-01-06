
plugins {
    `c`
}

description = "JNI bindings for the AWS Common Runtime"

buildDir = File("../../build")

var libcryptoPath : String? = null

var buildType = "RelWithDebInfo"
if (project.hasProperty("buildType")) {
    buildType = project.property("buildType").toString()
    logger.info("Using custom build type: ${buildType}")
}

val cmakeConfigure = tasks.register("cmakeConfigure") {
    var cmakeArgs = listOf(
        "-B${buildDir}/cmake-build",
        "-H${projectDir}/../../",
        "-DCMAKE_BUILD_TYPE=${buildType}",
        "-DCMAKE_INSTALL_PREFIX=${buildDir}/cmake-build",
        "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON",
        "-DBUILD_DEPS=ON",
        "-DBUILD_TESTING=OFF"
    )

    if (org.gradle.internal.os.OperatingSystem.current().isLinux()) {
        libcryptoPath = null;
        // To set this, add -PlibcryptoPath=/path/to/openssl/home on the command line
        if (project.hasProperty("libcryptoPath")) {
            libcryptoPath = project.property("libcryptoPath").toString()
            logger.info("Using project libcrypto path: ${libcryptoPath}")
        }
    }

    if (libcryptoPath != null) {
        cmakeArgs += listOf(
            "-DLibCrypto_INCLUDE_DIR=${libcryptoPath}/include",
            "-DLibCrypto_STATIC_LIBRARY=${libcryptoPath}/lib/libcrypto.a"
        )
    }

    inputs.file("../../CMakeLists.txt")
    outputs.file("${buildDir}/cmake-build/CMakeCache.txt")

    doLast {
        val argsStr = cmakeArgs.joinToString(separator=" ")
        logger.info("cmake ${argsStr}")
        exec {
            executable("cmake")
            args(cmakeArgs)
            environment(mapOf<String, String>("JAVA_HOME" to System.getProperty("java.home")))
        }
    }
}

val cmakeBuild = tasks.register("cmakeBuild") {
    dependsOn(cmakeConfigure)
    inputs.file("../../CMakeLists.txt")
    inputs.file("${buildDir}/cmake-build/CMakeCache.txt")
    inputs.files(fileTree(".").matching {
        include(listOf("**/*.c", "**/*.h"))
    })
    inputs.files(fileTree("../../crt").matching {
        include(listOf("**/CMakeLists.txt", "**/*.c", "**/*.h"))
    })
    outputs.file("${buildDir}/cmake-build/lib/libaws-crt-jni.so")
    outputs.upToDateWhen { false }  //shared lib doesn't seem to get placed in jar without this

    var cmakeArgs = listOf(
        "--build", "${buildDir}/cmake-build",
        "--target", "all"
    )

    doLast {
        val argsStr = cmakeArgs.joinToString(separator=" ")
        logger.info("cmake ${argsStr}")
        exec {
            executable("cmake")
            args(cmakeArgs)
        }
    }
}

tasks.assemble {
    dependsOn(cmakeBuild)
}
