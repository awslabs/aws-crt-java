/*
 * Helpful resources/examples:
 * http://gradle.monochromeroad.com/docs/userguide/nativeBinaries.html
 * https://github.com/NationalSecurityAgency/ghidra/blob/master/Ghidra/Features/Decompiler/build.gradle
 */

plugins {
    `c`
    `java-library`
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("http://repo.maven.apache.org/maven2")
    }
}

dependencies {
    testImplementation("junit:junit:4.12")
}

group = "software.amazon.awssdk.crt"
version = "1.0.0-SNAPSHOT"
description = "software.amazon.awssdk.crt:aws-crt"

var libcryptoPath : String? = null

// buildTypes {
//     create("debug") {

//     }
//     create("release") {

//     }
// }

var buildType = "RelWithDebInfo"
if (project.hasProperty("buildType")) {
    buildType = project.property("buildType").toString()
    println("Using custom build type: ${buildType}")
}

val cmakeConfigure = tasks.register("cmakeConfigure") {
    var cmakeArgs = listOf(
        "-B${buildDir}/cmake-build",
        "-H${projectDir}",
        "-DCMAKE_BUILD_TYPE=${buildType}",
        "-DCMAKE_INSTALL_PREFIX=${buildDir}/cmake-build",
        "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON",
        "-DBUILD_DEPS=ON",
        "-DBUILD_TESTING=OFF"
    )

    if (org.gradle.internal.os.OperatingSystem.current().isLinux()) {
        libcryptoPath = "/opt/openssl"
        // To set this, add -PlibcryptoPath=/path/to/openssl/home on the command line
        if (project.hasProperty("libcryptoPath")) {
            libcryptoPath = project.property("libcryptoPath").toString()
            println("Using project libcrypto path: ${libcryptoPath}")
        }
    }

    if (libcryptoPath != null) {
        cmakeArgs += listOf(
            "-DLibCrypto_INCLUDE_DIR=${libcryptoPath}/include",
            "-DLibCrypto_STATIC_LIBRARY=${libcryptoPath}/lib/libcrypto.a"
        )
    }

    inputs.file("CMakeLists.txt")
    outputs.file("${buildDir}/cmake-build/CMakeCache.txt")

    doLast {
        val argsStr = cmakeArgs.joinToString(separator=" ")
        println("cmake ${argsStr}")
        exec {
            executable("cmake")
            args(cmakeArgs)
        }
    }
}

val cmakeBuild = tasks.register("cmakeBuild") {
    dependsOn(cmakeConfigure)
    inputs.file("${buildDir}/cmake-build/CMakeCache.txt")
    inputs.files(fileTree("src/native").matching {
        include(listOf("**/*.c", "**/*.h"))
    })
    inputs.files(fileTree("aws-common-runtime").matching {
        include(listOf("**/CMakeLists.txt", "**/*.c", "**/*.h"))
    })
    outputs.file("${buildDir}/cmake-build/lib/libaws-crt-jni.so")

    var cmakeArgs = listOf(
        "--build", "${buildDir}/cmake-build",
        "--target", "all"
    )

    doLast {
        val argsStr = cmakeArgs.joinToString(separator=" ")
        println("cmake ${argsStr}")
        exec {
            executable("cmake")
            args(cmakeArgs)
        }
    }
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/java"))
        }
        // include shared libraries built by cmake/CI/CD in the lib folder
        resources {
            srcDir("${buildDir}/cmake-build/lib")
        }
    }
    test {
        java {
            setSrcDirs(listOf("src/test/java"))
        }
    }
}

java {
    withJavadocJar()
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.compileJava {
    dependsOn(cmakeBuild)
}

tasks.test {
    useJUnit()
    testLogging {
        events("passed", "skipped", "failed")
    }
    for (prop in listOf("certificate", "privatekey", "endpoint", "rootca")) {
        if (project.hasProperty(prop)) {
            systemProperty(prop, project.property(prop).toString())
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = project.name
            from(components["java"])
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/awslabs/aws-crt-java")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("aws-sdk-common-runtime")
                        name.set("AWS SDK Common Runtime Team")
                        email.set("aws-sdk-common-runtime@amazon.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/awslabs/aws-crt-java.git")
                    developerConnection.set("scm:git:ssh://github.com/awslabs/aws-crt-java.git")
                    url.set("https://github.com/awslabs/aws-crt-java")
                }
            }
        }
    }
    repositories {
        maven {
            val releasesRepo = uri("https://aws.oss.sonatype.org/")
            val snapshotRepo = uri("https://aws.oss.sonatype.org/content/repositories/snapshots")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotRepo else releasesRepo
        }
    }
}
