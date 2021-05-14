/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.X509CredentialsProvider;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpStream;
import software.amazon.awssdk.crt.http.HttpStreamResponseHandler;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.ClientTlsContext;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt.MqttClient;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttConnectionConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/*

# AWS_TEST_HTTP_PROXY_HOST - host address of the proxy to use for tests that make open connections to the proxy
# AWS_TEST_HTTP_PROXY_PORT - port to use for tests that make open connections to the proxy
# AWS_TEST_HTTPS_PROXY_HOST - host address of the proxy to use for tests that make tls-protected connections to the proxy
# AWS_TEST_HTTPS_PROXY_PORT - port to use for tests that make tls-protected connections to the proxy
# AWS_TEST_HTTP_PROXY_BASIC_HOST - host address of the proxy to use for tests that make open connections to the proxy with basic authentication
# AWS_TEST_HTTP_PROXY_BASIC_PORT - port to use for tests that make open connections to the proxy with basic authentication

# AWS_TEST_BASIC_AUTH_USERNAME - username to use when using basic authentication to the proxy
# AWS_TEST_BASIC_AUTH_PASSWORD - password to use when using basic authentication to the proxy

# AWS_TEST_TLS_CERT_PATH - file path to certificate used to initialize the tls context of the x509 provider connection
# AWS_TEST_TLS_KEY_PATH - file path to the key used to initialize the tls context of the x509 provider connection
# AWS_TEST_TLS_ROOT_CERT_PATH - file path to the root CA used to initialize the tls context of the x509 provider connection
# AWS_TEST_X509_ENDPOINT - AWS account-specific endpoint to source x509 credentials from
# AWS_TEST_X509_THING_NAME - associated name of the x509 thing
# AWS_TEST_X509_ROLE_ALIAS - associated role alias ...

# AWS_TEST_IOT_SIGNING_REGION - AWS region to make a websocket connection to
# AWS_TEST_IOT_MQTT_ENDPOINT - AWS account-specific endpoint to connect to IoT core by

 */
public class ProxyTest extends CrtTestFixture  {

    enum ProxyTestType {
        FORWARDING,
        TUNNELING_HTTP,
        TUNNELING_HTTPS,
        TUNNELING_DOUBLE_TLS,
        LEGACY_HTTP,
        LEGACY_HTTPS,
    }

    enum ProxyAuthType {
        None,
        Basic
    }

    private static String HTTP_PROXY_HOST = System.getenv("AWS_TEST_HTTP_PROXY_HOST");
    private static String HTTP_PROXY_PORT = System.getenv("AWS_TEST_HTTP_PROXY_PORT");
    private static String HTTPS_PROXY_HOST = System.getenv("AWS_TEST_HTTPS_PROXY_HOST");
    private static String HTTPS_PROXY_PORT = System.getenv("AWS_TEST_HTTPS_PROXY_PORT");
    private static String HTTP_PROXY_BASIC_HOST = System.getenv("AWS_TEST_HTTP_PROXY_BASIC_HOST");
    private static String HTTP_PROXY_BASIC_PORT = System.getenv("AWS_TEST_HTTP_PROXY_BASIC_PORT");

    private static String HTTP_PROXY_BASIC_AUTH_USERNAME = System.getenv("AWS_TEST_BASIC_AUTH_USERNAME");
    private static String HTTP_PROXY_BASIC_AUTH_PASSWORD = System.getenv("AWS_TEST_BASIC_AUTH_PASSWORD");

    private static String X509_CERT_PATH = System.getenv("AWS_TEST_TLS_CERT_PATH");
    private static String X509_KEY_PATH = System.getenv("AWS_TEST_TLS_KEY_PATH");
    private static String X509_ROOT_CA_PATH = System.getenv("AWS_TEST_TLS_ROOT_CERT_PATH");

