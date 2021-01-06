
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
            srcDirs("${buildDir}/smithyprojections/${project.name}/source/crt")
        }
    }
}

dependencies {
    implementation(project(":smithy-crt"))
    implementation(rootProject)
}
