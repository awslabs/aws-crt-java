/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
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
             throw new CrtRuntimeException(lastError);
         }
    }

    private static native int processGetPid();
    private static native long processGetMaxIOHandlesSoftLimit();
    private static native long processGetMaxIOHandlesHardLimit();
    private static native boolean processSetMaxIOHandlesSoftLimit(long maxHandles);
}
