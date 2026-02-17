/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.checksums;
import software.amazon.awssdk.crt.CrtResource;

public class XXHash extends CrtResource {
    
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
     * @param seed seed to use for the hash
     * @return new XXHash instance
     */
    static public XXHash newXXHash64(long seed) {
        long nativeHandle = xxHash64Create(seed);
        return new XXHash(nativeHandle);
    }


    /**
     * Create new streaming XXHash64.
     * @return new XXHash instance
     */
    static public XXHash newXXHash64() {
        long nativeHandle = xxHash64Create(0);
        return new XXHash(nativeHandle);
    }

    /**
     * Create new streaming XXHash3_64.
     * @param seed seed to use for the hash
     * @return new XXHash instance
     */
    static public XXHash newXXHash3_64(long seed) {
        long nativeHandle = xxHash364Create(seed);
        return new XXHash(nativeHandle);
    }

    /**
     * Create new streaming XXHash3_64.
     * @return new XXHash instance
     */
    static public XXHash newXXHash3_64() {
        long nativeHandle = xxHash364Create(0);
        if (nativeHandle != 0) {
            return new XXHash(nativeHandle);
        }

        return null;
    }

    /**
     * Create new streaming XXHash3_128.
     * @param seed seed to use for the hash
     * @return new XXHash instance
     */
    static public XXHash newXXHash3_128(long seed) {
        long nativeHandle = xxHash3128Create(seed);
        if (nativeHandle != 0) {
            return new XXHash(nativeHandle);
        }

        return null;
    }

    /**
     * Create new streaming XXHash3_128.
     * @return new XXHash instance
     */
    static public XXHash newXXHash3_128() {
        long nativeHandle = xxHash3128Create(0);
        if (nativeHandle != 0) {
            return new XXHash(nativeHandle);
        }

        return null;
    }

    /**
     * Update xxhash state from input
     * @param input input to update with
     */
    public void update(byte[] input) {
        this.update(input, 0, input.length);
        
    }

    /**
     * Update xxhash state with a single byte
     * @param b input to update with
     */
    public void update(int b) {
        if (b < 0 || b > 0xff) {
            throw new IllegalArgumentException();
        }
        byte[] buf = { (byte) (b & 0x000000ff) };
        this.update(buf);
    }

    /**
     * Update xxhash state with a subrange of input
     * @param input input to update with
     * @param offset to start update with
     * @param length of data
     */
    public void update(byte[] input, int offset, int length) {
        if (input == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || length < 0 || offset > b.length - length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        xxHashUpdate(getNativeHandle(), input, offset, length);
    }

    /**
     * Return digest for the current state of hash.
     * @return hash as bytes in big endian
     */
    public byte[] digest() {
        return xxHashFinalize(getNativeHandle());
    }

    /**
     * Oneshot compute XXHash64.
     * @param input input input to hash
     * @param seed seed
     * @return xxhash64 hash
     */
    static public byte[] computeXXHash64(byte[] input, long seed) {
        return xxHash64Compute(input, seed);
    }

    /**
     * Oneshot compute XXHash64.
     * @param input input input to hash
     * @return xxhash64 hash
     */
    static public byte[] computeXXHash64(byte[] input) {
        return xxHash64Compute(input, 0);
    }


    /**
     * Oneshot compute XXHash3_64.
     * @param input input input to hash
     * @param seed seed
     * @return xxhash64 hash
     */
    static public byte[] computeXXHash3_64(byte[] input, long seed) {
        return xxHash364Compute(input, seed);
    }

    /**
     * Oneshot compute XXHash3_64.
     * @param input input input to hash
     * @return xxhash64 hash
     */
    static public byte[] computeXXHash3_64(byte[] input) {
        return xxHash364Compute(input, 0);
    }

    /**
     * Oneshot compute XXHash3_128.
     * @param input input input to hash
     * @param seed seed
     * @return xxhash64 hash
     */
    static public byte[] computeXXHash3_128(byte[] input, long seed) {
        return xxHash3128Compute(input, seed);
    }

    /**
     * Oneshot compute XXHash3_128.
     * @param input input input to hash
     * @return xxhash64 hash
     */
    static public byte[] computeXXHash3_128(byte[] input) {
        return xxHash3128Compute(input, 0);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native byte[] xxHash64Compute(byte[] input, long seed);
    private static native byte[] xxHash364Compute(byte[] input, long seed);
    private static native byte[] xxHash3128Compute(byte[] input, long seed);

    private static native long xxHash64Create(long seed);
    private static native long xxHash364Create(long seed);
    private static native long xxHash3128Create(long seed);
    private static native void xxHashRelease(long xxhash);

    private static native void xxHashUpdate(long xxhash, byte[] input, int offset, int length);
    private static native byte[] xxHashFinalize(long xxhash);
}
