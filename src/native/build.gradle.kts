/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 *
 * Helpful resources/examples:
 * http://gradle.monochromeroad.com/docs/userguide/nativeBinaries.html
 * https://github.com/NationalSecurityAgency/ghidra/blob/master/Ghidra/Features/Decompiler/build.gradle
 */

plugins {
    `c`
    `java`
}

val rootDir = "${projectDir}/../.."
// Use the same buildDir as the root project to make packaging sane
buildDir = rootProject.buildDir

var libcryptoPath : String? = null

var buildType = "RelWithDebInfo"
if (project.hasProperty("buildType")) {
    buildType = project.property("buildType").toString()
    logger.info("Using custom build type: ${buildType}")
}

fun getHostOs(): String {
    val os = org.gradle.internal.os.OperatingSystem.current().toString().toLowerCase()
    return os.split(" ").first()
}

fun getHostArch(): String {
    val arch = System.getProperty("os.arch")
    when (arch) {
        in listOf("x86_64", "amd64") -> return "x86_64"
        in listOf("x86", "i386", "i586", "i686") -> return "x86"
        else -> throw GradleException("Can't normalize host's CPU architecture: '${arch}'")
    }
}

val targetOs = getHostOs()
val targetArch = getHostArch()

println("Target: OS=${targetOs} ARCH=${targetArch}")

val cmakeConfigure = tasks.register("cmakeConfigure") {
    var cmakeArgs = listOf(
        "-B${buildDir}/cmake-build",
        "-H${rootDir}",
        "-DCMAKE_BUILD_TYPE=${buildType}",
        "-DCMAKE_INSTALL_PREFIX=${buildDir}/cmake-build",
        "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON",
        "-DBUILD_DEPS=ON",
        "-DBUILD_TESTING=OFF"
    )

    if (targetOs.startsWith("linux")) {
        libcryptoPath = "/opt/openssl"
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

    inputs.file("${rootDir}/CMakeLists.txt")
    outputs.file("${buildDir}/cmake-build/CMakeCache.txt")

    doLast {
        val argsStr = cmakeArgs.joinToString(separator=" ")
        logger.info("cmake ${argsStr}")
        exec {
            executable("cmake")
            args(cmakeArgs)
        }
    }
}

val cmakeBuild = tasks.register("cmakeBuild") {
    dependsOn(cmakeConfigure)
    inputs.file("${rootDir}/CMakeLists.txt")
    inputs.file("${buildDir}/cmake-build/CMakeCache.txt")
    inputs.files(fileTree("${rootDir}/src/native").matching {
        include(listOf("**/*.c", "**/*.h"))
    })
    inputs.files(fileTree("${rootDir}/aws-common-runtime").matching {
        include(listOf("**/CMakeLists.txt", "**/*.c", "**/*.h"))
    })
    outputs.files(fileTree("${buildDir}/cmake-build/lib/${targetOs}/${targetArch}"))

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

val crtjni = tasks.register("crtjni") {
    dependsOn(cmakeBuild)
}

// no tests for this lib
tasks.test {
    enabled = false
}

tasks.register<Jar>("crtjar") {
    dependsOn(crtjni)
    archiveBaseName.set("aws-crt-jni")
    archiveClassifier.set("${targetOs}-${targetArch}")
    from(fileTree("${buildDir}/cmake-build/lib/${targetOs}/${targetArch}"))
    into("lib/${targetOs}/${targetArch}")
}
