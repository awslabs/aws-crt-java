/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt;

public class SystemInfo {
    public static int getProcessorCount() {
        return processorCount();
    }

    public static short getCpuGroupCount() {
        return cpuGroupCount();
    }

    public static CpuInfo[] getCpuInfoForGroup(short groupIdx) {
        return cpuInfoForGroup(groupIdx);
    }

    public static class CpuInfo {
        public int cpuId;
        public boolean isSuspectedHyperThread;

        public CpuInfo(int cpuId, boolean isSuspectedHyperThread) {
            this.cpuId = cpuId;
            this.isSuspectedHyperThread = isSuspectedHyperThread;
        }
    }

    /* native functions */
    private static native int processorCount();
    private static native short cpuGroupCount();
    private static native CpuInfo[] cpuInfoForGroup(short groupIdx);
}
