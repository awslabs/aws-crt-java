/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 *
 * Helpful resources/examples:
 * http://gradle.monochromeroad.com/docs/userguide/nativeBinaries.html
 * https://github.com/NationalSecurityAgency/ghidra/blob/master/Ghidra/Features/Decompiler/build.gradle
 */

plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2")
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("commons-cli:commons-cli:1.5.0")
    testImplementation("org.mockito:mockito-core:3.11.2")
}

group = "software.amazon.awssdk.crt"
version = "1.0.0-SNAPSHOT"
description = "software.amazon.awssdk.crt:aws-crt"

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
    dependsOn(":native:cmakeBuild")
}

tasks.processResources {
    // sourceSets includes the compiled libs, so declare the dependency
    dependsOn(":native:cmakeBuild")
}

// withSourcesJar uses output of task :native:cmakeBuild so explicitly declaring dependency:
tasks.named("sourcesJar") {
    dependsOn(":native:cmakeBuild")
}

tasks.test {
    useJUnit()
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
    }
    for (prop in listOf("certificate", "privatekey", "endpoint", "rootca", "privatekey_p8")) {
        if (project.hasProperty(prop)) {
            systemProperty(prop, project.property(prop).toString())
        }
    }
    //uncomment the next line to attach the debugger to the JNI layer.
    // systemProperty("aws.crt.debugwait", "1")
}

tasks.compileTestJava {
    dependsOn(tasks.compileJava)
}

publishing {

    repositories {
        maven { name = "testLocal"; url = file("${rootProject.buildDir}/m2").toURI() }
    }

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
