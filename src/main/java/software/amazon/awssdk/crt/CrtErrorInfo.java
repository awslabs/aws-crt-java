/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt;

import static software.amazon.awssdk.crt.CRT.awsErrorName;
import static software.amazon.awssdk.crt.CRT.awsErrorString;

/**
 * This enum contains errors that need to be explicitly handled on the Java side of the JNI boundary.
 * Any time someone needs to respond to specific errorCodes, they should be added here
 * , so they'll at least
 * be in a central place.
 */
public enum CrtErrorInfo {
    Success(0),
    TLSNegotiationFailure(1029),
    /**
     * We didn't know what it was, and need a way to know that.
     */
    UnKnownError(Integer.MAX_VALUE);

    private int errorCode = 0;

    CrtErrorInfo(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getAwsErrorString() {
        return awsErrorString(errorCode);
    }

    public String getAwsErrorName() {
        return awsErrorName(errorCode);
    }

    public boolean isModeledError() {
        return errorCode != UnKnownError.getErrorCode();
    }

    public static CrtErrorInfo fromErrorCode(int errorCode) {
        for(CrtErrorInfo error : CrtErrorInfo.values()) {
            if (error.getErrorCode() == errorCode) {
                return error;
            }
        }

        return UnKnownError;
    }
}
