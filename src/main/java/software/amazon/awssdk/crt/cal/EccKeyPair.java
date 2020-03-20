/*
 * Copyright 2010-2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.awssdk.crt.cal;

import software.amazon.awssdk.crt.CrtResource;

/**
 * This class puts an opaque wrapper around aws_ecc_key_pair from aws-c-cal.  Currently, it is only intended to be
 * cached and returned to native code by a signing invocation.
 *
 * If there's a compelling reason, we can add accessors and conversions to/from Java's KeyPair.
 */
public final class EccKeyPair extends CrtResource {

    /**
     * Creates a new ecc key pair.  Only called from native at the moment.
     */
    public EccKeyPair(long nativeHandle) {
        acquireNativeHandle(nativeHandle);
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /**
     * Releases the instance's reference to the underlying native key pair
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            eccKeyPairRelease(this, getNativeHandle());
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void eccKeyPairRelease(EccKeyPair thisObj, long elg);
};
