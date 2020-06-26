/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt;

import software.amazon.awssdk.crt.CRT;

/**
 * This exception will be thrown by any exceptional cases encountered within the
 * JNI bindings to the AWS Common Runtime
 */
public class MqttException extends RuntimeException {
    private int errorCode;

    public MqttException(String msg) {
        super(msg);
        this.errorCode = -1;
    }

    public MqttException(int errorCode) {
        super(CRT.awsErrorString(errorCode));
        this.errorCode = errorCode;
    }

    /**
     * Returns the error code captured when the exception occurred. This can be fed to {@link CRT.awsErrorString} to
     * get a user-friendly error string
     * @return The error code associated with this exception
     */
    int getErrorCode() {
        return errorCode;
    }
};
