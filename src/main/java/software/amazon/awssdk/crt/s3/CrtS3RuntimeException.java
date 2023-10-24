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
    private final String awsErrorCode;
    private final String awsErrorMessage;
    private final String errorPayload;

    private final static String codeBeginBlock = new String("<Code>");
    private final static String codeEndBlock = new String("</Code>");
    private final static String messageBeginBlock = new String("<Message>");
    private final static String messageEndBlock = new String("</Message>");

    public CrtS3RuntimeException(int errorCode, int responseStatus, String errorPayload) {
        super(errorCode);
        this.statusCode = responseStatus;
        this.errorPayload = errorPayload;
        this.awsErrorCode = GetElementFromPayload(errorPayload, codeBeginBlock, codeEndBlock);
        this.awsErrorMessage = GetElementFromPayload(errorPayload, messageBeginBlock, messageEndBlock);
    }


    public CrtS3RuntimeException(int errorCode, int responseStatus, byte[] errorPayload) {
        super(errorCode);
        String errorString = new String(errorPayload, java.nio.charset.StandardCharsets.UTF_8);
        this.statusCode = responseStatus;
        this.errorPayload = errorString;
        this.awsErrorCode = GetElementFromPayload(this.errorPayload, codeBeginBlock, codeEndBlock);
        this.awsErrorMessage = GetElementFromPayload(this.errorPayload, messageBeginBlock, messageEndBlock);
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
        String errorString = new String(errorPayload, java.nio.charset.StandardCharsets.UTF_8);
        this.statusCode = responseStatus;
        this.errorPayload = errorString;
        this.awsErrorCode = GetElementFromPayload(this.errorPayload, codeBeginBlock, codeEndBlock);
        this.awsErrorMessage = GetElementFromPayload(this.errorPayload, messageBeginBlock, messageEndBlock);
    }

    /**
     * Helper function to get the detail of an element from xml payload. If not
     * found, empty string will be returned.
     */
    private String GetElementFromPayload(String errorPayload, String beginBlock, String endBlock) {
        Pattern regexFormat = Pattern.compile(beginBlock + ".*" + endBlock);
        Matcher matcher = regexFormat.matcher(errorPayload);
        String result = "";
        if (matcher.find()) {
            result = errorPayload.substring(matcher.start() + beginBlock.length(), matcher.end() - endBlock.length());
        }
        return result;
    }

    /**
     * Returns the aws error code from S3 response. The {@code Code} element in xml
     * response.
     *
     * @return errorCode, if no {@code Code} element in the response, empty string will be
     *         returned
     */
    public String getAwsErrorCode() {
        return awsErrorCode;
    }

    /**
     * Returns the error message from S3 response. The detail among {@code Message}
     * element in xml response.
     *
     * @return error message, if no {@code Message} element in the response, empty string
     *         will be returned
     */
    public String getAwsErrorMessage() {
        return awsErrorMessage;
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
    public String getErrorPayload() {
        return errorPayload;
    }

    @Override
    public String toString() {
        return String.format("%s: response status code(%d). aws error code(%s), aws error message(%s)", super.toString(), statusCode, awsErrorCode, awsErrorMessage);
    }
}
