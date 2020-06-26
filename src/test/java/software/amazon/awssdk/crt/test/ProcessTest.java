/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.Process;

public class ProcessTest extends CrtTestFixture  {
    public ProcessTest() {}
    
    @Test
    public void testGetPid() {
        int pid = Process.getPid();
        assertTrue(pid > 0);
    }

    @Test
    public void testGetIOHandleLimits() {
        long softLimit = Process.getMaxIOHandlesSoftLimit();
        long hardLimit = Process.getMaxIOHandlesHardLimit();

        assertTrue(softLimit > 0);
        assertTrue(hardLimit >= softLimit);
    }

    @Test
    public void testSetSoftIOHandleLimits() {
        long softLimit = Process.getMaxIOHandlesSoftLimit();
        long hardLimit = Process.getMaxIOHandlesHardLimit();

        assertTrue(softLimit > 0);
        assertTrue(hardLimit >= softLimit);

        try {
            Process.setMaxIOHandlesSoftLimit(softLimit - 1);
        } catch (CrtRuntimeException ex) {
            // make sure it's the not-implemented exception if it was thrown.
            assertEquals(36, ex.errorCode);
        }
    }
};
