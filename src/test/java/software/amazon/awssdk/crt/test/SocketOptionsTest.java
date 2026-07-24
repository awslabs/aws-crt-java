/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import org.junit.Assert;
import org.junit.Test;

import software.amazon.awssdk.crt.io.SocketOptions;

public class SocketOptionsTest extends CrtTestFixture {

    @Test
    public void testSocketOptionsDefaults() {
        try (SocketOptions options = new SocketOptions()) {
            Assert.assertEquals(SocketOptions.TcpNoDelay.ON, options.tcpNoDelay);
            Assert.assertNotEquals(0, options.getNativeHandle());
        }
    }

    @Test
    public void testSocketOptionsTcpNoDelay() {
        for (SocketOptions.TcpNoDelay tcpNoDelay : SocketOptions.TcpNoDelay.values()) {
            try (SocketOptions options = new SocketOptions()) {
                options.tcpNoDelay = tcpNoDelay;
                Assert.assertNotEquals(0, options.getNativeHandle());
            }
        }
    }
}
