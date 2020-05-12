/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import static software.amazon.awssdk.crt.CRT.awsErrorString;
import static software.amazon.awssdk.crt.CRT.awsLastError;

/**
 * Encapsulates Process information and manipulation of process level operations.
 */
public class Process {
    /**
     * Gets the process id of the running process.
     * @return process id.
     */
    public static int getPid() {
        return processGetPid();
    }

    /**
     * Gets the soft limit for IO handles for this process (max fds in unix terminology)
     *
     * @return soft limit for IO handles.
     */
    public static long getMaxIOHandlesSoftLimit() {
        return processGetMaxIOHandlesSoftLimit();
    }

    /**
     * Gets the hard limit for IO handles for this process (max fds in unix terminology). This
     * value cannot be altered without root permissions.
     *
     * @return hard limit for IO handles.
     */
    public static long getMaxIOHandlesHardLimit() {
        return processGetMaxIOHandlesHardLimit();
    }

    /**
     * Sets the soft limit for IO handles for this process (max fds in unix terminology). maxHandles may not exceed the
     * return value of getMaxIOHandlesHardLimit(). In addition, avoid calling this function unless you've checked
     * getMaxIOHandlesSoftLimit() is actually less than getMaxIOHandlesHardLimit() since this function will always
     * fail on some platforms (such as windows) where there are no practical limits in the first place.
     *
     * @param maxHandles new soft limit for this process.
     *
     * @throws CrtRuntimeException if the operation fails due to illegal arguments or the opereration is unsupported on
     * the current platform.
     */
    public static void setMaxIOHandlesSoftLimit(long maxHandles) {
         if (!processSetMaxIOHandlesSoftLimit(maxHandles)) {
             int lastError = awsLastError();
             throw new CrtRuntimeException(lastError, awsErrorString(lastError));
         }
    }

    private static native int processGetPid();
    private static native long processGetMaxIOHandlesSoftLimit();
    private static native long processGetMaxIOHandlesHardLimit();
    private static native boolean processSetMaxIOHandlesSoftLimit(long maxHandles);
}
