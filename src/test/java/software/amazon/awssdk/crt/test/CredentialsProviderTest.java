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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.auth.CredentialsProvider;
import software.amazon.awssdk.crt.auth.providers.StaticCredentialsProvider;
import software.amazon.awssdk.crt.auth.providers.DefaultChainCredentialsProvider;

public class CredentialsProviderTest {
    static private String ACCESS_KEY_ID = "access_key_id";
    static private String SECRET_ACCESS_KEY = "secret_access_key";
    static private String SESSION_TOKEN = "session_token";

    public CredentialsProviderTest() {}
    
    @Test
    public void testCreateDestroyStatic() {
        StaticCredentialsProviderBuilder builder = new StaticCredentialsProviderBuilder();
        builder.withAccessKeyId(ACCESS_KEY_ID);
        builder.withSecretAccessKey(SECRET_ACCESS_KEY);
        builder.withSessionToken(SESSION_TOKEN);

        try (StaticCredentialsProvider provider = builder.build()) {
            assertNotNull(provider);
            assertTrue(provider.getNativeHandle != 0);
        } catch (CrtRuntimeException ex) {
            fail(ex.getMessage());
        }

        CrtResource.waitForNoResources();
    }

    @Test
    public void testCreateDestroyDefaultChain() {
        try (ClientBootstrap bootstrap = new ClientBootstrap()) {
            DefaultChainCredentialsProviderBuilder builder = new DefaultChainCredentialsProviderBuilder();
            builder.withClientBootstrap(bootstrap);

            try (DefaultChainCredentialsProvider provider = builder.build()) {
                assertNotNull(provider);
                assertTrue(provider.getNativeHandle != 0);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }

        CrtResource.waitForNoResources();
    }
};
