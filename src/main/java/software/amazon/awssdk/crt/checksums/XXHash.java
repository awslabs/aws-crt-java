/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.checksums;

public class XXHash implements AutoCloseable {
    
    private XXHash(long nativeHandle) {
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
            xxHashRelease(getNativeHandle());
        }
    }

    /**
     * Create new streaming XXHash64.
     */
    static public XXHash newXXHash64(long seed = 0) {
        long nativeHandle = xxHash64Create(seed);
        if (nativeHandle != 0) {
            return new EccKeyPair(nativeHandle);
        }

        return null;
    }

    /**
     * Create new streaming XXHash3_64.
     */
    static public XXHash newXXHash3_64(long seed = 0) {
        long nativeHandle = xxHash3_64Create(seed);
        if (nativeHandle != 0) {
            return new EccKeyPair(nativeHandle);
        }

        return null;
    }

    /**
     * Create new streaming XXHash3_128.
     */
    static public XXHash newXXHash3_128(long seed = 0) {
        long nativeHandle = xxHash3_128Create(seed);
        if (nativeHandle != 0) {
            return new EccKeyPair(nativeHandle);
        }

        return null;
    }

    /**
     * Update xxhash state from message
     * @param message message to update with
     */
    public void update(byte[] message) {
        xxHashUpdate(getNativeHandle(), message);
    }

    /**
     * Finalize the state of the hash
     * @return hash as bytes in big endian
     */
    public byte[] finalize() {
        return xxHashFinalize(getNativeHandle());
    }

    /**
     * Oneshot compute XXHash64.
     */
    static public byte[] xxHash64Compute(byte[] message, long seed = 0) {
        return xxHash64Compute(seed);
    }

    /**
     * Oneshot compute XXHash3_64.
     */
    static public byte[] xxHash3_64Compute(byte[] message, long seed = 0) {
        return xxHash3_64Compute(seed);
    }

    /**
     * Oneshot compute XXHash3_128.
     */
    static public byte[] xxHash3_128Compute(byte[] message, long seed = 0) {
        return xxHash3_128Compute(seed);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native byte[] xxHash64Compute(byte[] message, long seed);
    private static native byte[] xxHash3_64Compute(byte[] message, long seed);
    private static native byte[] xxHash3_128Compute(byte[] message, long seed);

    private static native long xxHash64Create(long seed);
    private static native long xxHash3_64Create(long seed);
    private static native long xxHash3_128Create(long seed);
    private static native void xxHashRelease(long xxhash);

    private static native void xxHashUpdate(long xxhash, byte[] message);
    private static native byte[] xxHashFinalize(long xxhash);
}
