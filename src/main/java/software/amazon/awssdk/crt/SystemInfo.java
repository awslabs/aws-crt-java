/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt;

public class SystemInfo {

    /**
     * @return active count of processors configured on this system.
     */
    public static int getProcessorCount() {
        return processorCount();
    }

    /**
     * @return number of active Cpu groupings on this system. This currently refers to NUMA nodes.
     */
    public static short getCpuGroupCount() {
        return cpuGroupCount();
    }

    /**
     * Get info on all active Cpus in a Cpu group.
     * @param groupIdx group index to query.
     * @return Array of CpuInfo objects configured for this group. This value is never null even if groupIdx was invalid.
     */
    public static CpuInfo[] getCpuInfoForGroup(short groupIdx) {
        return cpuInfoForGroup(groupIdx);
    }

    public static class CpuInfo {
        /**
         * OS CpuId that can be used for pinning a thread to a specific Cpu
         */
        public final int cpuId;
        /**
         * If true, the Cpu is suspected of being virtual. If false, it's likely a hw core.
         */
        public final boolean isSuspectedHyperThread;

        private CpuInfo(int cpuId, boolean isSuspectedHyperThread) {
            this.cpuId = cpuId;
            this.isSuspectedHyperThread = isSuspectedHyperThread;
        }
    }

    /* native functions */
    private static native int processorCount();
    private static native short cpuGroupCount();
    private static native CpuInfo[] cpuInfoForGroup(short groupIdx);
}