    private static String X509_CREDENTIALS_ENDPOINT = System.getenv("AWS_TEST_X509_ENDPOINT");
    private static String X509_CREDENTIALS_THING_NAME = System.getenv("AWS_TEST_X509_THING_NAME");
    private static String X509_CREDENTIALS_ROLE_ALIAS = System.getenv("AWS_TEST_X509_ROLE_ALIAS");

    private static String MQTT_WEBSOCKET_REGION = System.getenv("AWS_TEST_IOT_SIGNING_REGION");
    private static String MQTT_ENDPOINT = System.getenv("AWS_TEST_IOT_MQTT_ENDPOINT");

    private static String PROXY_TEST_CLIENTID = "ProxyTest-";
    private static final short MQTT_DIRECT_PORT = 8883;

    public ProxyTest() {}

    private boolean isEnvironmentSetUpForProxyTests() {
        if (HTTP_PROXY_HOST == null || HTTP_PROXY_PORT == null || HTTPS_PROXY_HOST == null || HTTPS_PROXY_PORT == null || HTTP_PROXY_BASIC_HOST == null || HTTP_PROXY_BASIC_PORT == null) {
            return false;
        }

        if (HTTP_PROXY_BASIC_AUTH_USERNAME == null || HTTP_PROXY_BASIC_AUTH_PASSWORD == null) {
            return false;
        }

        if (X509_CERT_PATH == null || X509_KEY_PATH == null || X509_ROOT_CA_PATH == null || X509_CREDENTIALS_ENDPOINT == null || X509_CREDENTIALS_THING_NAME == null || X509_CREDENTIALS_ROLE_ALIAS == null) {
            return false;
        }

        if (MQTT_WEBSOCKET_REGION == null || MQTT_ENDPOINT == null) {
            return false;
        }

        return true;
    }

    private TlsContext createHttpClientTlsContext() {
        try (TlsContextOptions options = TlsContextOptions.createDefaultClient()) {
            return new ClientTlsContext(options);
        }
    }

    private TlsContext createProxyTlsContext(ProxyTestType testType) {
        if (testType == ProxyTestType.TUNNELING_DOUBLE_TLS) {
            try (TlsContextOptions options = TlsContextOptions.createDefaultClient()) {
                options.verifyPeer = false;

                return new ClientTlsContext(options);
            }
        }

        return null;
    }

    private String getProxyHostForTest(ProxyTestType testType, ProxyAuthType authType) {
        if (authType == ProxyAuthType.Basic) {
            return HTTP_PROXY_BASIC_HOST;
        }

        if (testType == ProxyTestType.TUNNELING_DOUBLE_TLS) {
            return HTTPS_PROXY_HOST;
        }

        return HTTP_PROXY_HOST;
    }

    private int getProxyPortForTest(ProxyTestType testType, ProxyAuthType authType) {
        if (authType == ProxyAuthType.Basic) {
            return Integer.parseInt(HTTP_PROXY_BASIC_PORT);
        }

        if (testType == ProxyTestType.TUNNELING_DOUBLE_TLS) {
            return Integer.parseInt(HTTPS_PROXY_PORT);
        }

        return Integer.parseInt(HTTP_PROXY_PORT);
    }

    private URI getUriForTest(ProxyTestType testType) {
        try {
            switch (testType) {
                case TUNNELING_HTTPS:
                case TUNNELING_DOUBLE_TLS:
                case LEGACY_HTTPS:
                    return new URI("https://s3.amazonaws.com");
                default:
                    return new URI("http://www.example.com");

            }
        } catch (URISyntaxException e) {
            throw new CrtRuntimeException(e.toString());
        }
    }

    private HttpProxyOptions.HttpProxyConnectionType getProxyConnectionTypeForTest(ProxyTestType testType) {
        switch(testType) {
            case FORWARDING:
                return HttpProxyOptions.HttpProxyConnectionType.Forwarding;

            case TUNNELING_HTTP:
            case TUNNELING_HTTPS:
            case TUNNELING_DOUBLE_TLS:
                return HttpProxyOptions.HttpProxyConnectionType.Tunneling;

            default:
                return HttpProxyOptions.HttpProxyConnectionType.Legacy;
        }
    }

