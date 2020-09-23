/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

plugins {
    `java-library`
    `maven-publish`
}

apply {
    from("common.gradle.kts")
}
val getHostOs = extra["getHostOs"] as () -> String
val getHostArch = extra["getHostArch"] as () -> String

val platforms = listOf(
    listOf("linux", "x86_32"),
    listOf("linux", "x86_64"),
    listOf("linux", "armv7"),
    listOf("linux", "armv6"),
    listOf("linux", "armv8"),
    listOf("android", "x86"),
    listOf("android", "x86_64"),
    listOf("android", "armeabi-v7a"),
    listOf("android", "arm64-v8a"),
    listOf("windows", "x86"),
    listOf("windows", "x86_64"),
    listOf("osx", "x86_64"),
    listOf("freebsd", "x86_64")
)

val targetOs = getHostOs()
val targetArch = getHostArch()

repositories {
    mavenLocal()
    maven {
        url = uri("http://repo.maven.apache.org/maven2")
    }
}

configurations {
    create("jni")
}

dependencies {
    "jni"(project(":src:native"))
    testImplementation("junit:junit:4.12")
}

group = "software.amazon.awssdk.crt"
version = "1.0.0-SNAPSHOT"
description = "software.amazon.awssdk.crt:aws-crt"

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src/main/java"))
        }
    }
    test {
        java {
            setSrcDirs(listOf("src/test/java"))
        }
        // include shared libraries for testing (since testing is done without JARs)
        resources {
            srcDir("${buildDir}/cmake-build/lib/")
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
    dependsOn(":src:native:crtjni")
}

tasks.test {
    dependsOn(tasks.jar)
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

tasks.compileTestJava {
    dependsOn(tasks.compileJava)
}

tasks.jar {
    archiveClassifier.set("${targetOs}-${targetArch}")
    from(sourceSets.main.get().output)
    from(fileTree("${buildDir}/cmake-build/lib/${targetOs}/${targetArch}")) {
        into("lib/${targetOs}/${targetArch}")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven-${project.name}") {
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
