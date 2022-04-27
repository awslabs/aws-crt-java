/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class ClientBootstrapTest extends CrtTestFixture {
    public ClientBootstrapTest() {}
    
    @Test
    public void testCreateDestroy() throws ExecutionException, InterruptedException {
        EventLoopGroup elg = new EventLoopGroup(1);
        HostResolver hostResolver = new HostResolver(elg);
        ClientBootstrap bootstrap = new ClientBootstrap(elg, hostResolver);

        assertNotNull(bootstrap);
    }
};
