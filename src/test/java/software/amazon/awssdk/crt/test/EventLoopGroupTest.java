/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.io.EventLoopGroup;

public class EventLoopGroupTest extends CrtTestFixture  {
    public EventLoopGroupTest() {}
    
    @Test
    public void testCreateDestroy() {
        try (EventLoopGroup elg = new EventLoopGroup(1)) {
            assertNotNull(elg);
            assertTrue(!elg.isNull());
        } catch (CrtRuntimeException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testCreateDestroyWithCpuGroup() {
        try (EventLoopGroup elg = new EventLoopGroup(0,2)) {
            assertNotNull(elg);
            assertTrue(!elg.isNull());
        } catch (CrtRuntimeException ex) {
            fail(ex.getMessage());
        }
    }
};
