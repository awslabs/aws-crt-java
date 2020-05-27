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
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
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
        bootstrap.close();
        bootstrap.getShutdownCompleteFuture().get();
        hostResolver.close();
        elg.close();
    }
};
