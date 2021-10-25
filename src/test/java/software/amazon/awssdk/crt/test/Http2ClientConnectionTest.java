/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import org.junit.Assert;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.Http2ClientConnection;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.Log;

public class Http2ClientConnectionTest extends HttpClientTestFixture {
    protected final static String HOST = "https://httpbin.org";
    protected final static HttpClientConnection.AwsHTTPProtocolVersion EXPECTED_VERSION = HttpClientConnection.AwsHTTPProtocolVersion.HTTP_2;

    @Test
    public void testHttp2ConnectionGetVersion() throws Exception {
        skipIfNetworkUnavailable();

        CompletableFuture<Void> shutdownComplete = null;
        boolean actuallyConnected = false;
        URI uri = new URI(HOST);

        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, EXPECTED_VERSION)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (HttpClientConnection conn = connPool.acquireConnection().get(60, TimeUnit.SECONDS)) {
                actuallyConnected = true;
                Assert.assertTrue(conn.getVersion() == EXPECTED_VERSION);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assert.assertTrue(actuallyConnected);

        shutdownComplete.get(60, TimeUnit.SECONDS);

        CrtResource.waitForNoResources();
    }

    @Test
    public void testHttp2ConnectionPing() throws Exception {
        skipIfNetworkUnavailable();

        CompletableFuture<Void> shutdownComplete = null;
        boolean actuallyConnected = false;
        URI uri = new URI(HOST);

        try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, EXPECTED_VERSION)) {
            shutdownComplete = connPool.getShutdownCompleteFuture();
            try (Http2ClientConnection conn = (Http2ClientConnection) connPool.acquireConnection().get(60,
                    TimeUnit.SECONDS);) {
                actuallyConnected = true;
                Assert.assertTrue(conn.getVersion() == EXPECTED_VERSION);
                long time = conn.sendPing(null).get(5, TimeUnit.SECONDS);
                Assert.assertNotNull(time);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Assert.assertTrue(actuallyConnected);

        shutdownComplete.get(60, TimeUnit.SECONDS);

        CrtResource.waitForNoResources();
    }

}
