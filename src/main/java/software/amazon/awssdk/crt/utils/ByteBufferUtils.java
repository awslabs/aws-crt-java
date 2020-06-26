/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.utils;

import java.nio.ByteBuffer;

/**
 * Utility Class with Helper functions for working with ByteBuffers
 */
public class ByteBufferUtils {
    private ByteBufferUtils() {}

    /**
     * Transfers as much data as possible from an input ByteBuffer to an output ByteBuffer
     * @param in The input ByteBuffer
     * @param out The output ByteBuffer
     * @return The number of bytes transferred
     */
    public static int transferData(ByteBuffer in, ByteBuffer out) {
        int amtToTransfer = Math.min(in.remaining(), out.remaining());

        // Make a new ByteBuffer that shares the same underlying buffer as the input ByteBuffer
        ByteBuffer shallowCopy = in.duplicate();

        // Modify the shallow copy's read limit so that it matches the write space remaining in the output Buffer so
        // we don't throw an OutOfBounds exception
        shallowCopy.limit(shallowCopy.position() + amtToTransfer);

        // Transfer the data
        out.put(shallowCopy);

        // Increment the read position of the original input buffer by the number of bytes transferred
        in.position(in.position() + amtToTransfer);
        return amtToTransfer;
    }
}
