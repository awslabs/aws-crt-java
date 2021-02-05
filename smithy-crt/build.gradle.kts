
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile

description = "Generates the S3 Native Client from S3 Smithy model"

repositories {
    mavenLocal()
    mavenCentral()
}

plugins {
    kotlin("jvm") version "1.4.20"
}

tasks.named<KotlinJvmCompile>("compileKotlin") {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.named<KotlinJvmCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation("com.squareup:javapoet:1.13.0")
    implementation("com.atlassian.commonmark:commonmark:0.14.0")
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("software.amazon.smithy:smithy-codegen-core:[1.0.2,1.1.0[")
    implementation("software.amazon.smithy:smithy-protocol-test-traits:[1.0.2,1.1.0[")
    implementation("software.amazon.smithy:smithy-model:1.4.0")
    implementation("software.amazon.smithy:smithy-aws-traits:1.4.0")

    testCompileOnly("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
    testCompileOnly("org.junit.jupiter:junit-jupiter-params:5.4.0")
    testCompileOnly("org.hamcrest:hamcrest:2.1")
}
