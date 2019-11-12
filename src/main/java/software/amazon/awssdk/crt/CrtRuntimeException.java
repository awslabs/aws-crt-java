/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
package software.amazon.awssdk.crt;

/**
 * This exception will be thrown by any exceptional cases encountered within
 * the JNI bindings to the AWS Common Runtime
 */
public class CrtRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 0; // Shut up linters

    static {
        new CRT();
    }

    public final int errorCode;
    public final String errorName;

    CrtRuntimeException(String msg) {
        super(msg);
        errorCode = -1;
        errorName = "UNKNOWN";
    }

    CrtRuntimeException(int errorCode, String msg) {
        super(String.format("%s: %s(%d) %s", msg, CRT.awsErrorName(errorCode), errorCode, CRT.awsErrorString(errorCode)));
        this.errorCode = errorCode;
        this.errorName = CRT.awsErrorName(errorCode);
    }
};
