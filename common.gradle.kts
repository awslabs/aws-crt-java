/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

extra["getHostOs"] = fun(): String {
    val os = org.gradle.internal.os.OperatingSystem.current().toString().toLowerCase()
    return os.split(" ").first()
}

extra["getHostArch"] = fun(): String {
    val arch = System.getProperty("os.arch")
    when (arch) {
        in listOf("x86_64", "amd64") -> return "x86_64"
        in listOf("x86", "i386", "i586", "i686") -> return "x86"
        else -> throw GradleException("Can't normalize host's CPU architecture: '${arch}'")
    }
}
