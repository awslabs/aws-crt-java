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
package software.amazon.awssdk.crt.io;

import java.io.FileDescriptor;
import java.io.File;
import java.nio.file.Path;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;

public final class Logger extends CrtResource {
    public enum Level {
        NONE(0), FATAL(1), ERROR(2), WARN(3), INFO(4), DEBUG(5), TRACE(6);

        private int value;

        Level(int value) {
            this.value = value;
        }

        public int getLevel() {
            return this.value;
        }
    }
    
    public static void configure(FileDescriptor fd, Level level) throws CrtRuntimeException {
        configureLoggerWithFd(fd, level.getLevel());
    }

    public static void configure(Path filename, Level level) throws CrtRuntimeException {
        configureLoggerWithFilename(filename.toString(), level.getLevel());
    }

    public static void configure(File file, Level level) throws CrtRuntimeException {
        configureLoggerWithFilename(file.getAbsolutePath(), level.getLevel());
    }

    public static void flush() {
        flushLogger();
    }

    private static native void configureLoggerWithFd(FileDescriptor fd, int level) throws CrtRuntimeException;

    private static native void configureLoggerWithFilename(String filename, int level) throws CrtRuntimeException;

    private static native void flushLogger();
}