    private HttpProxyOptions buildProxyOptions(ProxyTestType testType, ProxyAuthType authType, TlsContext proxyTlsContext) {
        HttpProxyOptions proxyOptions = new HttpProxyOptions();
        proxyOptions.setHost(getProxyHostForTest(testType, authType));
        proxyOptions.setPort(getProxyPortForTest(testType, authType));
        proxyOptions.setConnectionType(getProxyConnectionTypeForTest(testType));
        proxyOptions.setTlsContext(proxyTlsContext);
        if (authType == ProxyAuthType.Basic) {
            proxyOptions.setAuthorizationType(HttpProxyOptions.HttpProxyAuthorizationType.Basic);
            proxyOptions.setAuthorizationUsername(HTTP_PROXY_BASIC_AUTH_USERNAME);
            proxyOptions.setAuthorizationPassword(HTTP_PROXY_BASIC_AUTH_PASSWORD);
        }

        return proxyOptions;
    }

    private HttpClientConnectionManager buildProxiedConnectionManager(ProxyTestType testType, ProxyAuthType authType) {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
             HostResolver resolver = new HostResolver(eventLoopGroup);
             ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
             SocketOptions sockOpts = new SocketOptions();
             TlsContext tlsContext = createHttpClientTlsContext();
             TlsContext proxyTlsContext = createProxyTlsContext(testType)) {

            HttpProxyOptions proxyOptions = buildProxyOptions(testType, authType, proxyTlsContext);

            HttpClientConnectionManagerOptions options = new HttpClientConnectionManagerOptions();
            options.withClientBootstrap(bootstrap)
                    .withSocketOptions(sockOpts)
                    .withTlsContext(tlsContext)
                    .withUri(getUriForTest(testType))
                    .withMaxConnections(1)
                    .withProxyOptions(proxyOptions);

            return HttpClientConnectionManager.create(options);
        }
    }

    private void doHttpConnectionManagerProxyTest(HttpClientConnectionManager manager) {
        HttpRequest request = new HttpRequest("GET", "/");

        CompletableFuture requestCompleteFuture = new CompletableFuture();

        manager.acquireConnection()
                // When the connection is acquired, submit a request on it
                .whenComplete((conn, throwable) -> {
                    if (throwable != null) {
                        requestCompleteFuture.completeExceptionally(throwable);
                    }

                    HttpStream stream = conn.makeRequest(request, new HttpStreamResponseHandler() {
                        @Override
                        public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType,
                                                      HttpHeader[] nextHeaders) {
                            ;
                        }

                        @Override
                        public void onResponseComplete(HttpStream stream, int errorCode) {
                            // When this Request is complete, release the conn back to the pool
                            manager.releaseConnection(conn);
                            stream.close();
                            if (errorCode != CRT.AWS_CRT_SUCCESS) {
                                requestCompleteFuture.completeExceptionally(new CrtRuntimeException(errorCode));
                            } else {
                                requestCompleteFuture.complete(null);
                            }
                        }
                    });

                    if (stream != null) {
                        stream.activate();
                    }
                });

        requestCompleteFuture.join();
    }

    @Test
    public void testConnectionManager_ForwardingProxy_NoAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (HttpClientConnectionManager manager = buildProxiedConnectionManager(ProxyTestType.FORWARDING, ProxyAuthType.None)) {
            doHttpConnectionManagerProxyTest(manager);
        }
    }

    @Test
    public void testConnectionManager_LegacyHttpProxy_NoAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (HttpClientConnectionManager manager = buildProxiedConnectionManager(ProxyTestType.LEGACY_HTTP, ProxyAuthType.None)) {
            doHttpConnectionManagerProxyTest(manager);
        }
    }

    @Test
    public void testConnectionManager_LegacyHttpsProxy_NoAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (HttpClientConnectionManager manager = buildProxiedConnectionManager(ProxyTestType.LEGACY_HTTPS, ProxyAuthType.None)) {
            doHttpConnectionManagerProxyTest(manager);
        }
    }

    @Test
    public void testConnectionManager_TunnelingHttpProxy_NoAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (HttpClientConnectionManager manager = buildProxiedConnectionManager(ProxyTestType.TUNNELING_HTTP, ProxyAuthType.None)) {
            doHttpConnectionManagerProxyTest(manager);
        }
    }

    @Test
    public void testConnectionManager_TunnelingHttpsProxy_NoAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (HttpClientConnectionManager manager = buildProxiedConnectionManager(ProxyTestType.TUNNELING_HTTPS, ProxyAuthType.None)) {
            doHttpConnectionManagerProxyTest(manager);
        }
    }

    @Test
    public void testConnectionManager_TunnelingHttpsProxy_NoAuth_DoubleTls() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (HttpClientConnectionManager manager = buildProxiedConnectionManager(ProxyTestType.TUNNELING_DOUBLE_TLS, ProxyAuthType.None)) {
            doHttpConnectionManagerProxyTest(manager);
        }
    }

    @Test
    public void testConnectionManager_ForwardingProxy_BasicAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (HttpClientConnectionManager manager = buildProxiedConnectionManager(ProxyTestType.FORWARDING, ProxyAuthType.Basic)) {
            doHttpConnectionManagerProxyTest(manager);
        }
    }

    @Test
    public void testConnectionManager_LegacyHttpProxy_BasicAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (HttpClientConnectionManager manager = buildProxiedConnectionManager(ProxyTestType.LEGACY_HTTP, ProxyAuthType.Basic)) {
            doHttpConnectionManagerProxyTest(manager);
        }
    }

    @Test
    public void testConnectionManager_LegacyHttpsProxy_BasicAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (HttpClientConnectionManager manager = buildProxiedConnectionManager(ProxyTestType.LEGACY_HTTPS, ProxyAuthType.Basic)) {
            doHttpConnectionManagerProxyTest(manager);
        }
    }

    @Test
    public void testConnectionManager_TunnelingHttpProxy_BasicAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (HttpClientConnectionManager manager = buildProxiedConnectionManager(ProxyTestType.TUNNELING_HTTP, ProxyAuthType.Basic)) {
            doHttpConnectionManagerProxyTest(manager);
        }
    }

    @Test
    public void testConnectionManager_TunnelingHttpsProxy_BasicAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (HttpClientConnectionManager manager = buildProxiedConnectionManager(ProxyTestType.TUNNELING_HTTPS, ProxyAuthType.Basic)) {
            doHttpConnectionManagerProxyTest(manager);
        }
    }

    private void doCredentialsProviderProxyTest(CredentialsProvider provider) {
        try {
            Credentials credentials = provider.getCredentials().get();
            Assert.assertNotNull(credentials);
        } catch (Exception e) {
            throw new CrtRuntimeException(e.toString());
        }
    }

    private TlsContext createX509TlsContext(String alpn) {
        try (TlsContextOptions options = TlsContextOptions.createWithMtlsFromPath(X509_CERT_PATH, X509_KEY_PATH)) {
            options.withCertificateAuthorityFromPath(null, X509_ROOT_CA_PATH);
            if (alpn != null) {
                options.withAlpnList(alpn);
            }

            return new ClientTlsContext(options);
        }
    }

    private CredentialsProvider buildProxiedX509CredentialsProvider(ProxyTestType testType, ProxyAuthType authType) {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
             HostResolver resolver = new HostResolver(eventLoopGroup);
             ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
             TlsContext tlsContext = createX509TlsContext(null);
             TlsContext proxyTlsContext = createProxyTlsContext(testType)) {

            HttpProxyOptions proxyOptions = buildProxyOptions(testType, authType, proxyTlsContext);

            X509CredentialsProvider.X509CredentialsProviderBuilder builder = new X509CredentialsProvider.X509CredentialsProviderBuilder();
            builder.withClientBootstrap(bootstrap)
                    .withEndpoint(X509_CREDENTIALS_ENDPOINT)
                    .withProxyOptions(proxyOptions)
                    .withRoleAlias(X509_CREDENTIALS_ROLE_ALIAS)
                    .withThingName(X509_CREDENTIALS_THING_NAME)
                    .withTlsContext(tlsContext);

            return builder.build();
        }
    }

    @Test
    public void testX509Credentials_TunnelingProxy_NoAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (CredentialsProvider provider = buildProxiedX509CredentialsProvider(ProxyTestType.TUNNELING_HTTPS, ProxyAuthType.None)) {
            doCredentialsProviderProxyTest(provider);
        }
    }

    @Test
    public void testX509Credentials_TunnelingProxy_DoubleTls_NoAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (CredentialsProvider provider = buildProxiedX509CredentialsProvider(ProxyTestType.TUNNELING_DOUBLE_TLS, ProxyAuthType.None)) {
            doCredentialsProviderProxyTest(provider);
        }
    }

    @Test
    public void testX509Credentials_TunnelingProxy_BasicAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (CredentialsProvider provider = buildProxiedX509CredentialsProvider(ProxyTestType.TUNNELING_HTTPS, ProxyAuthType.Basic)) {
            doCredentialsProviderProxyTest(provider);
        }
    }

    private MqttClientConnection buildDirectMqttConnection(ProxyTestType testType, ProxyAuthType authType) {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
             HostResolver resolver = new HostResolver(eventLoopGroup);
             ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
             TlsContext tlsContext = createX509TlsContext(null);
             MqttClient mqttClient = new MqttClient(bootstrap, tlsContext);
             MqttConnectionConfig connectionConfig = new MqttConnectionConfig();
             TlsContext proxyTlsContext = createProxyTlsContext(testType)) {

            HttpProxyOptions proxyOptions = buildProxyOptions(testType, authType, proxyTlsContext);
            String clientId = PROXY_TEST_CLIENTID + (UUID.randomUUID()).toString();

            connectionConfig.setMqttClient(mqttClient);
            connectionConfig.setEndpoint(MQTT_ENDPOINT);
            connectionConfig.setHttpProxyOptions(proxyOptions);
            connectionConfig.setCleanSession(true);
            connectionConfig.setClientId(clientId);
            connectionConfig.setPort(MQTT_DIRECT_PORT);
            connectionConfig.setProtocolOperationTimeoutMs(60000);

            return new MqttClientConnection(connectionConfig);
        }
    }

    private void doMqttConnectTest(MqttClientConnection connection) {
        CompletableFuture<Boolean> connectFuture = connection.connect();
        connectFuture.join();
    }

    @Test
    public void testMqttDirect_TunnelingProxy_NoAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (MqttClientConnection connection = buildDirectMqttConnection(ProxyTestType.TUNNELING_HTTPS, ProxyAuthType.None)) {
            doMqttConnectTest(connection);
        }
    }

    @Test
    public void testMqttDirect_TunnelingProxy_DoubleTls_NoAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (MqttClientConnection connection = buildDirectMqttConnection(ProxyTestType.TUNNELING_DOUBLE_TLS, ProxyAuthType.None)) {
            doMqttConnectTest(connection);
        }
    }

    @Test
    public void testMqttDirect_TunnelingProxy_BasicAuth() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(isEnvironmentSetUpForProxyTests());

        try (MqttClientConnection connection = buildDirectMqttConnection(ProxyTestType.TUNNELING_HTTPS, ProxyAuthType.Basic)) {
            doMqttConnectTest(connection);
        }
    }
}
