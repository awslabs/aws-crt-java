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

import java.lang.InterruptedException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;

public class CredentialsProviderTest extends CrtTestFixture  {
    static private String ACCESS_KEY_ID = "access_key_id";
    static private String SECRET_ACCESS_KEY = "secret_access_key";
    static private String SESSION_TOKEN = "session_token";

    public CredentialsProviderTest() {}

    @Test
    public void testCreateDestroyStatic() {
        StaticCredentialsProvider.StaticCredentialsProviderBuilder builder = new StaticCredentialsProvider.StaticCredentialsProviderBuilder();
        builder.withAccessKeyId(ACCESS_KEY_ID.getBytes());
        builder.withSecretAccessKey(SECRET_ACCESS_KEY.getBytes());
        builder.withSessionToken(SESSION_TOKEN.getBytes());

        try (StaticCredentialsProvider provider = builder.build()) {
            assertNotNull(provider);
            assertTrue(provider.getNativeHandle() != 0);
        } catch (CrtRuntimeException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testGetCredentialsStatic() {
        StaticCredentialsProvider.StaticCredentialsProviderBuilder builder = new StaticCredentialsProvider.StaticCredentialsProviderBuilder();
        builder.withAccessKeyId(ACCESS_KEY_ID.getBytes());
        builder.withSecretAccessKey(SECRET_ACCESS_KEY.getBytes());
        builder.withSessionToken(SESSION_TOKEN.getBytes());

        try (StaticCredentialsProvider provider = builder.build()) {
            CompletableFuture<Credentials> future = provider.getCredentials();
            Credentials credentials = future.get();
            assertTrue(Arrays.equals(credentials.getAccessKeyId(), ACCESS_KEY_ID.getBytes()));
            assertTrue(Arrays.equals(credentials.getSecretAccessKey(), SECRET_ACCESS_KEY.getBytes()));
            assertTrue(Arrays.equals(credentials.getSessionToken(), SESSION_TOKEN.getBytes()));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }


    @Test
    public void testCreateDestroyDefaultChain() {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
             HostResolver resolver = new HostResolver(eventLoopGroup);
             ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver)) {
            DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder builder = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder();
            builder.withClientBootstrap(bootstrap);

            try (DefaultChainCredentialsProvider provider = builder.build()) {
                assertNotNull(provider);
                assertTrue(provider.getNativeHandle() != 0);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetCredentialsDefaultChain() {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
             HostResolver resolver = new HostResolver(eventLoopGroup);
             ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver)) {
            DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder builder = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder();
            builder.withClientBootstrap(bootstrap);

            try (DefaultChainCredentialsProvider provider = builder.build()) {
                CompletableFuture<Credentials> future = provider.getCredentials();
                /*
                 * This may or may not succeed depending on the test environment and setting up the environment to force the test
                 * to always succeed or fail would mean modifying the environment from Java, which is gross.
                 */
                Credentials credentials = future.get();
            }
        } catch (Exception e) {
            ;
        }
    }

};
