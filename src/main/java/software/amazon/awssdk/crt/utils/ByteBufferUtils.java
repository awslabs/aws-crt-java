/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.amazon.awssdk.crt.utils;

import java.nio.ByteBuffer;

/**
 * Utility Class with Helper functions for working with ByteBuffers
 */
public class ByteBufferUtils {
    private ByteBufferUtils() {}

    /**
     * Allocates a new backing byte[] with a new ByteBuffer, and copies the contents of the input buffer to the copy.
     *
     * @param input The ByteBuffer to copy
     * @return A new ByteBuffer containing a deep copy of the input ByteBuffer.
     */
    public static ByteBuffer deepCopy(ByteBuffer input) {
        // Allocate a new ByteBuffer that's large enough to store a copy
        ByteBuffer deepCopy = ByteBuffer.allocate(input.remaining());

        // Copy contents of input buffer to new Buffer
        deepCopy.put(input);

        // Flip deepCopy from Write mode to Read Mode
        deepCopy.flip();

        return deepCopy;
    }

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

    /**
     * Guarantees a DirectByteBuffer from a ByteBuffer. If the ByteBuffer is already direct, it will be returned
     * and avoid an unnecessary copy.
     * @param in Input ByteBuffer
     * @return A ByteBuffer that is guaranteed to be directly allocated
     */
    public static ByteBuffer toDirectBuffer(ByteBuffer in) {
        /* If the buffer is already direct, we can avoid an additional copy */
        if (in.isDirect()) {
            return in;
        }

        ByteBuffer direct = ByteBuffer.allocateDirect(in.capacity());
        direct.put(in);
        return direct;
    }
}
