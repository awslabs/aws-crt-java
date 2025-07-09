package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.CognitoCredentialsProvider.CognitoCredentialsProviderBuilder;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder;
import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider.StaticCredentialsProviderBuilder;
import software.amazon.awssdk.crt.auth.credentials.X509CredentialsProvider.X509CredentialsProviderBuilder;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig.AwsSigningAlgorithm;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.http.HttpProxyOptions.HttpProxyConnectionType;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.Pkcs11Lib;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.io.TlsContextPkcs11Options;
import software.amazon.awssdk.crt.io.ExponentialBackoffRetryOptions.JitterMode;
import software.amazon.awssdk.crt.mqtt5.*;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.ClientOfflineQueueBehavior;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.ClientSessionBehavior;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.ExtendedValidationAndFlowControlOptions;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.LifecycleEvents;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.Mqtt5ClientOptionsBuilder;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.PublishEvents;
import software.amazon.awssdk.crt.mqtt5.packets.*;
import software.amazon.awssdk.crt.mqtt5.packets.ConnectPacket.ConnectPacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.DisconnectPacket.DisconnectPacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.DisconnectPacket.DisconnectReasonCode;
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket.PublishPacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.SubscribePacket.SubscribePacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.UnsubscribePacket.UnsubscribePacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.SubscribePacket.RetainHandlingType;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/* For environment variable setup, see SetupCrossCICrtEnvironment in the CRT builder */
public class Mqtt5ClientTest extends Mqtt5ClientTestFixture {

    private final int MAX_TEST_RETRIES = 3;
    private final int TEST_RETRY_SLEEP_MILLIS = 3000;

    public Mqtt5ClientTest() {
    }

    /**
     * ============================================================
     * CREATION TEST CASES
     * ============================================================
     */

