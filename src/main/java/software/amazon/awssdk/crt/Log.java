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
 *
 * It is NOT safe to change the logging setup after it has been initialized.
 */
public class Log {

    // Log must initialize the CRT in case it is the first API call made
    static {
        new CRT();
    }

    /*
     * System properties for automatic logging initialization on CRT initialization
     */
    private static final String LOG_DESTINATION_PROPERTY_NAME = "aws.crt.log.destination";
    private static final String LOG_FILE_NAME_PROPERTY_NAME = "aws.crt.log.filename";
    private static final String LOG_LEVEL_PROPERTY_NAME = "aws.crt.log.level";

    /**
     * Enum that determines where logging should be routed to.
     */
    private enum LogDestination {
        None,
        Stdout,
        Stderr,
        File
    }

    /**
     * Enum that controls how detailed logging should be.
     */
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

    public enum LogSubject {
        // aws-c-common
        CommonGeneral(0x000),
        CommonTaskScheduler(0x001),

        // aws-c-io
        IoGeneral(0x400),
        IoEventLoop(0x401),
        IoSocket(0x402),
        IoSocketHandler(0x403),
        IoTls(0x404),
        IoAlpn(0x405),
        IoDns(0x406),
        IoPki(0x407),
        IoChannel(0x408),
        IoChannelBootstrap(0x409),
        IoFileUtils(0x40A),
        IoSharedLibrary(0x40B),

        // aws-c-http
        HttpGeneral(0x800),
        HttpConnection(0x801),
        HttpServer(0x802),
        HttpStream(0x803),
        HttpConnectionManager(0x804),
        HttpWebsocket(0x805),
        HttpWebsocketSetup(0x806),

        // aws-c-mqtt
        MqttGeneral(0x1400),
        MqttClient(0x1401),
        MqttTopicTree(0x1402),

        // aws-c-auth
        AuthGeneral(0x1800),
        AuthProfile(0x1801),
        AuthCredentialsProvider(0x1802),
        AuthSigning(0x1803),

        // aws-crt-java, we're authoritative
        JavaCrtGeneral(0x2400),
        JavaCrtResource(0x2401)
        ;

        LogSubject(int value) {
            this.value = value;
        }

        public int getValue() { return value; }

        private int value;
    };

    /**
     * Logs a message at the specified log level.
     * @param level (for filtering purposes) level attached to the log invocation
     * @param subject (for filtering purposes) log subject
     * @param message log string to write
     */
    public static void log(LogLevel level, LogSubject subject, String message) {
        log(level.getValue(), subject.getValue(), message);
    }

    /**
     * Examines logging-related system properties and initializes the logging system if they
     * have been properly set.
     */
    public static void initLoggingFromSystemProperties() throws IllegalArgumentException {
        String destinationString = System.getProperty(LOG_DESTINATION_PROPERTY_NAME);
        String filenameString = System.getProperty(LOG_FILE_NAME_PROPERTY_NAME);
        String levelString = System.getProperty(LOG_LEVEL_PROPERTY_NAME);

        // If nothing was specified, disable logging
        if (destinationString == null && levelString == null) {
            return;
        }

        // If no destination wasn't specified, default to stderr
        if (destinationString == null) {
            destinationString = "Stderr";
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
            case None:
                break;
        }
    }

    /*
     * Initializes logging to go to stdout
     * @param level the filter level to apply to log calls
     */
    public static void initLoggingToStdout(LogLevel level) {
        initLoggingToStdout(level.getValue());
    }

    /*
     * Initializes logging to go to stderr
     * @param level the filter level to apply to log calls
     */
    public static void initLoggingToStderr(LogLevel level) {
        initLoggingToStderr(level.getValue());
    }

    /*
     * Initializes logging to go to a file
     * @param level the filter level to apply to log calls
     * @param filename name of the file to direct logging to
     */
    public static void initLoggingToFile(LogLevel level, String filename) {
        initLoggingToFile(level.getValue(), filename);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void log(int level, int subject, String logstring);

    private static native void initLoggingToStdout(int level);
    private static native void initLoggingToStderr(int level);
    private static native void initLoggingToFile(int level, String filename);
};
