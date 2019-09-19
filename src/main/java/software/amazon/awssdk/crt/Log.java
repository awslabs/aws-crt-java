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
 * Static wrapper around native and crt logging.
 */
public class Log {

    private static final String LOG_DESTINATION_PROPERTY_NAME = "aws.iot.sdk.log.destination";
    private static final String LOG_FILE_NAME_PROPERTY_NAME = "aws.iot.sdk.log.filename";
    private static final String LOG_LEVEL_PROPERTY_NAME = "aws.iot.sdk.log.level";

    private enum LogDestination {
        None,
        Stdout,
        Stderr,
        File
    }

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

    public static void log(LogLevel level, String logstring) {
        log(level.getValue(), logstring);
    }

    public static void initLoggingFromSystemProperties() throws IllegalArgumentException {
        String destinationString = System.getProperty(LOG_DESTINATION_PROPERTY_NAME);
        String filenameString = System.getProperty(LOG_FILE_NAME_PROPERTY_NAME);
        String levelString = System.getProperty(LOG_LEVEL_PROPERTY_NAME);
        if (destinationString == null) {
            return;
        }

        LogDestination destination = LogDestination.valueOf(destinationString);
        LogLevel level = LogLevel.Warn;
        if (levelString != null) {
            level = LogLevel.valueOf(levelString);
        }

        switch(destination) {
            case Stdout:
                initLoggingToStdout(level.getValue());
                break;

            case Stderr:
                initLoggingToStderr(level.getValue());
                break;

            case File:
                if (filenameString == null) {
                    return;
                }

                initLoggingToFile(level.getValue(), filenameString);
                break;
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void log(int level, String logstring);

    private static native void initLoggingToStdout(int level);
    private static native void initLoggingToStderr(int level);
    private static native void initLoggingToFile(int level, String filename);
};
