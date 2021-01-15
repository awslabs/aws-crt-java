/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
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
        super(CRT.awsErrorString(errorCode));
        this.errorCode = errorCode;
        this.errorName = errorName;
    }

    public CrtRuntimeException(int errorCode) {
        super(CRT.awsErrorString(errorCode));
        this.errorCode = errorCode;
        this.errorName = CRT.awsErrorName(errorCode);
    }

    @Override
    public String toString() {
        return String.format("%s %s(%d)", super.toString(), errorName, errorCode);
    }
};
