/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrtS3RuntimeException extends CrtRuntimeException {

    private final int statusCode;
    private final byte[] errorPayload;

    public CrtS3RuntimeException(int errorCode, int responseStatus, byte[] errorPayload) {
        super(errorCode);
        this.statusCode = responseStatus;
        this.errorPayload = errorPayload;
    }


    /**
     * Helper function to create a runtime exception from S3 response and a cause.
     * @param errorCode The CRT error code
     * @param responseStatus statusCode of the HTTP response
     * @param errorPayload  body of the error response
     * @param cause cause of the exception such as a Java exception in a callback
     */
    public CrtS3RuntimeException(int errorCode, int responseStatus, byte[] errorPayload, Throwable cause) {
        super(errorCode, cause);
        this.statusCode = responseStatus;
        this.errorPayload = errorPayload;
    }

    /**
     * Returns the status code in S3 response.
     *
     * @return status code in int
     */
    public int getStatusCode() {
        return statusCode;

    }

    /**
     * Returns the error payload without any parsing from S3 response. Can be empty if there was no error body.
     * @return error payload
     */
    public byte[] getErrorPayload() {
        return errorPayload;
    }

    @Override
    public String toString() {
        return String.format("%s: response status code(%d), error payload(%s)", super.toString(), statusCode, errorPayload);
    }
}
