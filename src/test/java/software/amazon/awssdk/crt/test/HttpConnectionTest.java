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

import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpConnection;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;

import java.net.URI;

public class HttpConnectionTest {

    protected void testConnection(URI uri, boolean expectConnected, String exceptionMsg) throws CrtRuntimeException {
        boolean actuallyConnected = false;
        boolean exceptionThrown = false;

        ClientBootstrap bootstrap = new ClientBootstrap(1);
        SocketOptions sockOpts = new SocketOptions();
        TlsContext tlsContext =  new TlsContext();

        try {
            HttpConnection conn = HttpConnection.createConnection(uri, bootstrap, sockOpts, tlsContext).get();
            actuallyConnected = true;
            conn.shutdown().get();
            conn.close();
        } catch (Exception e) {
            exceptionThrown = true;
            Assert.assertTrue(e.getMessage(), e.getMessage().contains(exceptionMsg));
        } finally {
            tlsContext.close();
            sockOpts.close();
            bootstrap.close();
        }

        Assert.assertEquals("URI: " + uri.toString(), expectConnected, actuallyConnected);
        Assert.assertEquals("URI: " + uri.toString(), expectConnected, !exceptionThrown);
        Assert.assertEquals(0, CrtResource.getAllocatedNativeResourceCount());
    }

    @Test
    public void testHttpConnection() throws Exception {
        testConnection(new URI("https://aws-crt-test-stuff.s3.amazonaws.com"), true, null);
        testConnection(new URI("http://aws-crt-test-stuff.s3.amazonaws.com"), true, null);
        testConnection(new URI("http://aws-crt-test-stuff.s3.amazonaws.com:80"), true, null);
        testConnection(new URI("http://aws-crt-test-stuff.s3.amazonaws.com:443"), true, null);
        testConnection(new URI("https://aws-crt-test-stuff.s3.amazonaws.com:443"), true, null);
        testConnection(new URI("https://rsa2048.badssl.com/"), true, null);
        testConnection(new URI("http://http.badssl.com/"), true, null);
        testConnection(new URI("https://expired.badssl.com/"), false, "TLS (SSL) negotiation failed");
        testConnection(new URI("https://self-signed.badssl.com/"), false, "TLS (SSL) negotiation failed");
    }
}