    /* Happy path. Minimal creation and cleanup */
    @Test
    public void New_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_DIRECT_MQTT_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_PORT);
        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                AWS_TEST_MQTT5_DIRECT_MQTT_HOST,
                Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_PORT));
            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                assertNotNull(client);
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        CrtResource.waitForNoResources();
    }

    /* Maximum creation and cleanup */
    @Test
    public void New_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_DIRECT_MQTT_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_PORT,
            AWS_TEST_MQTT5_BASIC_AUTH_USERNAME, AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD,
            AWS_TEST_MQTT5_PROXY_HOST, AWS_TEST_MQTT5_PROXY_PORT);
        try {

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
                SocketOptions socketOptions = new SocketOptions();
            ) {

                PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder("test/topic", QOS.AT_LEAST_ONCE, "Hello World".getBytes());

                ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
                connectBuilder.withClientId("MQTT5 CRT")
                .withKeepAliveIntervalSeconds(1000L)
                .withMaximumPacketSizeBytes(1000L)
                .withPassword(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD.getBytes())
                .withReceiveMaximum(1000L)
                .withRequestProblemInformation(true)
                .withRequestResponseInformation(true)
                .withSessionExpiryIntervalSeconds(1000L)
                .withUsername(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME)
                .withWill(willPacketBuilder.build())
                .withWillDelayIntervalSeconds(1000L);

                ArrayList<UserProperty> userProperties = new ArrayList<UserProperty>();
                userProperties.add(new UserProperty("Hello", "World"));
                connectBuilder.withUserProperties(userProperties);

                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_HOST,
                    Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_PORT));
                builder.withBootstrap(bootstrap)
                .withConnackTimeoutMs(100L)
                .withConnectOptions(connectBuilder.build())
                .withExtendedValidationAndFlowControlOptions(ExtendedValidationAndFlowControlOptions.NONE)
                .withLifecycleEvents(new LifecycleEvents() {
                    @Override
                    public void onAttemptingConnect(Mqtt5Client client, OnAttemptingConnectReturn onAttemptingConnectReturn) {}

                    @Override
                    public void onConnectionSuccess(Mqtt5Client client, OnConnectionSuccessReturn onConnectionSuccessReturn) {}

                    @Override
                    public void onConnectionFailure(Mqtt5Client client, OnConnectionFailureReturn onConnectionFailureReturn) {}

                    @Override
                    public void onDisconnection(Mqtt5Client client, OnDisconnectionReturn onDisconnectionReturn) {}

                    @Override
                    public void onStopped(Mqtt5Client client, OnStoppedReturn onStoppedReturn) {}
                })
                .withMaxReconnectDelayMs(1000L)
                .withMinConnectedTimeToResetReconnectDelayMs(1000L)
                .withMinReconnectDelayMs(1000L)
                .withOfflineQueueBehavior(ClientOfflineQueueBehavior.FAIL_ALL_ON_DISCONNECT)
                .withAckTimeoutSeconds(1000L)
                .withPingTimeoutMs(1000L)
                .withPublishEvents(new PublishEvents() {
                    @Override
                    public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn) {}
                })
                .withRetryJitterMode(JitterMode.Default)
                .withSessionBehavior(ClientSessionBehavior.CLEAN)
                .withSocketOptions(socketOptions);
                // Skip websocket and TLS options - those are all different tests

                HttpProxyOptions proxyOptions = new HttpProxyOptions();
                proxyOptions.setHost(AWS_TEST_MQTT5_PROXY_HOST);
                proxyOptions.setPort((Integer.parseInt(AWS_TEST_MQTT5_PROXY_PORT)));
                proxyOptions.setConnectionType(HttpProxyConnectionType.Tunneling);
                builder.withHttpProxyOptions(proxyOptions);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    assertNotNull(client);
                }
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        CrtResource.waitForNoResources();
    }

    /* Minimal memory check */
    @Test
    public void New_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_DIRECT_MQTT_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_PORT);
        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                AWS_TEST_MQTT5_DIRECT_MQTT_HOST,
                Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_PORT));
            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                assertNotNull(client);
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        CrtResource.waitForNoResources();
    }

    /* Maximum memory test */
    @Test
    public void New_UC4() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_DIRECT_MQTT_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_PORT,
            AWS_TEST_MQTT5_BASIC_AUTH_USERNAME, AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD,
            AWS_TEST_MQTT5_PROXY_HOST, AWS_TEST_MQTT5_PROXY_PORT);
        try {
            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
                SocketOptions socketOptions = new SocketOptions();
            ) {
                PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder("test/topic", QOS.AT_LEAST_ONCE, "Hello World".getBytes());

                ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
                connectBuilder.withClientId("MQTT5 CRT");
                connectBuilder.withKeepAliveIntervalSeconds(1000L);
                connectBuilder.withMaximumPacketSizeBytes(1000L);
                connectBuilder.withPassword(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD.getBytes());
                connectBuilder.withReceiveMaximum(1000L);
                connectBuilder.withRequestProblemInformation(true);
                connectBuilder.withRequestResponseInformation(true);
                connectBuilder.withSessionExpiryIntervalSeconds(1000L);
                connectBuilder.withUsername(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME);
                connectBuilder.withWill(willPacketBuilder.build());
                connectBuilder.withWillDelayIntervalSeconds(1000L);

                ArrayList<UserProperty> userProperties = new ArrayList<UserProperty>();
                userProperties.add(new UserProperty("Hello", "World"));
                connectBuilder.withUserProperties(userProperties);

                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_HOST,
                    Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_PORT));
                builder.withBootstrap(bootstrap)
                .withConnackTimeoutMs(1000L)
                .withConnectOptions(connectBuilder.build())
                .withExtendedValidationAndFlowControlOptions(ExtendedValidationAndFlowControlOptions.NONE)
                .withLifecycleEvents(new LifecycleEvents() {
                    @Override
                    public void onAttemptingConnect(Mqtt5Client client, OnAttemptingConnectReturn onAttemptingConnectReturn) {}

                    @Override
                    public void onConnectionSuccess(Mqtt5Client client, OnConnectionSuccessReturn onConnectionSuccessReturn) {}

                    @Override
                    public void onConnectionFailure(Mqtt5Client client, OnConnectionFailureReturn onConnectionFailureReturn) {}

                    @Override
                    public void onDisconnection(Mqtt5Client client, OnDisconnectionReturn onDisconnectionReturn) {}

                    @Override
                    public void onStopped(Mqtt5Client client, OnStoppedReturn onStoppedReturn) {}
                })
                .withMaxReconnectDelayMs(1000L)
                .withMinConnectedTimeToResetReconnectDelayMs(1000L)
                .withMinReconnectDelayMs(1000L)
                .withOfflineQueueBehavior(ClientOfflineQueueBehavior.FAIL_ALL_ON_DISCONNECT)
                .withAckTimeoutSeconds(1000L)
                .withPingTimeoutMs(1000L)
                .withPublishEvents(new PublishEvents() {
                    @Override
                    public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn) {}
                })
                .withRetryJitterMode(JitterMode.Default)
                .withSessionBehavior(ClientSessionBehavior.CLEAN)
                .withSocketOptions(socketOptions);
                // Skip websocket and TLS options - those are all different tests

                HttpProxyOptions proxyOptions = new HttpProxyOptions();
                proxyOptions.setHost(AWS_TEST_MQTT5_PROXY_HOST);
                proxyOptions.setPort((Integer.parseInt(AWS_TEST_MQTT5_PROXY_PORT)));
                proxyOptions.setConnectionType(HttpProxyConnectionType.Tunneling);
                builder.withHttpProxyOptions(proxyOptions);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    assertNotNull(client);
                }
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * DIRECT CONNECT TEST CASES
     * ============================================================
     */

    private void doConnDC_UC1Test() {
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_HOST,
                    Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_PORT));
            builder.withLifecycleEvents(events);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                client.stop();
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Happy path. Direct connection with minimal configuration */
    @Test
    public void ConnDC_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_DIRECT_MQTT_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_PORT);

        TestUtils.doRetryableTest(() -> { this.doConnDC_UC1Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnDC_UC2Test() {
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST,
                    Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT));
            builder.withLifecycleEvents(events);

            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();
            connectOptions.withUsername(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME).withPassword(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD.getBytes());
            builder.withConnectOptions(connectOptions.build());

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Direct connection with basic authentication */
    @Test
    public void ConnDC_UC2() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT,
            AWS_TEST_MQTT5_BASIC_AUTH_USERNAME, AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD);

        TestUtils.doRetryableTest(() -> { this.doConnDC_UC2Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnDC_UC3Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient()) {
            tlsOptions.withVerifyPeer(false);
            try (TlsContext tlsContext = new TlsContext(tlsOptions)) {
                LifecycleEvents_Futured events = new LifecycleEvents_Futured();
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                        AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST, Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT));
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    client.stop();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Direct connection with TLS */
    @Test
    public void ConnDC_UC3() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT);

        TestUtils.doRetryableTest(() -> { this.doConnDC_UC3Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnDC_UC4Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions)) {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Direct connection with mTLS */
    @Test
    public void ConnDC_UC4() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doConnDC_UC4Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnDC_UC5Test() {
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
             TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient()) {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST, Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT));
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);

            HttpProxyOptions proxyOptions = new HttpProxyOptions();
            proxyOptions.setHost(AWS_TEST_MQTT5_PROXY_HOST);
            proxyOptions.setPort(Integer.parseInt(AWS_TEST_MQTT5_PROXY_PORT));
            proxyOptions.setConnectionType(HttpProxyConnectionType.Tunneling);

            tlsOptions.withVerifyPeer(false);
            try (TlsContext tlsContext = new TlsContext(tlsOptions)) {
                builder.withTlsContext(tlsContext);
                builder.withHttpProxyOptions(proxyOptions);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    client.stop();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Direct connection with HttpProxyOptions */
    @Test
    public void ConnDC_UC5() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT,
            AWS_TEST_MQTT5_PROXY_HOST, AWS_TEST_MQTT5_PROXY_PORT);

        TestUtils.doRetryableTest(() -> { this.doConnDC_UC5Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnDC_UC6Test() {
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            SocketOptions socketOptions = new SocketOptions()) {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder("test/topic", QOS.AT_LEAST_ONCE, "Hello World".getBytes());

            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId("MQTT5 CRT" + UUID.randomUUID().toString());
            connectBuilder.withKeepAliveIntervalSeconds(1000L);
            connectBuilder.withMaximumPacketSizeBytes(1000L);
            connectBuilder.withPassword(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD.getBytes());
            connectBuilder.withReceiveMaximum(1000L);
            connectBuilder.withRequestProblemInformation(true);
            connectBuilder.withRequestResponseInformation(true);
            connectBuilder.withSessionExpiryIntervalSeconds(1000L);
            connectBuilder.withUsername(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME);
            connectBuilder.withWill(willPacketBuilder.build());
            connectBuilder.withWillDelayIntervalSeconds(1000L);

            ArrayList<UserProperty> userProperties = new ArrayList<UserProperty>();
            userProperties.add(new UserProperty("Hello", "World"));
            connectBuilder.withUserProperties(userProperties);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST,
                    Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT));
            builder.withBootstrap(bootstrap)
                    .withConnackTimeoutMs(1000L)
                    .withConnectOptions(connectBuilder.build())
                    .withExtendedValidationAndFlowControlOptions(ExtendedValidationAndFlowControlOptions.NONE)
                    .withLifecycleEvents(events)
                    .withMaxReconnectDelayMs(1000L)
                    .withMinConnectedTimeToResetReconnectDelayMs(1000L)
                    .withMinReconnectDelayMs(1000L)
                    .withOfflineQueueBehavior(ClientOfflineQueueBehavior.FAIL_ALL_ON_DISCONNECT)
                    .withAckTimeoutSeconds(1000L)
                    .withPingTimeoutMs(1000L)
                    .withPublishEvents(new PublishEvents() {
                        @Override
                        public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn) {}
                    })
                    .withRetryJitterMode(JitterMode.Default)
                    .withSessionBehavior(ClientSessionBehavior.CLEAN)
                    .withSocketOptions(socketOptions);
            // Skip websocket, proxy options, and TLS options - those are all different tests

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                client.stop(disconnect.build());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Maximum options set connection test */
    @Test
    public void ConnDC_UC6() throws Exception{
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT,
            AWS_TEST_MQTT5_BASIC_AUTH_USERNAME, AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD);

        TestUtils.doRetryableTest(() -> { this.doConnDC_UC6Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * WEBSOCKET CONNECT TEST CASES
     * ============================================================
     */

    private void doConnWS_UC1Test() {
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr)) {

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_WS_MQTT_HOST, Long.parseLong(AWS_TEST_MQTT5_WS_MQTT_PORT));
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);

            Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                @Override
                public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                    t.complete(t.getHttpRequest());
                }
            };
            builder.withWebsocketHandshakeTransform(websocketTransform);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Happy path. Websocket connection with minimal configuration */
    @Test
    public void ConnWS_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_WS_MQTT_HOST, AWS_TEST_MQTT5_WS_MQTT_PORT);

        TestUtils.doRetryableTest(() -> { this.doConnWS_UC1Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnWS_UC2Test() {
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr)) {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST, Long.parseLong(AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT));
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);

            Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                @Override
                public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                    t.complete(t.getHttpRequest());
                }
            };
            builder.withWebsocketHandshakeTransform(websocketTransform);

            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();
            connectOptions.withUsername(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME).withPassword(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD.getBytes());
            builder.withConnectOptions(connectOptions.build());

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Websocket connection with basic authentication */
    @Test
    public void ConnWS_UC2() throws Exception{
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST, AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT,
            AWS_TEST_MQTT5_BASIC_AUTH_USERNAME, AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD);

        TestUtils.doRetryableTest(() -> { this.doConnWS_UC2Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnWS_UC3Test() {
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient()) {

            tlsOptions.withVerifyPeer(false);
            try (TlsContext tlsContext = new TlsContext(tlsOptions)) {
                LifecycleEvents_Futured events = new LifecycleEvents_Futured();
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                        AWS_TEST_MQTT5_WS_MQTT_TLS_HOST, Long.parseLong(AWS_TEST_MQTT5_WS_MQTT_TLS_PORT));
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);

                Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                    @Override
                    public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                        t.complete(t.getHttpRequest());
                    }
                };
                builder.withWebsocketHandshakeTransform(websocketTransform);
                builder.withTlsContext(tlsContext);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    client.stop();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Websocket connection with TLS */
    @Test
    public void ConnWS_UC3() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_WS_MQTT_TLS_HOST, AWS_TEST_MQTT5_WS_MQTT_TLS_PORT);

        TestUtils.doRetryableTest(() -> { this.doConnWS_UC3Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnWS_UC5Test() {
        try (EventLoopGroup elg = new EventLoopGroup(1);
             HostResolver hr = new HostResolver(elg);
             ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
             TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient()) {

            tlsOptions.withVerifyPeer(false);
            try (TlsContext tlsContext = new TlsContext(tlsOptions)) {
                LifecycleEvents_Futured events = new LifecycleEvents_Futured();
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                        AWS_TEST_MQTT5_WS_MQTT_TLS_HOST, Long.parseLong(AWS_TEST_MQTT5_WS_MQTT_TLS_PORT));
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);
                builder.withTlsContext(tlsContext);

                Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                    @Override
                    public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                        t.complete(t.getHttpRequest());
                    }
                };
                builder.withWebsocketHandshakeTransform(websocketTransform);

                HttpProxyOptions proxyOptions = new HttpProxyOptions();
                proxyOptions.setHost(AWS_TEST_MQTT5_PROXY_HOST);
                proxyOptions.setPort(Integer.parseInt(AWS_TEST_MQTT5_PROXY_PORT));
                proxyOptions.setConnectionType(HttpProxyConnectionType.Tunneling);
                builder.withHttpProxyOptions(proxyOptions);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    client.stop();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Websocket connection with HttpProxyOptions */
    @Test
    public void ConnWS_UC5() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_WS_MQTT_TLS_HOST, AWS_TEST_MQTT5_WS_MQTT_TLS_PORT,
            AWS_TEST_MQTT5_PROXY_HOST, AWS_TEST_MQTT5_PROXY_PORT);

        TestUtils.doRetryableTest(() -> { this.doConnWS_UC5Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnWS_UC6Test() {
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            SocketOptions socketOptions = new SocketOptions()) {
            PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder("test/topic", QOS.AT_LEAST_ONCE, "Hello World".getBytes());

            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId("MQTT5 CRT"+UUID.randomUUID().toString());
            connectBuilder.withKeepAliveIntervalSeconds(1000L);
            connectBuilder.withMaximumPacketSizeBytes(1000L);
            connectBuilder.withPassword(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD.getBytes());
            connectBuilder.withReceiveMaximum(1000L);
            connectBuilder.withRequestProblemInformation(true);
            connectBuilder.withRequestResponseInformation(true);
            connectBuilder.withSessionExpiryIntervalSeconds(1000L);
            connectBuilder.withUsername(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME);
            connectBuilder.withWill(willPacketBuilder.build());
            connectBuilder.withWillDelayIntervalSeconds(1000L);

            ArrayList<UserProperty> userProperties = new ArrayList<UserProperty>();
            userProperties.add(new UserProperty("Hello", "World"));
            connectBuilder.withUserProperties(userProperties);

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST, Long.parseLong(AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT));
            builder.withBootstrap(bootstrap)
                    .withConnackTimeoutMs(1000L)
                    .withConnectOptions(connectBuilder.build())
                    .withExtendedValidationAndFlowControlOptions(ExtendedValidationAndFlowControlOptions.NONE)
                    .withLifecycleEvents(events)
                    .withMaxReconnectDelayMs(1000L)
                    .withMinConnectedTimeToResetReconnectDelayMs(1000L)
                    .withMinReconnectDelayMs(1000L)
                    .withOfflineQueueBehavior(ClientOfflineQueueBehavior.FAIL_ALL_ON_DISCONNECT)
                    .withAckTimeoutSeconds(1000L)
                    .withPingTimeoutMs(1000L)
                    .withPublishEvents(new PublishEvents() {
                        @Override
                        public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn) {}
                    })
                    .withRetryJitterMode(JitterMode.Default)
                    .withSessionBehavior(ClientSessionBehavior.CLEAN)
                    .withSocketOptions(socketOptions);

            Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                @Override
                public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                    t.complete(t.getHttpRequest());
                }
            };
            builder.withWebsocketHandshakeTransform(websocketTransform);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                client.stop(disconnect.build());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Websocket connection with all options set */
    @Test
    public void ConnWS_UC6() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST, AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT,
            AWS_TEST_MQTT5_BASIC_AUTH_USERNAME, AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD);

        TestUtils.doRetryableTest(() -> { this.doConnWS_UC6Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * Negative Connect Tests with Incorrect Data
     * ============================================================
     */

    /* Client connect with invalid host name */
    @Test
    public void ConnNegativeID_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_DIRECT_MQTT_PORT);
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                "_test", Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_PORT));
            builder.withLifecycleEvents(events);
            builder.withMinReconnectDelayMs(1000L);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();

                try {
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    exceptionOccurred = true;
                    if (events.connectFailureCode == 1059) {
                        foundExpectedError = true;
                    } else {
                        System.out.println("EXCEPTION: " + ex);
                        System.out.println(ex.getMessage() + " \n");
                    }
                }

                if (foundExpectedError == false) {
                    System.out.println("Error code was not AWS_IO_DNS_INVALID_NAME like expected! There was an exception though");
                }
                if (exceptionOccurred == false) {
                    fail("No exception occurred!");
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Client connect with invalid, nonexistent port for direct connection */
    @Test
    public void ConnNegativeID_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_DIRECT_MQTT_HOST);
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_DIRECT_MQTT_HOST, 65535L);
            builder.withLifecycleEvents(events);
            builder.withMinReconnectDelayMs(1000L);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();

                try {
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    exceptionOccurred = true;
                    if (events.connectFailureCode == 1047) {
                        foundExpectedError = true;
                    }
                }

                if (foundExpectedError == false) {
                    System.out.println("Error code was not AWS_IO_SOCKET_CONNECTION_REFUSED like expected! There was an exception though");
                }
                if (exceptionOccurred == false) {
                    fail("No exception occurred!");
                }
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Client connect with invalid protocol port for direct connection */
    @Test
    public void ConnNegativeID_UC2_ALT() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_DIRECT_MQTT_HOST, AWS_TEST_MQTT5_WS_MQTT_PORT);
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                AWS_TEST_MQTT5_DIRECT_MQTT_HOST, Long.parseLong(AWS_TEST_MQTT5_WS_MQTT_PORT));
            builder.withLifecycleEvents(events);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();

                try {
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    exceptionOccurred = true;
                    if (events.connectFailureCode == 5149) {
                        foundExpectedError = true;
                    }
                }

                if (foundExpectedError == false) {
                    System.out.println("Error code was not AWS_ERROR_MQTT5_DECODE_PROTOCOL_ERROR like expected! There was an exception though");
                }
                if (exceptionOccurred == false) {
                    fail("No exception occurred!");
                }
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Client connect with invalid, nonexistent port for websocket connection */
    @Test
    public void ConnNegativeID_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_WS_MQTT_HOST);
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            ) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_WS_MQTT_HOST, 444L);
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);

                Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                    @Override
                    public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                        t.complete(t.getHttpRequest());
                    }
                };
                builder.withWebsocketHandshakeTransform(websocketTransform);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();

                    try {
                        events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    } catch (Exception ex) {
                        exceptionOccurred = true;
                        if (events.connectFailureCode == 1047) {
                            foundExpectedError = true;
                        }
                    }

                    if (foundExpectedError == false) {
                        System.out.println("Error code was not AWS_ERROR_MQTT5_DECODE_PROTOCOL_ERROR like expected! There was an exception though");
                    }
                    if (exceptionOccurred == false) {
                        fail("No exception occurred!");
                    }
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Client connect with invalid protocol port for websocket connection */
    @Test
    public void ConnNegativeID_UC3_ALT() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_WS_MQTT_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_PORT);
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            ) {

                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_WS_MQTT_HOST, Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_PORT));
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);

                Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                    @Override
                    public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                        t.complete(t.getHttpRequest());
                    }
                };
                builder.withWebsocketHandshakeTransform(websocketTransform);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    try {
                        events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    } catch (Exception ex) {
                        exceptionOccurred = true;
                        if (events.connectFailureCode == 46) {
                            foundExpectedError = true;
                        }
                    }

                    if (foundExpectedError == false) {
                        System.out.println("Error code was not AWS_ERROR_SYS_CALL_FAILURE (occurs right after AWS_ERROR_MQTT5_DECODE_PROTOCOL_ERROR for Websockets) like expected! There was an exception though");
                    }
                    if (exceptionOccurred == false) {
                        fail("No exception occurred!");
                    }
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Client connect with socket timeout */
    @Test
    public void ConnNegativeID_UC4() {
        skipIfNetworkUnavailable();
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
                SocketOptions options = new SocketOptions();
            ) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder("www.example.com", 81L);
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);

                options.connectTimeoutMs = 100;
                builder.withSocketOptions(options);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();

                    try {
                        events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    } catch (Exception ex) {
                        exceptionOccurred = true;
                        if (events.connectFailureCode == 1048) {
                            foundExpectedError = true;
                        }
                    }
                    if (foundExpectedError == false) {
                        System.out.println("Error code was not AWS_IO_SOCKET_TIMEOUT like expected! There was an exception though");
                    }
                    if (exceptionOccurred == false) {
                        fail("No exception occurred!");
                    }
                }
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Websocket handshake failure test */
    @Test
    public void ConnNegativeID_UC6() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_WS_MQTT_HOST, AWS_TEST_MQTT5_WS_MQTT_PORT);
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            ) {

                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_WS_MQTT_HOST, Long.parseLong(AWS_TEST_MQTT5_WS_MQTT_PORT));
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);

                Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                    @Override
                    public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                        t.completeExceptionally(new Throwable("Intentional failure!"));
                    }
                };
                builder.withWebsocketHandshakeTransform(websocketTransform);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();

                    try {
                        events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    } catch (Exception ex) {
                        exceptionOccurred = true;
                        if (events.connectFailureCode == 3) {
                            foundExpectedError = true;
                        }
                    }
                    if (foundExpectedError == false) {
                        System.out.println("Error code was not AWS_ERROR_UNKNOWN like expected! There was an exception though");
                    }
                    if (exceptionOccurred == false) {
                        fail("No exception occurred!");
                    }

                    client.stop();
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* For the double client ID test */
    static final class LifecycleEvents_DoubleClientID implements Mqtt5ClientOptions.LifecycleEvents {
        CompletableFuture<Void> connectedFuture = new CompletableFuture<>();
        CompletableFuture<Void> disconnectedFuture = new CompletableFuture<>();
        CompletableFuture<Void> stoppedFuture = new CompletableFuture<>();
        String client_name = "";

        @Override
        public void onAttemptingConnect(Mqtt5Client client, OnAttemptingConnectReturn onAttemptingConnectReturn) {}

        @Override
        public void onConnectionSuccess(Mqtt5Client client, OnConnectionSuccessReturn onConnectionSuccessReturn) {
            connectedFuture.complete(null);
        }

        @Override
        public void onConnectionFailure(Mqtt5Client client, OnConnectionFailureReturn onConnectionFailureReturn) {
            // failing the connected future here is not valid from a race condition standpoint.  It is possible that
            // the interrupting client itself gets interrupted and fails to fully connect due to the original client
            // interrupting it.  Eventually it will succeed (briefly) as the two clients fight over the client id
            // with increasing reconnect backoff.
        }

        @Override
        public void onDisconnection(Mqtt5Client client, OnDisconnectionReturn onDisconnectionReturn) {
            disconnectedFuture.complete(null);
        }

        @Override
        public void onStopped(Mqtt5Client client, OnStoppedReturn onStoppedReturn) {
            stoppedFuture.complete(null);
        }
    }

    private void doConnNegativeID_UC7Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {
            String testUUID = UUID.randomUUID().toString();
            LifecycleEvents_DoubleClientID eventsOne = new LifecycleEvents_DoubleClientID();
            LifecycleEvents_DoubleClientID eventsTwo = new LifecycleEvents_DoubleClientID();
            eventsOne.client_name = "client_one";
            eventsTwo.client_name = "client_two";

            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder().withClientId("test/MQTT5_Java_Double_ClientIDFail_" + testUUID);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(eventsOne);
            builder.withConnectOptions(connectOptions.build());
            builder.withConnackTimeoutMs(30000l); // 30 seconds

            Mqtt5ClientOptionsBuilder builderTwo = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builderTwo.withLifecycleEvents(eventsTwo);
            builderTwo.withConnectOptions(connectOptions.build());
            builderTwo.withConnackTimeoutMs(30000l); // 30 seconds

            builder.withTlsContext(tlsContext);
            builderTwo.withTlsContext(tlsContext);
            try (Mqtt5Client clientOne = new Mqtt5Client(builder.build());
                 Mqtt5Client clientTwo = new Mqtt5Client(builderTwo.build());) {
                clientOne.start();
                eventsOne.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                Thread.sleep(2000); // Sleep for 2 seconds to not hit IoT Core limits

                clientTwo.start();
                eventsTwo.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Make sure a disconnection for client 1 happened
                eventsOne.disconnectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Stop the clients from disconnecting each other. If we do not do this, then the clients will
                // attempt to reconnect endlessly, making a never ending loop.
                clientOne.stop();
                clientTwo.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Double Client ID failure test */
    @Test
    public void ConnNegativeID_UC7() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doConnNegativeID_UC7Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * Negative Data Input Tests
     * ============================================================
     */

    /* Negative Connect Packet Properties */
    @Test
    public void NewNegative_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
        boolean clientCreationFailed = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();

            connectOptions.withKeepAliveIntervalSeconds(-100L);
            builder.withConnectOptions(connectOptions.build());

            try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
                TlsContext tlsContext = new TlsContext(tlsOptions);) {
                builder.withTlsContext(tlsContext);

                clientCreationFailed = false;
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    ;
                } catch (Exception ex) {
                    clientCreationFailed = true;
                }
                if (clientCreationFailed == false) {
                    fail("Client creation did not fail with negative KeepAliveIntervalSeconds");
                }
                connectOptions.withKeepAliveIntervalSeconds(100L);

                connectOptions.withSessionExpiryIntervalSeconds(-100L);
                builder.withConnectOptions(connectOptions.build());
                clientCreationFailed = false;
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    ;
                } catch (Exception ex) {
                    clientCreationFailed = true;
                }
                if (clientCreationFailed == false) {
                    fail("Client creation did not fail with negative SessionExpiryIntervalSeconds");
                }
                connectOptions.withSessionExpiryIntervalSeconds(100L);

                connectOptions.withReceiveMaximum(-100L);
                builder.withConnectOptions(connectOptions.build());
                clientCreationFailed = false;
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    ;
                } catch (Exception ex) {
                    clientCreationFailed = true;
                }
                if (clientCreationFailed == false) {
                    fail("Client creation did not fail negative ReceiveMaximum");
                }
                connectOptions.withReceiveMaximum(100L);

                connectOptions.withMaximumPacketSizeBytes(-100L);
                builder.withConnectOptions(connectOptions.build());
                clientCreationFailed = false;
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    ;
                } catch (Exception ex) {
                    clientCreationFailed = true;
                }
                if (clientCreationFailed == false) {
                    fail("Client creation did not fail with negative MaximumPacketSizeBytes");
                }
                connectOptions.withMaximumPacketSizeBytes(100L);

                connectOptions.withWillDelayIntervalSeconds(-100L);
                builder.withConnectOptions(connectOptions.build());
                clientCreationFailed = false;
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    ;
                } catch (Exception ex) {
                    clientCreationFailed = true;
                }
                if (clientCreationFailed == false) {
                    fail("Client creation did not fail with negative willDelayIntervalSeconds");
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Overflow Connect Packet Properties */
    @Test
    public void NewNegative_UC1_ALT() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
        boolean clientCreationFailed = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();

            connectOptions.withKeepAliveIntervalSeconds(2147483647L);
            builder.withConnectOptions(connectOptions.build());

            try(TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
                TlsContext tlsContext = new TlsContext(tlsOptions)) {

                builder.withTlsContext(tlsContext);

                clientCreationFailed = false;
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    ;
                } catch (Exception ex) {
                    clientCreationFailed = true;
                }
                if (clientCreationFailed == false) {
                    fail("Client creation did not fail with overflow KeepAliveIntervalSeconds");
                }
                connectOptions.withKeepAliveIntervalSeconds(100L);

                connectOptions.withSessionExpiryIntervalSeconds(9223372036854775807L);
                builder.withConnectOptions(connectOptions.build());
                clientCreationFailed = false;
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    ;
                } catch (Exception ex) {
                    clientCreationFailed = true;
                }
                if (clientCreationFailed == false) {
                    fail("Client creation did not fail with overflow SessionExpiryIntervalSeconds");
                }
                connectOptions.withSessionExpiryIntervalSeconds(100L);

                connectOptions.withReceiveMaximum(2147483647L);
                builder.withConnectOptions(connectOptions.build());
                clientCreationFailed = false;
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    ;
                } catch (Exception ex) {
                    clientCreationFailed = true;
                }
                if (clientCreationFailed == false) {
                    fail("Client creation did not fail overflow ReceiveMaximum");
                }
                connectOptions.withReceiveMaximum(100L);

                connectOptions.withMaximumPacketSizeBytes(9223372036854775807L);
                builder.withConnectOptions(connectOptions.build());
                clientCreationFailed = false;
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    ;
                } catch (Exception ex) {
                    clientCreationFailed = true;
                }
                if (clientCreationFailed == false) {
                    fail("Client creation did not fail with overflow MaximumPacketSizeBytes");
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    private void doNewNegative_UC2Test(DisconnectPacketBuilder disconnectBuilder) {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);

            builder.withTlsContext(tlsContext);
            boolean clientDisconnectFailed = false;

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    client.stop(disconnectBuilder.build());
                } catch (Exception ex) {
                    clientDisconnectFailed = true;
                }

                if (clientDisconnectFailed == false) {
                    fail("Client disconnect packet creation did not fail!");
                }

                client.stop(new DisconnectPacketBuilder().build());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Negative Disconnect Packet Properties */
    @Test
    public void NewNegative_UC2() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        DisconnectPacketBuilder disconnectBuilder = new DisconnectPacketBuilder();
        disconnectBuilder.withSessionExpiryIntervalSeconds(-100L);

        TestUtils.doRetryableTest(() -> { this.doNewNegative_UC2Test(disconnectBuilder); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /* Overflow Disconnect Packet Properties */
    @Test
    public void NewNegative_UC2_ALT() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        DisconnectPacketBuilder disconnectBuilder = new DisconnectPacketBuilder();
        disconnectBuilder.withSessionExpiryIntervalSeconds(9223372036854775807L);

        TestUtils.doRetryableTest(() -> { this.doNewNegative_UC2Test(disconnectBuilder); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doNewNegative_UC3Test(PublishPacketBuilder publishBuilder) {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                boolean clientPublishFailed = false;
                try {
                    CompletableFuture<PublishResult> future = client.publish(publishBuilder.build());
                    future.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    clientPublishFailed = true;
                }

                if (clientPublishFailed == false) {
                    fail("Client publish did not fail!");
                }

                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Negative Publish Packet Properties */
    @Test
    public void NewNegative_UC3() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        PublishPacketBuilder publishBuilder = new PublishPacketBuilder();
        publishBuilder.withPayload("Hello World".getBytes()).withTopic("test/topic");
        publishBuilder.withMessageExpiryIntervalSeconds(-100L);

        TestUtils.doRetryableTest(() -> { this.doNewNegative_UC3Test(publishBuilder); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /* Overflow Publish Packet Properties */
    @Test
    public void NewNegative_UC3_ALT() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        PublishPacketBuilder publishBuilder = new PublishPacketBuilder("test/topic", QOS.AT_LEAST_ONCE, "Hello World".getBytes());
        publishBuilder.withMessageExpiryIntervalSeconds(9223372036854775807L);

        TestUtils.doRetryableTest(() -> { this.doNewNegative_UC3Test(publishBuilder); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doNewNegative_UC4Test(SubscribePacketBuilder subscribeBuilder) {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                boolean clientSubscribeFailed = false;
                try {
                    CompletableFuture<SubAckPacket> future = client.subscribe(subscribeBuilder.build());
                    future.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    clientSubscribeFailed = true;
                }

                if (clientSubscribeFailed == false) {
                    fail("Client subscribe did not fail!");
                }

                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Negative Subscribe Packet Properties */
    @Test
    public void NewNegative_UC4() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        SubscribePacketBuilder subscribeBuilder = new SubscribePacketBuilder("test/topic", QOS.AT_LEAST_ONCE);
        subscribeBuilder.withSubscriptionIdentifier(-100L);

        TestUtils.doRetryableTest(() -> { this.doNewNegative_UC4Test(subscribeBuilder); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /* Overflow Subscribe Packet Properties */
    @Test
    public void NewNegative_UC4_ALT() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        SubscribePacketBuilder subscribeBuilder = new SubscribePacketBuilder("test/topic", QOS.AT_LEAST_ONCE);
        subscribeBuilder.withSubscriptionIdentifier(9223372036854775807L);

        TestUtils.doRetryableTest(() -> { this.doNewNegative_UC4Test(subscribeBuilder); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * Negotiated Settings Tests
     * ============================================================
     */

    private void doNegotiated_UC1Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            ConnectPacketBuilder optionsBuilder = new ConnectPacketBuilder();
            optionsBuilder.withSessionExpiryIntervalSeconds(600000L);
            builder.withConnectOptions(optionsBuilder.build());

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // TODO: Add support for this in the future
                // assertEquals(
                //     "Negotiated Settings session expiry interval does not match sent session expiry interval",
                //     events.connectSuccessSettings.getSessionExpiryIntervalSeconds(),
                //     600000L);

                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Happy path, minimal success test */
    @Test
    public void Negotiated_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doNegotiated_UC1Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doNegotiated_UC2Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            String testUUID = UUID.randomUUID().toString();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            ConnectPacketBuilder optionsBuilder = new ConnectPacketBuilder();
            optionsBuilder.withClientId("test/MQTT5_Binding_Java_" + testUUID);
            optionsBuilder.withSessionExpiryIntervalSeconds(0L);
            optionsBuilder.withKeepAliveIntervalSeconds(360L);
            builder.withConnectOptions(optionsBuilder.build());

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                assertEquals(
                        "Negotiated Settings client ID does not match sent client ID",
                        events.connectSuccessSettings.getAssignedClientID(),
                        "test/MQTT5_Binding_Java_" + testUUID);
                assertEquals(
                        "Negotiated Settings session expiry interval does not match sent session expiry interval",
                        events.connectSuccessSettings.getSessionExpiryIntervalSeconds(),
                        0L);
                assertEquals(
                        "Negotiated Settings keep alive result does not match sent keep alive",
                        events.connectSuccessSettings.getServerKeepAliveSeconds(),
                        360);
                assertEquals(
                        "Negotiated Settings rejoined session does not match expected value",
                        false,
                        events.connectSuccessSettings.getRejoinedSession());

                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Maximum success test */
    @Test
    public void Negotiated_UC2() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doNegotiated_UC2Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doNegotiated_Rejoin_AlwaysTest() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            String testUUID = UUID.randomUUID().toString();
            ConnectPacketBuilder optionsBuilder = new ConnectPacketBuilder();
            optionsBuilder.withClientId("test/MQTT5_Binding_Java_" + testUUID);
            optionsBuilder.withSessionExpiryIntervalSeconds(3600L);
            optionsBuilder.withKeepAliveIntervalSeconds(360L);
            builder.withConnectOptions(optionsBuilder.build());

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                assertEquals(
                        "Negotiated Settings client ID does not match sent client ID",
                        events.connectSuccessSettings.getAssignedClientID(),
                        "test/MQTT5_Binding_Java_" + testUUID);
                assertEquals(
                        "Negotiated Settings session expiry interval does not match sent session expiry interval",
                        events.connectSuccessSettings.getSessionExpiryIntervalSeconds(),
                        3600L);
                assertEquals(
                        "Negotiated Settings keep alive result does not match sent keep alive",
                        events.connectSuccessSettings.getServerKeepAliveSeconds(),
                        360);
                assertEquals(
                        "Negotiated Settings rejoined session does not match expected value",
                        false,
                        events.connectSuccessSettings.getRejoinedSession());

                client.stop();
                events.stopFuture.get();
            }

            /* Avoid accidentally triggering re-connect throttle */
            Thread.sleep(2000);

            builder.withSessionBehavior(ClientSessionBehavior.REJOIN_ALWAYS);
            LifecycleEvents_Futured rejoinEvents = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(rejoinEvents);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                rejoinEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                assertEquals(
                        "Negotiated Settings rejoined session does not match expected value",
                        true,
                        rejoinEvents.connectSuccessSettings.getRejoinedSession());

                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Rejoin always session resumption test */
    @Test
    public void Negotiated_Rejoin_Always() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doNegotiated_Rejoin_AlwaysTest(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * Operation Tests
     * ============================================================
     */

    private void doOp_UC1Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            String testUUID = UUID.randomUUID().toString();
            String testTopic = "test/MQTT5_Binding_Java_" + testUUID;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            PublishEvents_Futured publishEvents = new PublishEvents_Futured();
            builder.withPublishEvents(publishEvents);

            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder(testTopic, QOS.AT_LEAST_ONCE, "Hello World".getBytes());
            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder(testTopic, QOS.AT_LEAST_ONCE);

            UnsubscribePacketBuilder unsubscribePacketBuilder = new UnsubscribePacketBuilder(testTopic);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                client.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                client.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                publishEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                publishEvents.publishReceivedFuture = new CompletableFuture<>();
                publishEvents.publishPacket = null;
                client.unsubscribe(unsubscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                client.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                assertEquals(
                        "Publish after unsubscribe still arrived!",
                        publishEvents.publishPacket,
                        null);

                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Sub-UnSub happy path */
    @Test
    public void Op_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doOp_UC1Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doOp_UC2Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions);
             TlsContextOptions tlsOptionsTwo = TlsContextOptions.createWithMtlsFromPath(
                     AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContextTwo = new TlsContext(tlsOptionsTwo)) {

            String testUUID = UUID.randomUUID().toString();
            String testTopic = "test/MQTT5_Binding_Java_" + testUUID;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();
            PublishPacketBuilder willPacket = new PublishPacketBuilder(testTopic, QOS.AT_LEAST_ONCE, "Hello World".getBytes());
            connectOptions.withWill(willPacket.build());
            connectOptions.withWillDelayIntervalSeconds(0L);
            builder.withConnectOptions(connectOptions.build());

            Mqtt5ClientOptionsBuilder builderTwo = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured eventsTwo = new LifecycleEvents_Futured();
            builderTwo.withLifecycleEvents(eventsTwo);
            PublishEvents_Futured publishEvents = new PublishEvents_Futured();
            builderTwo.withPublishEvents(publishEvents);
            builderTwo.withTlsContext(tlsContextTwo);

            SubscribePacketBuilder subscribeOptions = new SubscribePacketBuilder(testTopic, QOS.AT_LEAST_ONCE);

            try (Mqtt5Client clientOne = new Mqtt5Client(builder.build());
                 Mqtt5Client clientTwo = new Mqtt5Client(builderTwo.build())) {
                clientOne.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                clientTwo.start();
                eventsTwo.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                clientTwo.subscribe(subscribeOptions.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                clientOne.stop();

                // Did we get a publish message?
                publishEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                assertTrue(publishEvents.publishPacket != null);

                clientTwo.stop(new DisconnectPacketBuilder().build());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Sub-UnSub happy path */
    @Test
    public void Op_UC2() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doOp_UC2Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doOp_UC3Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            String testUUID = UUID.randomUUID().toString();
            String testTopic = "test/MQTT5_Binding_Java_" + testUUID;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            PublishEvents_Futured publishEvents = new PublishEvents_Futured();
            builder.withPublishEvents(publishEvents);

            // Make random binary
            byte[] randomBytes = new byte[256];
            Random random = new Random();
            random.nextBytes(randomBytes);

            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder(testTopic, QOS.AT_LEAST_ONCE, randomBytes);

            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder(testTopic, QOS.AT_LEAST_ONCE);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                client.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                client.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                publishEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                assertTrue(java.util.Arrays.equals(publishEvents.publishPacket.getPayload(), randomBytes));

                client.stop();
                events.stopFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Binary Publish Test */
    @Test
    public void Op_UC3() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doOp_UC3Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doOp_UC4Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            String testUUID = UUID.randomUUID().toString();
            String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

            // Publisher
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Subscriber
            Mqtt5ClientOptionsBuilder builderTwo = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured eventsTwo = new LifecycleEvents_Futured();
            builderTwo.withLifecycleEvents(eventsTwo);

            builder.withTlsContext(tlsContext);
            builderTwo.withTlsContext(tlsContext);

            PublishEvents_Futured publishEvents = new PublishEvents_Futured();
            builderTwo.withPublishEvents(publishEvents);
            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder(testTopic, QOS.AT_LEAST_ONCE);

            ConnectPacketBuilder connectPacketBuilder = new ConnectPacketBuilder();
            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder(testTopic, QOS.AT_LEAST_ONCE, "Hello World".getBytes());
            connectPacketBuilder.withWill(publishPacketBuilder.build());
            connectPacketBuilder.withKeepAliveIntervalSeconds(4l);
            builder.withConnectOptions(connectPacketBuilder.build());
            builder.withPingTimeoutMs(8l);

            DisconnectPacketBuilder disconnectPacketBuilder = new DisconnectPacketBuilder();
            disconnectPacketBuilder.withReasonCode(DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE);

            try (Mqtt5Client publisher = new Mqtt5Client(builder.build());
                 Mqtt5Client subscriber = new Mqtt5Client(builderTwo.build())) {

                publisher.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                subscriber.start();
                eventsTwo.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                subscriber.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Paranoid about service-side eventual consistency.  Add a wait to reduce chances of a missed will publish.
                Thread.sleep(2000);

                publisher.stop(disconnectPacketBuilder.build());

                publishEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                subscriber.stop(new DisconnectPacketBuilder().build());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Will test */
    @Test
    public void Op_UC4() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doOp_UC4Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doOp_SharedSubscriptionTest() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            int messageCount = 10;
            String testUUID = UUID.randomUUID().toString();
            String testTopic = "test/MQTT5_Binding_Java_" + testUUID;
            String sharedTopicfilter = "$share/crttest/test/MQTT5_Binding_Java_" + testUUID;

            // Publisher builder
            Mqtt5ClientOptionsBuilder publisherBuilder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured publisherLCEvents = new LifecycleEvents_Futured();
            publisherBuilder.withLifecycleEvents(publisherLCEvents);
            publisherBuilder.withTlsContext(tlsContext);

            PublishEvents_Futured_Counted publishEvents = new PublishEvents_Futured_Counted();
            publishEvents.desiredPublishCount = messageCount;

            // SubscriberOne builder
            Mqtt5ClientOptionsBuilder subscriberOneBuilder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured subscriberOneLCEvents = new LifecycleEvents_Futured();
            subscriberOneBuilder.withLifecycleEvents(subscriberOneLCEvents);
            subscriberOneBuilder.withTlsContext(tlsContext);
            subscriberOneBuilder.withPublishEvents(publishEvents);

            // SubscriberTwo builder
            Mqtt5ClientOptionsBuilder subscriberTwoBuilder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured subscriberTwoLCEvents = new LifecycleEvents_Futured();
            subscriberTwoBuilder.withLifecycleEvents(subscriberTwoLCEvents);
            subscriberTwoBuilder.withTlsContext(tlsContext);
            subscriberTwoBuilder.withPublishEvents(publishEvents);

            // PublishPacketBuilder
            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
            publishPacketBuilder.withTopic(testTopic);
            publishPacketBuilder.withQOS(QOS.AT_LEAST_ONCE);

            // SubscribePacketBuilder
            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder(sharedTopicfilter, QOS.AT_LEAST_ONCE);

            try (Mqtt5Client publisherClient = new Mqtt5Client(publisherBuilder.build());
                Mqtt5Client subscriberOneClient = new Mqtt5Client(subscriberOneBuilder.build());
                Mqtt5Client subscriberTwoClient = new Mqtt5Client(subscriberTwoBuilder.build())) {

                publisherClient.start();
                subscriberOneClient.start();
                subscriberTwoClient.start();

                publisherLCEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                subscriberOneLCEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                subscriberTwoLCEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                subscriberOneClient.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                subscriberTwoClient.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                for (int i = 0; i < messageCount; ++i) {
                    publishPacketBuilder.withPayload(String.valueOf(i).getBytes());
                    publisherClient.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                }

                publishEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Wait a little longer just to ensure that no packets beyond expectations are arrived.
                publishEvents.afterCompletionFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                subscriberOneClient.stop();
                subscriberTwoClient.stop();
                publisherClient.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Shared subscriptions test */
    @Test
    public void Op_SharedSubscription() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doOp_SharedSubscriptionTest(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * Error Operation Tests
     * ============================================================
     */

    private void doErrorOp_UC1Test(PublishPacket publish) {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            boolean didExceptionOccur = false;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    client.publish(publish).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    didExceptionOccur = true;
                }

                if (didExceptionOccur == false) {
                    fail("Null publish packet did not cause exception with error!");
                }
                client.stop(new DisconnectPacketBuilder().build());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Null Publish Test */
    @Test
    public void ErrorOp_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doErrorOp_UC1Test(null); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /* Publish with empty builder test */
    @Test
    public void ErrorOp_UC1_ALT() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        PublishPacket publish = new PublishPacketBuilder().build();
        TestUtils.doRetryableTest(() -> { this.doErrorOp_UC1Test(publish); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doErrorOp_UC2Test(SubscribePacket subscribe) {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {
            boolean didExceptionOccur = false;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    client.subscribe(subscribe).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    didExceptionOccur = true;
                }

                if (didExceptionOccur == false) {
                    fail("Null subscribe packet did not cause exception with error!");
                }
                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Null Subscribe Test */
    @Test
    public void ErrorOp_UC2() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doErrorOp_UC2Test(null); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /* Empty Subscribe Test */
    @Test
    public void ErrorOp_UC2_ALT() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        SubscribePacket subscribe = new SubscribePacketBuilder().build();
        TestUtils.doRetryableTest(() -> { this.doErrorOp_UC2Test(subscribe); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doErrorOp_UC3Test(UnsubscribePacket unsubscribe) {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            boolean didExceptionOccur = false;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    client.unsubscribe(unsubscribe).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    didExceptionOccur = true;
                }

                if (didExceptionOccur == false) {
                    fail("Null unsubscribe packet did not cause exception with error!");
                }
                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Null Unsubscribe Test */
    @Test
    public void ErrorOp_UC3() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doErrorOp_UC3Test(null); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /* Empty Unsubscribe Test */
    @Test
    public void ErrorOp_UC3_ALT() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        UnsubscribePacket unsubscribe = new UnsubscribePacketBuilder().build();
        TestUtils.doRetryableTest(() -> { this.doErrorOp_UC3Test(unsubscribe); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /* Unsupported Connect packet data sent (IoT Core only) */
    @Test
    public void ErrorOp_UC4() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
        boolean didExceptionOccur = false;
        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                    AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
                TlsContext tlsContext = new TlsContext(tlsOptions)) {

                builder.withTlsContext(tlsContext);

                ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();
                String clientIDString = "";
                for (int i = 0; i < 256; i++) {
                    clientIDString += "a";
                }
                connectOptions.withClientId(clientIDString);
                builder.withConnectOptions(connectOptions.build());

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    try {
                        client.start();
                        events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    } catch (Exception ex) {
                        didExceptionOccur = true;
                    }

                    if (didExceptionOccur == false) {
                        fail("Was able to connect with Client ID longer than 128 characters (AWS_IOT_CORE_MAXIMUM_CLIENT_ID_LENGTH)");
                    }
                    client.stop();
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * ============================================================
     * QoS1 Tests
     * ============================================================
     */

    private void doQoS1_UC1Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            int messageCount = 10;
            String testUUID = UUID.randomUUID().toString();
            String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            Mqtt5ClientOptionsBuilder builderTwo = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured eventsTwo = new LifecycleEvents_Futured();
            builderTwo.withLifecycleEvents(eventsTwo);
            PublishEvents_Futured_Counted publishEvents = new PublishEvents_Futured_Counted();
            publishEvents.desiredPublishCount = messageCount;
            builderTwo.withPublishEvents(publishEvents);

            builder.withTlsContext(tlsContext);
            builderTwo.withTlsContext(tlsContext);

            try (Mqtt5Client publisher = new Mqtt5Client(builder.build());
                Mqtt5Client subscriber = new Mqtt5Client(builderTwo.build())) {
                publisher.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                subscriber.start();
                eventsTwo.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder(testTopic, QOS.AT_LEAST_ONCE);
                subscriber.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder(testTopic, QOS.AT_LEAST_ONCE, "Hello World".getBytes());

                for (int i = 0; i < messageCount; i++) {
                    publisher.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                }

                // Did we get all the messages?
                publishEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                subscriber.stop();
                publisher.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Happy path. No drop in connection, no retry, no reconnect */
    @Test
    public void QoS1_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doQoS1_UC1Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * Retain Tests
     * ============================================================
     */
    private void doRetain_UC1Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            String testUUID = UUID.randomUUID().toString();
            String testTopic = "test/retained_topic/MQTT5_Binding_Java_" + testUUID;

            Mqtt5ClientOptionsBuilder publisherEventsBuilder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured publisherEvents = new LifecycleEvents_Futured();
            publisherEventsBuilder.withLifecycleEvents(publisherEvents);

            Mqtt5ClientOptionsBuilder successSubscriberBuilder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured successSubscriberEvents = new LifecycleEvents_Futured();
            PublishEvents_Futured successSubscriberPublishEvents = new PublishEvents_Futured();
            successSubscriberBuilder.withLifecycleEvents(successSubscriberEvents);
            successSubscriberBuilder.withPublishEvents(successSubscriberPublishEvents);

            Mqtt5ClientOptionsBuilder unsuccessSubscriberBuilder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured unsuccessfulSubscriberEvents = new LifecycleEvents_Futured();
            PublishEvents_Futured unsuccessfulSubscriberPublishEvents = new PublishEvents_Futured();
            unsuccessSubscriberBuilder.withLifecycleEvents(unsuccessfulSubscriberEvents);
            unsuccessSubscriberBuilder.withPublishEvents(unsuccessfulSubscriberPublishEvents);

            publisherEventsBuilder.withTlsContext(tlsContext);
            successSubscriberBuilder.withTlsContext(tlsContext);
            unsuccessSubscriberBuilder.withTlsContext(tlsContext);

            try (Mqtt5Client publisher = new Mqtt5Client(publisherEventsBuilder.build());
                Mqtt5Client successSubscriber = new Mqtt5Client(successSubscriberBuilder.build());
                Mqtt5Client unsuccessfulSubscriber = new Mqtt5Client(unsuccessSubscriberBuilder.build())) {
                // Connect and publish a retained message
                publisher.start();
                publisherEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder(testTopic, QOS.AT_LEAST_ONCE, "Hello World".getBytes());
                publishPacketBuilder.withRetain(true);
                publisher.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Setup for clearing the retained message
                publishPacketBuilder.withPayload(null);

                // Connect the successful subscriber
                successSubscriber.start();
                try {
                    successSubscriberEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    // Clear the retained message
                    publisher.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    fail("Success subscriber could not connect!");
                }

                // Subscribe and verify the retained message
                SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
                subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE, false, true, RetainHandlingType.SEND_ON_SUBSCRIBE);
                try {
                    successSubscriber.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    // Clear the retained message
                    publisher.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    fail("Success subscriber could not subscribe!");
                }
                try {
                    successSubscriberPublishEvents.publishReceivedFuture.get(360, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    // Clear the retained message
                    publisher.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    fail("Success subscriber did not get retained message!");
                }

                // Clear the retained message
                publisher.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Wait 15 seconds to give the server time to clear everything out
                Thread.sleep(15000);

                // Connect the unsuccessful subscriber
                unsuccessfulSubscriber.start();
                unsuccessfulSubscriberEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                unsuccessfulSubscriber.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                // Make sure we do NOT get a publish
                boolean didExceptionOccur = false;
                try {
                    unsuccessfulSubscriberPublishEvents.publishReceivedFuture.get(30, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    didExceptionOccur = true;
                }

                if (didExceptionOccur == false) {
                    fail("Unsuccessful subscriber got retained message even though it should be cleared!");
                }

                // Disconnect all clients
                publisher.stop();
                publisherEvents.stopFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                successSubscriber.stop();
                successSubscriberEvents.stopFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                unsuccessfulSubscriber.stop();
                unsuccessfulSubscriberEvents.stopFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Happy path. No drop in connection, no retry, no reconnect */
    @Test
    public void Retain_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doRetain_UC1Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * Operation Interrupt Tests
     * ============================================================
     */

    private void doInterrupt_Sub_UC1Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {
            String testUUID = UUID.randomUUID().toString();
            String testTopic = "test/MQTT5_Binding_Java_" + testUUID;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder(testTopic, QOS.AT_LEAST_ONCE);

                try {
                    CompletableFuture<SubAckPacket> subscribeResult = client.subscribe(subscribePacketBuilder.build());
                    client.stop();
                    SubAckPacket packet = subscribeResult.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    if (ex.getCause().getClass() == CrtRuntimeException.class) {
                        CrtRuntimeException exCrt = (CrtRuntimeException) ex.getCause();
                        if (exCrt.errorCode != 5153) {
                            System.out.println("Exception occurred when stopping subscribe" +
                                    "but it was not AWS_ERROR_MQTT5_USER_REQUESTED_STOP like expected");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // Subscribe interrupt test
    // Note: I don't think this "test" is useful.
    @Test
    public void Interrupt_Sub_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doInterrupt_Sub_UC1Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doInterrupt_Unsub_UC1Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            String testUUID = UUID.randomUUID().toString();
            String testTopic = "test/MQTT5_Binding_Java_" + testUUID;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                UnsubscribePacketBuilder unsubscribePacketBuilder = new UnsubscribePacketBuilder(testTopic);

                try {
                    CompletableFuture<UnsubAckPacket> unsubscribeResult = client.unsubscribe(unsubscribePacketBuilder.build());
                    client.stop();
                    UnsubAckPacket packet = unsubscribeResult.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    if (ex.getCause().getClass() == CrtRuntimeException.class) {
                        CrtRuntimeException exCrt = (CrtRuntimeException) ex.getCause();
                        if (exCrt.errorCode != 5153) {
                            System.out.println("Exception ocurred when stopping unsubscribe" +
                                    "but it was not AWS_ERROR_MQTT5_USER_REQUESTED_STOP like expected");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // Unsubscribe interrupt test
    // Note: I don't think this "test" is useful.
    @Test
    public void Interrupt_Unsub_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doInterrupt_Unsub_UC1Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doInterrupt_Publish_UC1Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            String testUUID = UUID.randomUUID().toString();
            String testTopic = "test/MQTT5_Binding_Java_" + testUUID;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder(testTopic, QOS.AT_LEAST_ONCE, "null".getBytes());

                try {
                    CompletableFuture<PublishResult> publishResult = client.publish(publishPacketBuilder.build());
                    client.stop();
                    PublishResult publishData = publishResult.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    if (ex.getCause().getClass() == CrtRuntimeException.class) {
                        CrtRuntimeException exCrt = (CrtRuntimeException) ex.getCause();
                        if (exCrt.errorCode != 5153) {
                            System.out.println("Exception ocurred when stopping publish" +
                                    "but it was not AWS_ERROR_MQTT5_USER_REQUESTED_STOP like expected");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // Publish interrupt test
    // Note: I don't think this "test" is useful.
    @Test
    public void Interrupt_Publish_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doInterrupt_Publish_UC1Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * Misc Tests
     * ============================================================
     */

    private void doOperationStatistics_UC1Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            int messageCount = 10;
            String testUUID = UUID.randomUUID().toString();
            String testTopic = "test/MQTT5_Binding_Java_" + testUUID;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client publisher = new Mqtt5Client(builder.build())) {
                publisher.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                Mqtt5ClientOperationStatistics statistics = publisher.getOperationStatistics();
                // Make sure it is empty
                if (statistics.getIncompleteOperationCount() != 0) {
                    fail("Incomplete operation count was not zero!");
                }
                if (statistics.getIncompleteOperationSize() != 0) {
                    fail("Incomplete operation size was not zero!");
                }
                if (statistics.getUnackedOperationCount() != 0) {
                    fail("Unacked operation count was not zero!");
                }
                if (statistics.getUnackedOperationSize() != 0) {
                    fail("Unacked operation size was not zero!");
                }

                PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder(testTopic, QOS.AT_LEAST_ONCE, "Hello World".getBytes());

                for (int i = 0; i < messageCount; i++) {
                    publisher.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                }

                // Make sure it is empty
                if (statistics.getIncompleteOperationCount() != 0) {
                    fail("Incomplete operation count was not zero!");
                }
                if (statistics.getIncompleteOperationSize() != 0) {
                    fail("Incomplete operation size was not zero!");
                }
                if (statistics.getUnackedOperationCount() != 0) {
                    fail("Unacked operation count was not zero!");
                }
                if (statistics.getUnackedOperationSize() != 0) {
                    fail("Unacked operation size was not zero!");
                }

                publisher.stop();
                events.stopFuture.get(60, TimeUnit.SECONDS);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* Happy path. Check statistics before, make some publishes, check it after */
    @Test
    public void OperationStatistics_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        TestUtils.doRetryableTest(() -> { this.doOperationStatistics_UC1Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * MQTT5 DIRECT IoT Core CONNECTION TEST CASES
     * ============================================================
     */

    private void doConnDC_Cred_UC1Test() {
        try {
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance(AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FORMAT);
            java.io.FileInputStream keyStoreStream = new java.io.FileInputStream(AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FILE);
            keyStore.load(keyStoreStream, AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_PASSWORD.toCharArray());
            keyStoreStream.close();

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsJavaKeystore(
                    keyStore,
                    AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_ALIAS,
                    AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_PASSWORD);
                TlsContext tlsContext = new TlsContext(tlsOptions)) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    client.stop();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* MQTT5 ConnDC_Cred_UC1 - MQTT5 connect with Java Keystore */
    @Test
    public void ConnDC_Cred_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FORMAT,
            AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FILE, AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_PASSWORD,
            AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_ALIAS, AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_PASSWORD);

        TestUtils.doRetryableTest(() -> { this.doConnDC_Cred_UC1Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnDC_Cred_UC2Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsPkcs12(
                AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY,
                AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY_PASSWORD);
            TlsContext tlsContext = new TlsContext(tlsOptions)) {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* MQTT5 ConnDC_Cred_UC2 - MQTT5 connect with PKCS12 Key */
    @Test
    public void ConnDC_Cred_UC2() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY,
            AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY_PASSWORD);

        TestUtils.doRetryableTest(() -> { this.doConnDC_Cred_UC2Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnDC_Cred_UC3Test() {
        try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsWindowsCertStorePath(
                AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE);
            TlsContext tlsContext = new TlsContext(tlsOptions)) {

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* MQTT5 ConnDC_Cred_UC3 - MQTT5 connect with Windows Cert Store */
    @Test
    public void ConnDC_Cred_UC3() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS,
            AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE);

        TestUtils.doRetryableTest(() -> { this.doConnDC_Cred_UC3Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    private void doConnDC_Cred_UC4Test() {
        // The published Softhsm package on muslc (Alpine) crashes if we don't call C_Finalize at the end.
        try (Pkcs11Lib pkcs11Lib = new Pkcs11Lib(AWS_TEST_MQTT5_IOT_CORE_PKCS11_LIB, Pkcs11Lib.InitializeFinalizeBehavior.STRICT);
            TlsContextPkcs11Options pkcs11Options = new TlsContextPkcs11Options(pkcs11Lib)) {

            pkcs11Options.withTokenLabel(AWS_TEST_MQTT5_IOT_CORE_PKCS11_TOKEN_LABEL);
            pkcs11Options.withUserPin(AWS_TEST_MQTT5_IOT_CORE_PKCS11_PIN);
            pkcs11Options.withPrivateKeyObjectLabel(AWS_TEST_MQTT5_IOT_CORE_PKCS11_PKEY_LABEL);
            pkcs11Options.withCertificateFilePath(AWS_TEST_MQTT5_IOT_CORE_PKCS11_CERT_FILE);

            try (TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsPkcs11(pkcs11Options);
                TlsContext tlsContext = new TlsContext(tlsOptions)) {

                LifecycleEvents_Futured events = new LifecycleEvents_Futured();
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    client.stop();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* MQTT5 ConnDC_Cred_UC4 - MQTT5 connect with PKCS11 */
    @Test
    public void ConnDC_Cred_UC4() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_PKCS11_LIB,
            AWS_TEST_MQTT5_IOT_CORE_PKCS11_TOKEN_LABEL, AWS_TEST_MQTT5_IOT_CORE_PKCS11_PIN,
            AWS_TEST_MQTT5_IOT_CORE_PKCS11_PKEY_LABEL, AWS_TEST_MQTT5_IOT_CORE_PKCS11_CERT_FILE);

        TestUtils.doRetryableTest(() -> { this.doConnDC_Cred_UC4Test(); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * MQTT5 WEBSOCKET IoT Core CONNECTION TEST CASES
     * ============================================================
     */

    private void doWebsocketIotCoreConnectionTest(Function<ClientBootstrap, CredentialsProvider> providerBuilder) {
        try (EventLoopGroup elg = new EventLoopGroup(1);
             HostResolver hr = new HostResolver(elg);
             ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
             TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
             TlsContext tlsContext = new TlsContext(tlsOptions);
             AwsSigningConfig signingConfig = new AwsSigningConfig();
             Mqtt5ClientTestSigv4HandshakeTransformer transformer = new Mqtt5ClientTestSigv4HandshakeTransformer(signingConfig);
             CredentialsProvider provider = providerBuilder.apply(bootstrap)) {

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 443L);
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);
            builder.withTlsContext(tlsContext);

            signingConfig.setCredentialsProvider(provider);
            signingConfig.setAlgorithm(AwsSigningAlgorithm.SIGV4);
            signingConfig.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_QUERY_PARAMS);
            signingConfig.setRegion(AWS_TEST_MQTT5_IOT_CORE_REGION);
            signingConfig.setService("iotdevicegateway");
            signingConfig.setOmitSessionToken(true);

            builder.withWebsocketHandshakeTransform(transformer);
            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                client.stop();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /* MQTT5 ConnWS_Cred_UC1 - static credentials connect */
    @Test
    public void ConnWS_Cred_UC1() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_REGION,
            AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY, AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY,
            AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN);

        TestUtils.doRetryableTest(() -> { this.doWebsocketIotCoreConnectionTest(
            (bootstrap) -> {
                StaticCredentialsProviderBuilder credentialsBuilder = new StaticCredentialsProviderBuilder();
                credentialsBuilder.withAccessKeyId(AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY.getBytes());
                credentialsBuilder.withSecretAccessKey(AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY.getBytes());
                credentialsBuilder.withSessionToken(AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN.getBytes());

                return credentialsBuilder.build();
            }
        ); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /* MQTT5 ConnWS_Cred_UC2 - default credentials connect */
    @Test
    public void ConnWS_Cred_UC2() throws Exception {
        skipIfAndroid(); // Credential Provider support not yet added for Android
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_REGION);

        TestUtils.doRetryableTest(() -> { this.doWebsocketIotCoreConnectionTest(
            (bootstrap) -> {
                DefaultChainCredentialsProviderBuilder credentialsBuilder = new DefaultChainCredentialsProviderBuilder();
                credentialsBuilder.withClientBootstrap(bootstrap);

                return credentialsBuilder.build();
            }
        ); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /**
     * MQTT5 ConnWS_Cred_UC3 - Cognito Identity credentials connect
     */
    @Test
    public void ConnWS_Cred_UC3() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_REGION,
            AWS_TEST_MQTT5_COGNITO_ENDPOINT, AWS_TEST_MQTT5_COGNITO_IDENTITY);

        TestUtils.doRetryableTest(() -> { this.doWebsocketIotCoreConnectionTest(
                (bootstrap) -> {
                    try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
                         TlsContext tlsContext = new TlsContext(tlsOptions)) {
                        CognitoCredentialsProviderBuilder credentialsBuilder = new CognitoCredentialsProviderBuilder();
                        credentialsBuilder.withClientBootstrap(bootstrap);
                        credentialsBuilder.withTlsContext(tlsContext);
                        credentialsBuilder.withEndpoint(AWS_TEST_MQTT5_COGNITO_ENDPOINT);
                        credentialsBuilder.withIdentity(AWS_TEST_MQTT5_COGNITO_IDENTITY);

                        return credentialsBuilder.build();
                    }
                }
        ); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }

    /* MQTT5 ConnWS_Cred_UC4 - X509 credentials connect */
    @Test
    public void ConnWS_Cred_UC4() throws Exception {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_REGION,
            AWS_TEST_MQTT5_IOT_CORE_X509_CERT, AWS_TEST_MQTT5_IOT_CORE_X509_KEY,
            AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT, AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS,
            AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME);

        TestUtils.doRetryableTest(() -> { this.doWebsocketIotCoreConnectionTest(
                (bootstrap) -> {
                    try (TlsContextOptions x509ContextOptions = TlsContextOptions.createWithMtlsFromPath(
                            AWS_TEST_MQTT5_IOT_CORE_X509_CERT, AWS_TEST_MQTT5_IOT_CORE_X509_KEY);
                        TlsContext x509Context = new TlsContext(x509ContextOptions)) {
                        X509CredentialsProviderBuilder credentialsBuilder = new X509CredentialsProviderBuilder();
                        credentialsBuilder.withClientBootstrap(bootstrap);
                        credentialsBuilder.withTlsContext(x509Context);
                        credentialsBuilder.withEndpoint(AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT);
                        credentialsBuilder.withRoleAlias(AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS);
                        credentialsBuilder.withThingName(AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME);

                        return credentialsBuilder.build();
                    }
                }
        ); }, (ex) -> { return TestUtils.isRetryableTimeout(ex); }, MAX_TEST_RETRIES, TEST_RETRY_SLEEP_MILLIS);

        CrtResource.waitForNoResources();
    }
}
