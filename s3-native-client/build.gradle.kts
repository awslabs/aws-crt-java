
plugins {
    id("software.amazon.smithy").version("0.5.0")
    id("com.github.johnrengelman.shadow") version "6.1.0"
    `java-library`
}

repositories {
    mavenLocal()
    mavenCentral()
}

tasks.compileJava {
    dependsOn("smithyBuildJar")
}

sourceSets {
    main {
        java {
            srcDirs("../src/main/java/s3_native_client")
        }
    }
}

dependencies {
    implementation(project(":smithy-crt"))
    implementation(rootProject)

    testImplementation("org.mockito:mockito-all:1.10.19")
    testImplementation("junit:junit:4.13.1")    //matches dependency as parent pom.xml
}
