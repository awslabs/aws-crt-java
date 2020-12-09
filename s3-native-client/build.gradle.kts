
plugins {
    id("software.amazon.smithy").version("0.5.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

val copyTask = tasks.register<Copy>("copyGeneratedSource") {
    from(fileTree("${buildDir}/smithyprojections/${project.name}/source/crt/model"))
    into(file("${projectDir}/src/main/java"))
    dependsOn("smithyBuildJar")
}

tasks.compileJava {
    dependsOn("copyGeneratedSource")
}

dependencies {
    implementation(project(":smithy-crt"))
}
