/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.*;

import static org.junit.Assert.*;

public class SystemInfoTest extends CrtTestFixture {
    public SystemInfoTest() { }

    @Test
    public void testCpuIteration() {
        int processorCount = SystemInfo.getProcessorCount();
        assertNotEquals(0, processorCount);

        short cpuGroupCount = SystemInfo.getCpuGroupCount();
        assertNotEquals(0, cpuGroupCount);

        int iteratedCpuCount = 0;
        for (short i = 0; i < cpuGroupCount; ++i) {
            SystemInfo.CpuInfo[] cpus = SystemInfo.getCpuInfoForGroup(i);
            assertNotNull(cpus);
            assertNotEquals(0, cpus.length);

            for (SystemInfo.CpuInfo cpuInfo : cpus) {
                iteratedCpuCount++;
                System.out.println(String.format("Found Cpu %d in group %d. Suspected hyper-thread? %s", cpuInfo.cpuId, i, cpuInfo.isSuspectedHyperThread ? "yes" : "no"));
            }
        }

        assertEquals(processorCount, iteratedCpuCount);
    }
}
