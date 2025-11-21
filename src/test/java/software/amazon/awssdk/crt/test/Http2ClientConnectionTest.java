/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.*;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpVersion;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.Http2ConnectionSetting;
import software.amazon.awssdk.crt.http.Http2ClientConnection;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.Http2ClientConnection.Http2ErrorCode;
import software.amazon.awssdk.crt.CrtResource;

public class Http2ClientConnectionTest extends HttpClientTestFixture {

    private final static int MAX_TEST_RETRIES = 5;
    private final static int TEST_RETRY_SLEEP_MILLIS = 2000;
    // crt/aws-c-http/tests/mock_server includes a readme on how the server can be run locally for testing.
    private final static String HOST = "https://localhost:3443";
    private final static HttpVersion EXPECTED_VERSION = HttpVersion.HTTP_2;



    private void doHttp2ConnectionGetVersionTest() {
        try {
            CompletableFuture<Void> shutdownComplete = null;
            boolean actuallyConnected = false;
            URI uri = new URI(HOST);

            try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, EXPECTED_VERSION)) {
                shutdownComplete = connPool.getShutdownCompleteFuture();
                try (HttpClientConnection conn = connPool.acquireConnection().get(60, TimeUnit.SECONDS)) {
                    actuallyConnected = true;
                    Assert.assertTrue(conn.getVersion() == EXPECTED_VERSION);
                }
            }

            Assert.assertTrue(actuallyConnected);

            shutdownComplete.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHttp2ConnectionGetVersion() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        skipIfLocalhostUnavailable();

