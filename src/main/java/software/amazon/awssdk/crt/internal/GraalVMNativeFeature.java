/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.internal;

import org.graalvm.nativeimage.hosted.Feature;

import software.amazon.awssdk.crt.CRT;

/**
 * Implementation of GraalVM feature to extract the share lib to the image path.
 * Internal API, not for external usage.
 */
public class GraalVMNativeFeature implements Feature {

    @Override
    public void afterImageWrite(AfterImageWriteAccess access) {
        new CRT();
        ExtractLib.extractLibrary(access.getImagePath().getParent().toString());
    }
}
