
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
            //srcDirs("${buildDir}/smithyprojections/${project.name}/source/crt")
            srcDirs("src/generated/java")
        }
    }
}

dependencies {
    implementation(project(":smithy-crt"))
    implementation(rootProject)

    testImplementation(testFixtures(rootProject))
    testImplementation("junit:junit:4.13.1")    //matches dependency as parent pom.xml
}