        TestUtils.doRetryableTest(this::doHttp2ConnectionGetVersionTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doHttp2ConnectionUpdateSettingsTest() {
        try {
            CompletableFuture<Void> shutdownComplete = null;
            boolean actuallyConnected = false;
            URI uri = new URI(HOST);

            try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, EXPECTED_VERSION)) {
                shutdownComplete = connPool.getShutdownCompleteFuture();
                try (Http2ClientConnection conn = (Http2ClientConnection) connPool.acquireConnection().get(60,
                        TimeUnit.SECONDS);) {
                    actuallyConnected = true;
                    Assert.assertTrue(conn.getVersion() == EXPECTED_VERSION);
                    List<Http2ConnectionSetting> settings = new ArrayList<Http2ConnectionSetting>();
                    conn.updateSettings(Http2ConnectionSetting.builder()
                            .enablePush(false)
                            .enablePush(false)
                            .build()).get(5, TimeUnit.SECONDS);
                }
            }

            Assert.assertTrue(actuallyConnected);

            shutdownComplete.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHttp2ConnectionUpdateSettings() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        skipIfLocalhostUnavailable();

        TestUtils.doRetryableTest(this::doHttp2ConnectionUpdateSettingsTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doHttp2ConnectionUpdateSettingsEmptyTest() {
        try {
            CompletableFuture<Void> shutdownComplete = null;
            boolean actuallyConnected = false;
            URI uri = new URI(HOST);

            try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, EXPECTED_VERSION)) {
                shutdownComplete = connPool.getShutdownCompleteFuture();
                try (Http2ClientConnection conn = (Http2ClientConnection) connPool.acquireConnection().get(60,
                        TimeUnit.SECONDS);) {
                    actuallyConnected = true;
                    Assert.assertTrue(conn.getVersion() == EXPECTED_VERSION);
                    conn.updateSettings(Http2ConnectionSetting.builder().build()).get(5, TimeUnit.SECONDS);
                }
            }

            Assert.assertTrue(actuallyConnected);

            shutdownComplete.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHttp2ConnectionUpdateSettingsEmpty() throws Exception {
        skipIfAndroid();
        /* empty settings is allowed to send */
        skipIfNetworkUnavailable();
        skipIfLocalhostUnavailable();

        TestUtils.doRetryableTest(this::doHttp2ConnectionUpdateSettingsEmptyTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doHttp2ConnectionPingTest() {
        try {
            CompletableFuture<Void> shutdownComplete = null;
            boolean actuallyConnected = false;
            URI uri = new URI(HOST);

            try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, EXPECTED_VERSION)) {
                shutdownComplete = connPool.getShutdownCompleteFuture();
                try (Http2ClientConnection conn = (Http2ClientConnection) connPool.acquireConnection().get(60,
                        TimeUnit.SECONDS);) {
                    actuallyConnected = true;
                    Assert.assertTrue(conn.getVersion() == EXPECTED_VERSION);
                    long time = conn.sendPing("12345678".getBytes()).get(5, TimeUnit.SECONDS);
                    Assert.assertNotNull(time);
                    time = conn.sendPing(null).get(5, TimeUnit.SECONDS);
                    Assert.assertNotNull(time);
                }
            }

            Assert.assertTrue(actuallyConnected);

            shutdownComplete.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHttp2ConnectionPing() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        skipIfLocalhostUnavailable();

        TestUtils.doRetryableTest(this::doHttp2ConnectionPingTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doHttp2ConnectionPingExceptionPingDataLengthTest() {
        try {
            CompletableFuture<Void> shutdownComplete = null;
            boolean exception = false;
            URI uri = new URI(HOST);

            try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, EXPECTED_VERSION)) {
                shutdownComplete = connPool.getShutdownCompleteFuture();
                try (Http2ClientConnection conn = (Http2ClientConnection) connPool.acquireConnection().get(60,
                        TimeUnit.SECONDS);) {
                    Assert.assertTrue(conn.getVersion() == EXPECTED_VERSION);
                    long time = conn.sendPing("123".getBytes()).get(5, TimeUnit.SECONDS);
                    Assert.assertNotNull(time);
                }
            } catch (ExecutionException e) {
                try {
                    throw e.getCause();
                } catch (CrtRuntimeException causeException) {
                    exception = true;
                    Assert.assertEquals(causeException.errorName, "AWS_ERROR_INVALID_ARGUMENT");
                } catch (Throwable throwable) {
                    /* Unexpected exception */
                    throwable.printStackTrace();
                }
            }

            Assert.assertTrue(exception);

            shutdownComplete.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHttp2ConnectionPingExceptionPingDataLength() throws Exception {
        skipIfAndroid();
        skipIfNetworkUnavailable();
        skipIfLocalhostUnavailable();

        TestUtils.doRetryableTest(this::doHttp2ConnectionPingExceptionPingDataLengthTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doHttp2ConnectionSendGoAwayTest() {
        try {
            CompletableFuture<Void> shutdownComplete = null;
            boolean actuallyConnected = false;
            URI uri = new URI(HOST);

            try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, EXPECTED_VERSION)) {
                shutdownComplete = connPool.getShutdownCompleteFuture();
                try (Http2ClientConnection conn = (Http2ClientConnection) connPool.acquireConnection().get(60,
                        TimeUnit.SECONDS);) {
                    actuallyConnected = true;
                    Assert.assertTrue(conn.getVersion() == EXPECTED_VERSION);
                    conn.sendGoAway(Http2ErrorCode.INTERNAL_ERROR, false, null);
                }
            }

            Assert.assertTrue(actuallyConnected);

            shutdownComplete.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHttp2ConnectionSendGoAway() throws Exception {
        skipIfAndroid();
        /*
         * Test that the binding works not the actual functionality. C part has the test
         * for functionality
         */
        skipIfNetworkUnavailable();
        skipIfLocalhostUnavailable();

        TestUtils.doRetryableTest(this::doHttp2ConnectionSendGoAwayTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doHttp2ConnectionUpdateConnectionWindowTest() {
        try {
            CompletableFuture<Void> shutdownComplete = null;
            boolean actuallyConnected = false;
            URI uri = new URI(HOST);

            try (HttpClientConnectionManager connPool = createConnectionPoolManager(uri, EXPECTED_VERSION)) {
                shutdownComplete = connPool.getShutdownCompleteFuture();
                try (Http2ClientConnection conn = (Http2ClientConnection) connPool.acquireConnection().get(60,
                        TimeUnit.SECONDS);) {
                    actuallyConnected = true;
                    Assert.assertTrue(conn.getVersion() == EXPECTED_VERSION);
                    conn.updateConnectionWindow(100);
                }
            }

            Assert.assertTrue(actuallyConnected);

            shutdownComplete.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testHttp2ConnectionUpdateConnectionWindow() throws Exception {
        skipIfAndroid();
        /*
         * Test that the binding works not the actual functionality. C part has the test
         * for functionality
         */
        skipIfNetworkUnavailable();
        skipIfLocalhostUnavailable();

        TestUtils.doRetryableTest(this::doHttp2ConnectionUpdateConnectionWindowTest, TestUtils::isRetryableTimeout, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

}
