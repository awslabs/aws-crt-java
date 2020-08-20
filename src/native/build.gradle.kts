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
}

val rootDir = rootProject.projectDir

apply {
    from("${rootProject.projectDir}/common.gradle.kts")
}

val getHostOs = extra["getHostOs"] as () -> String
val getHostArch = extra["getHostArch"] as () -> String

val targetOs = getHostOs()
val targetArch = getHostArch()

println("Target: OS=${targetOs} ARCH=${targetArch}")


// Use the same buildDir as the root project to make packaging sane
buildDir = rootProject.buildDir
// Use the group/version from rootProject
group = rootProject.group
version = rootProject.version
description = "${rootProject.group}:native:${targetOs}-${targetArch}"

var libcryptoPath : String? = null

var buildType = "Release"
if (project.hasProperty("buildType")) {
    buildType = project.property("buildType").toString()
    logger.info("Using custom build type: ${buildType}")
}

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

    inputs.property("buildType", buildType)
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
    inputs.property("buildType", buildType)
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
        "--config", buildType,
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

// java {
//     registerFeature("awsCrtJni") {
//         capability("software.amazon.awssdk.crt", "aws-crt-jni", "1.0")
//         capability("software.amazon.awssdk.crt", "aws-crt-jni:${targetOs}-${targetArch}", "1.0")
//     }
// }

// no tests for this lib
// tasks.test {
//     enabled = false
// }

// tasks.jar {
//     dependsOn(crtjni)
//     archiveClassifier.set("${targetOs}-${targetArch}")
//     from(fileTree("${buildDir}/cmake-build/lib/${targetOs}/${targetArch}"))
//     into("lib/${targetOs}/${targetArch}")
// }
