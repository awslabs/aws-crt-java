/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

/** Binary size units (powers of 1024). Internal to the S3 package. */
final class SizeUnits {
    static final long KIB = 1024L;
    static final long MIB = 1024L * KIB;
    static final long GIB = 1024L * MIB;

    private SizeUnits() {}
}
