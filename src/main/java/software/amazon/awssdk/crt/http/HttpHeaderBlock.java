/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

/**
 * Type of header block
 */
public enum HttpHeaderBlock {
    
    MAIN(0),

    INFORMATIONAL(1),

    TRAILING(2);

    private int blockType;

    HttpHeaderBlock(int value) {
        blockType = value;
    }

    public int getValue() {
        return blockType;
    }
}
