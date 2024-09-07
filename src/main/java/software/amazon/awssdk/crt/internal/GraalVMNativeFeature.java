/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.internal;

import org.graalvm.nativeimage.hosted.Feature;

import software.amazon.awssdk.crt.CRT;

/**
 * Implementation of GraalVM feature to extract the share lib to the image path.
 * From GraalVM docs:
 * > When loading native libraries using System.loadLibrary() (and related APIs),
 * > the native image will search the directory containing the native image before searching the Java library path
 * https://www.graalvm.org/latest/reference-manual/native-image/dynamic-features/JNI/#loading-native-libraries
 * Internal API, not for external usage.
 */
public class GraalVMNativeFeature implements Feature {

    @Override
    public void afterImageWrite(AfterImageWriteAccess access) {
        new CRT();
        ExtractLib.extractLibrary(access.getImagePath().getParent().toString());
    }
}
