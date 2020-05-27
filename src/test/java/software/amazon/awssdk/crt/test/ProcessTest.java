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
