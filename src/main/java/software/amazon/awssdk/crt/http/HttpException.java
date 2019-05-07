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

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CRT;

/**
 * This exception will be thrown by any exceptional cases encountered within the
 * JNI bindings to the AWS Common Runtime
 */
public class HttpException extends RuntimeException {
    private int errorCode;

    public HttpException(int errorCode) {
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
}
