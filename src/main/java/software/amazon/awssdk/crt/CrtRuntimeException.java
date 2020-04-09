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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private final static Pattern crtExFormat = Pattern.compile("aws_last_error: (.+)\\(([-0-9]+)\\),");

    public CrtRuntimeException(String msg) {
        super(msg);
        
        Matcher matcher = crtExFormat.matcher(msg);
        if (matcher.matches()) {
            errorName = matcher.group(1);
            errorCode = Integer.parseInt(matcher.group(2));
        } else {
            errorCode = -1;
            errorName = "UNKNOWN";
        }
    }

    public CrtRuntimeException(int errorCode, String errorName) {
        this.errorCode = errorCode;
        this.errorName = errorName;
    }
};
