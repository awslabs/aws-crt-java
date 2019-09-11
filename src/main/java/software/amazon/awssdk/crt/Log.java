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
public class Log {

    public enum LogLevel {
        None(0),
        Fatal(1),
        Error(2),
        Warn(3),
        Info(4),
        Debug(5),
        Trace(6);

        private int level;

        LogLevel(int value) {
            level = value;
        }

        public int getValue() {
            return level;
        }
    };

    public static void Log(LogLevel level, String logstring) {
        log(level.getValue(), logstring);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void log(int level, String logstring);
};
