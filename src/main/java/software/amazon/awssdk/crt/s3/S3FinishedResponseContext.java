/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.http.HttpHeader;

public class S3FinishedResponseContext {
    private final int errorCode;
    private final int responseStatus;
    private final byte[] errorPayload;
    private final ChecksumAlgorithm checksumAlgorithm;
    private final boolean didValidateChecksum;

    private final Throwable cause;
    private final HttpHeader[] errorHeaders;
    /*
     * errorCode The CRT error code
     * responseStatus statusCode of the HTTP response
     * errorPayload body of the error response. Can be null if the request completed successfully
     * checksumAlgorithm, the algorithm used to validate the Body, None if not validated
     * didValidateChecksum which is true if the response was validated.
     * cause of the error such as a Java exception in a callback. Maybe NULL if there was no exception in a callback.
     */
    S3FinishedResponseContext(final int errorCode, final int responseStatus, final byte[] errorPayload, final ChecksumAlgorithm checksumAlgorithm, final boolean didValidateChecksum, Throwable cause, final HttpHeader[] errorHeaders) {
        this.errorCode = errorCode;
        this.responseStatus = responseStatus;
        this.errorPayload = errorPayload;
        this.checksumAlgorithm = checksumAlgorithm;
        this.didValidateChecksum = didValidateChecksum;
        this.cause = cause;
        this.errorHeaders = errorHeaders;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    /*
     * If the request didn't receive a response due to a connection
     * failure or some other issue the response status will be 0.
     */
    public int getResponseStatus() {
        return this.responseStatus;
    }

    /*
     * In the case of a failed http response get the payload of the response.
     */
    public byte[] getErrorPayload() {
        return this.errorPayload;
    }

    /*
     * if no checksum is found, or the request finished with an error the Algorithm will be None,
     * otherwise the algorithm will correspond to the one attached to the object when uploaded.
     */
    public ChecksumAlgorithm getChecksumAlgorithm() {
        return this.checksumAlgorithm;
    }

    public boolean isChecksumValidated() {
        return this.didValidateChecksum;
    }

    /**
     * Cause of the error, such as a Java exception from a callback. May be NULL if there was no exception in a callback.
     * @return throwable
     */
    public Throwable getCause() {
        return cause;
    }


    public HttpHeader[] getErrorHeaders() {
        return errorHeaders;
    }
}
