/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpConnection;
import software.amazon.awssdk.crt.http.HttpConnectionPoolManager;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsCipherPreference;
import software.amazon.awssdk.crt.io.TlsContext;

import java.net.URI;
import software.amazon.awssdk.crt.io.TlsContextOptions;

public class HttpConnectionTest {

    private class HttpConnectionTestResponse {
        boolean actuallyConnected = false;
        boolean exceptionThrown = false;
        Exception exception = null;
    }

    private HttpConnectionTestResponse testConnection(URI uri, ClientBootstrap bootstrap, SocketOptions sockOpts, TlsContext tlsContext) {
        HttpConnectionTestResponse resp = new HttpConnectionTestResponse();
        try {
            HttpConnectionPoolManager connectionPool = new HttpConnectionPoolManager(bootstrap, sockOpts, tlsContext, uri);
            HttpConnection conn = connectionPool.acquireConnection().get(60, TimeUnit.SECONDS);
            resp.actuallyConnected = true;
            conn.close();
            connectionPool.close();
        } catch (Exception e) {
            resp.exceptionThrown = true;
            resp.exception = e;
        } finally {
            tlsContext.close();
            sockOpts.close();
            bootstrap.close();
        }

        return resp;
    }

    private void testConnectionWithAllCiphers(URI uri, boolean expectConnected, String exceptionMsg) throws CrtRuntimeException {
        for (TlsCipherPreference pref: TlsCipherPreference.values()) {
            if (!TlsContextOptions.isCipherPreferenceSupported(pref)) {
                continue;
            }

            TlsContextOptions tlsOpts = new TlsContextOptions().withCipherPreference(pref);
            HttpConnectionTestResponse resp = testConnection(uri, new ClientBootstrap(1), new SocketOptions(), new TlsContext(tlsOpts));
            tlsOpts.close();

            Assert.assertEquals("URI: " + uri.toString(), expectConnected, resp.actuallyConnected);
            Assert.assertEquals("URI: " + uri.toString(), expectConnected, !resp.exceptionThrown);
            if (resp.exception != null) {
                Assert.assertTrue(resp.exception.getMessage(), resp.exception.getMessage().contains(exceptionMsg));
            }

            Assert.assertEquals(0, CrtResource.getAllocatedNativeResourceCount());
        }
    }

    @Test
    public void testHttpConnection() throws Exception {
        // S3
        testConnectionWithAllCiphers(new URI("https://aws-crt-test-stuff.s3.amazonaws.com"), true, null);
        testConnectionWithAllCiphers(new URI("http://aws-crt-test-stuff.s3.amazonaws.com"), true, null);
        testConnectionWithAllCiphers(new URI("http://aws-crt-test-stuff.s3.amazonaws.com:80"), true, null);
        testConnectionWithAllCiphers(new URI("http://aws-crt-test-stuff.s3.amazonaws.com:443"), true, null);
        testConnectionWithAllCiphers(new URI("https://aws-crt-test-stuff.s3.amazonaws.com:443"), true, null);

        // KMS
        testConnectionWithAllCiphers(new URI("https://kms.us-east-1.amazonaws.com:443"), true, null);
        testConnectionWithAllCiphers(new URI("https://kms-fips.us-east-1.amazonaws.com:443"), true, null);
        testConnectionWithAllCiphers(new URI("https://kms.us-west-2.amazonaws.com:443"), true, null);
        testConnectionWithAllCiphers(new URI("https://kms-fips.us-west-2.amazonaws.com:443"), true, null);

        // BadSSL
        testConnectionWithAllCiphers(new URI("https://rsa2048.badssl.com/"), true, null);
        testConnectionWithAllCiphers(new URI("http://http.badssl.com/"), true, null);
        testConnectionWithAllCiphers(new URI("https://expired.badssl.com/"), false, "TLS (SSL) negotiation failed");
        testConnectionWithAllCiphers(new URI("https://self-signed.badssl.com/"), false, "TLS (SSL) negotiation failed");
    }
}
