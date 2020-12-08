
plugins {
    id("software.amazon.smithy").version("0.5.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

tasks.compileJava {
    dependsOn("smithyBuildJar")
}

dependencies {
    implementation(project(":smithy-crt"))
}
