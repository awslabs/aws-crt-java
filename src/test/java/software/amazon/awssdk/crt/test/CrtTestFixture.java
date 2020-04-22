/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtPlatform;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.test.CrtTestContext;

import org.junit.Before;
import org.junit.After;

public class CrtTestFixture {

    private CrtTestContext context;

    public final CrtTestContext getContext() {
        return context;
    }

    @Before
    public void setup() {
        context = new CrtTestContext();
        CrtPlatform platform = CRT.getPlatformImpl();
        if (platform != null) {
            platform.testSetup(context);
        }
    }

    @After
    public void tearDown() {
        CrtPlatform platform = CRT.getPlatformImpl();
        if (platform != null) {
            platform.testTearDown(context);
        }

        context = null;

        CrtResource.waitForNoResources();
    }

    protected TlsContext createTlsContextOptions(byte[] trustStore) {
        try (TlsContextOptions tlsOpts = configureTlsContextOptions(TlsContextOptions.createDefaultClient(),
                trustStore)) {
            return new TlsContext(tlsOpts);
        }        
    }

    protected TlsContextOptions configureTlsContextOptions(TlsContextOptions tlsOpts, byte[] trustStore) {
        if (trustStore != null) {
            tlsOpts.withCertificateAuthority(new String(trustStore));
        }
        return tlsOpts;
    }
}