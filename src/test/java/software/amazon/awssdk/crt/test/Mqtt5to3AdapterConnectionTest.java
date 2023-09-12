/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

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
import software.amazon.awssdk.crt.mqtt.MqttClient;
import software.amazon.awssdk.crt.mqtt.MqttClientConnection;
import software.amazon.awssdk.crt.mqtt.MqttClientConnectionEvents;
import software.amazon.awssdk.crt.mqtt.OnConnectionSuccessReturn;
import software.amazon.awssdk.crt.mqtt.QualityOfService;
import software.amazon.awssdk.crt.mqtt.OnConnectionFailureReturn;
import software.amazon.awssdk.crt.mqtt.OnConnectionClosedReturn;
import software.amazon.awssdk.crt.mqtt.MqttMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.junit.Test;

/* For environment variable setup, see SetupCrossCICrtEnvironment in the CRT builder */
public class Mqtt5to3AdapterConnectionTest extends Mqtt5ClientTestFixture {

    private boolean disconnecting = false;
    MqttClientConnection connection = null;
    private CompletableFuture<software.amazon.awssdk.crt.mqtt.OnConnectionSuccessReturn> onConnectionSuccessFuture = new CompletableFuture<software.amazon.awssdk.crt.mqtt.OnConnectionSuccessReturn>();
    private CompletableFuture<software.amazon.awssdk.crt.mqtt.OnConnectionFailureReturn> onConnectionFailureFuture = new CompletableFuture<software.amazon.awssdk.crt.mqtt.OnConnectionFailureReturn>();
    private CompletableFuture<software.amazon.awssdk.crt.mqtt.OnConnectionClosedReturn> onConnectionClosedFuture = new CompletableFuture<software.amazon.awssdk.crt.mqtt.OnConnectionClosedReturn>();
    int pubsAcked = 0;
    int subsAcked = 0;
    // Connection callback events
    private MqttClientConnectionEvents events = new MqttClientConnectionEvents() {
        @Override
        public void onConnectionResumed(boolean sessionPresent) {
            System.out.println("Connection resumed");
        }

        @Override
        public void onConnectionInterrupted(int errorCode) {
            if (!disconnecting) {
                System.out.println(
                        "Connection interrupted: error: " + errorCode + " " + CRT.awsErrorString(errorCode));
            }
        }

        @Override
        public void onConnectionFailure(OnConnectionFailureReturn data) {
            System.out.println("Connection failed with error: " + data.getErrorCode() + " "
                    + CRT.awsErrorString(data.getErrorCode()));
            onConnectionFailureFuture.complete(data);
        }

        @Override
        public void onConnectionSuccess(OnConnectionSuccessReturn data) {
            System.out.println("Connection success. Session present: " + data.getSessionPresent());
            onConnectionSuccessFuture.complete(data);
        }

        @Override
        public void onConnectionClosed(OnConnectionClosedReturn data) {
            System.out.println("Connection disconnected successfully");
            onConnectionClosedFuture.complete(data);
        }
    };

    public Mqtt5to3AdapterConnectionTest() {
    }

    Consumer<MqttMessage> connectionMessageTransfomer = null;

    void setConnectionMessageTransformer(Consumer<MqttMessage> connectionMessageTransfomer) {
        this.connectionMessageTransfomer = connectionMessageTransfomer;
    }

    void Mqtt3ConnectionDisconnect() {
        disconnecting = true;
        try {
            CompletableFuture<Void> disconnected = connection.disconnect();
            disconnected.get();
        } catch (Exception ex) {
            fail("Exception during disconnect: " + ex.getMessage());
        }

    }

    boolean Mqtt3Connect(Mqtt5Client client) throws Exception {
        try {
            connection = MqttClientConnection.NewConnection(client, events);
            if (connectionMessageTransfomer != null) {
                connection.onMessage(connectionMessageTransfomer);
            }
            CompletableFuture<Boolean> connected = connection.connect();
            connected.get();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        return true;
    }

    /* Minimal creation and clean up */
    @Test
    public void TestCreationMinimal() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_DIRECT_MQTT_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_PORT);
        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_HOST,
                    Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_PORT));
            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
            builder.withConnectOptions(connectBuilder.build());
            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                assertNotNull(client);
                MqttClientConnection.NewConnection(client, null);
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Maximum creation and cleanup */
    @Test
    public void TestCreationFull() {
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
                    SocketOptions socketOptions = new SocketOptions();) {

                PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder();
                willPacketBuilder.withQOS(QOS.AT_LEAST_ONCE).withPayload("Hello World".getBytes())
                        .withTopic("test/topic");

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
                        .withLifecycleEvents(new software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.LifecycleEvents() {
                            @Override
                            public void onAttemptingConnect(Mqtt5Client client,
                                    software.amazon.awssdk.crt.mqtt5.OnAttemptingConnectReturn onAttemptingConnectReturn) {
                            }

                            @Override
                            public void onConnectionSuccess(Mqtt5Client client,
                                    software.amazon.awssdk.crt.mqtt5.OnConnectionSuccessReturn onConnectionSuccessReturn) {
                            }

                            @Override
                            public void onConnectionFailure(Mqtt5Client client,
                                    software.amazon.awssdk.crt.mqtt5.OnConnectionFailureReturn onConnectionFailureReturn) {
                            }

                            @Override
                            public void onDisconnection(Mqtt5Client client,
                                    OnDisconnectionReturn onDisconnectionReturn) {
                            }

                            @Override
                            public void onStopped(Mqtt5Client client, OnStoppedReturn onStoppedReturn) {
                            }
                        })
                        .withMaxReconnectDelayMs(1000L)
                        .withMinConnectedTimeToResetReconnectDelayMs(1000L)
                        .withMinReconnectDelayMs(1000L)
                        .withOfflineQueueBehavior(ClientOfflineQueueBehavior.FAIL_ALL_ON_DISCONNECT)
                        .withAckTimeoutSeconds(1000L)
                        .withPingTimeoutMs(1000L)
                        .withPublishEvents(new PublishEvents() {
                            @Override
                            public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn) {
                            }
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
                    MqttClientConnection connection = MqttClientConnection.NewConnection(client, null);
                }
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /****************************************************************
     * CONNECT THROUGH MQTT5 INTERFACE
     ****************************************************************/
    /* Happy path. Direct connection with minimal configuration */
    @Test
    public void TestDirectConnectThroughMqtt5() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_DIRECT_MQTT_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_PORT);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_HOST,
                    Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_PORT));
            builder.withLifecycleEvents(events);
            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
            builder.withConnectOptions(connectBuilder.build());

            try (Mqtt5Client client = new Mqtt5Client(builder.build());) {
                MqttClientConnection connection = MqttClientConnection.NewConnection(client, null);
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                client.stop(disconnect.build());
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Direct connection with basic authentication */
    @Test
    public void TestBasicAuthConnectThroughMqtt5() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT,
                AWS_TEST_MQTT5_BASIC_AUTH_USERNAME, AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST,
                    Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT));
            builder.withLifecycleEvents(events);
            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();
            connectOptions.withUsername(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME)
                    .withPassword(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD.getBytes())
                    .withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
            builder.withConnectOptions(connectOptions.build());

            try (Mqtt5Client client = new Mqtt5Client(builder.build());) {
                MqttClientConnection connection = MqttClientConnection.NewConnection(client, null);
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                client.stop(disconnect.build());
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Direct connection with mTLS */
    @Test
    public void TestmTLSConnectThroughMqtt5() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                    TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                            AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
                    TlsContext tlsContext = new TlsContext(tlsOptions);) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);
                ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();
                connectOptions.withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
                builder.withConnectOptions(connectOptions.build());

                try (Mqtt5Client client = new Mqtt5Client(builder.build());) {
                        MqttClientConnection connection = MqttClientConnection.NewConnection(client, null);
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                    client.stop(disconnect.build());
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Happy path. Websocket connection with minimal configuration */
    @Test
    public void TestWebsocketMinimalConnectThroughMqtt5() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_WS_MQTT_HOST, AWS_TEST_MQTT5_WS_MQTT_PORT);
        try {

            try (
                    EventLoopGroup elg = new EventLoopGroup(1);
                    HostResolver hr = new HostResolver(elg);
                    ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);) {

                LifecycleEvents_Futured events = new LifecycleEvents_Futured();

                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                        AWS_TEST_MQTT5_WS_MQTT_HOST, Long.parseLong(AWS_TEST_MQTT5_WS_MQTT_PORT));
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);
                ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
                connectBuilder.withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
                builder.withConnectOptions(connectBuilder.build());

                Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                    @Override
                    public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                        t.complete(t.getHttpRequest());
                    }
                };
                builder.withWebsocketHandshakeTransform(websocketTransform);

                try (Mqtt5Client client = new Mqtt5Client(builder.build());) {
                        MqttClientConnection connection = MqttClientConnection.NewConnection(client, null);
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                    client.stop(disconnect.build());
                }
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Websocket connection with HttpProxyOptions */
    @Test
    public void TestWebsocketHttpProxyConnectThroughMqtt5() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_WS_MQTT_TLS_HOST, AWS_TEST_MQTT5_WS_MQTT_TLS_PORT,
                AWS_TEST_MQTT5_PROXY_HOST, AWS_TEST_MQTT5_PROXY_PORT);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_WS_MQTT_TLS_HOST, Long.parseLong(AWS_TEST_MQTT5_WS_MQTT_TLS_PORT));
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);
            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
            builder.withConnectOptions(connectBuilder.build());

            TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
            tlsOptions.withVerifyPeer(false);
            TlsContext tlsContext = new TlsContext(tlsOptions);
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

            Mqtt5Client client = new Mqtt5Client(builder.build());
            MqttClientConnection connection = MqttClientConnection.NewConnection(client, null);

            client.start();
            events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());

            client.close();
            tlsContext.close();
            tlsOptions.close();
            elg.close();
            hr.close();
            bootstrap.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /****************************************************************
     * CONNECT THROUGH MQTT311 INTERFACE
     ****************************************************************/
    /* Happy path. Direct connection with minimal configuration */
    @Test
    public void TestDirectConnectThroughMqtt311() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_DIRECT_MQTT_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_PORT);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_HOST,
                    Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_PORT));
            builder.withLifecycleEvents(events);
            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
            builder.withConnectOptions(connectBuilder.build());

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                Mqtt3Connect(client);
                Mqtt3ConnectionDisconnect();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Direct connection with basic authentication */
    @Test
    public void TestBasicAuthConnectThroughMqtt311() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST, AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT,
                AWS_TEST_MQTT5_BASIC_AUTH_USERNAME, AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST,
                    Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT));
            builder.withLifecycleEvents(events);

            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();
            connectOptions.withUsername(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME)
                    .withPassword(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD.getBytes())
                    .withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
            builder.withConnectOptions(connectOptions.build());

            try (Mqtt5Client client = new Mqtt5Client(builder.build());) {
                    MqttClientConnection connection = MqttClientConnection.NewConnection(client, null);
                Mqtt3Connect(client);
                Mqtt3ConnectionDisconnect();
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Direct connection with mTLS */
    @Test
    public void TestmTLSConnectThroughMqtt311() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                    TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                            AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
                    TlsContext tlsContext = new TlsContext(tlsOptions);) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);
                ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
                connectBuilder.withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
                builder.withConnectOptions(connectBuilder.build());
                try (Mqtt5Client client = new Mqtt5Client(builder.build());) {
                        MqttClientConnection connection = MqttClientConnection.NewConnection(client, null);
                    Mqtt3Connect(client);
                    Mqtt3ConnectionDisconnect();

                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Happy path. Websocket connection with minimal configuration */
    @Test
    public void TestWebsocketMinimalConnectThroughMqtt311() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_WS_MQTT_HOST, AWS_TEST_MQTT5_WS_MQTT_PORT);
        try {

            try (
                    EventLoopGroup elg = new EventLoopGroup(1);
                    HostResolver hr = new HostResolver(elg);
                    ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);) {

                LifecycleEvents_Futured events = new LifecycleEvents_Futured();

                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                        AWS_TEST_MQTT5_WS_MQTT_HOST, Long.parseLong(AWS_TEST_MQTT5_WS_MQTT_PORT));
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);
                ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
                connectBuilder.withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
                builder.withConnectOptions(connectBuilder.build());

                Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                    @Override
                    public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                        t.complete(t.getHttpRequest());
                    }
                };
                builder.withWebsocketHandshakeTransform(websocketTransform);

                try (Mqtt5Client client = new Mqtt5Client(builder.build());) {
                        MqttClientConnection connection = MqttClientConnection.NewConnection(client, null);
                    Mqtt3Connect(client);
                    Mqtt3ConnectionDisconnect();

                }
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Websocket connection with HttpProxyOptions */
    @Test
    public void TestWebsocketHttpProxyConnectThroughMqtt311() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_WS_MQTT_TLS_HOST, AWS_TEST_MQTT5_WS_MQTT_TLS_PORT,
                AWS_TEST_MQTT5_PROXY_HOST, AWS_TEST_MQTT5_PROXY_PORT);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_WS_MQTT_TLS_HOST, Long.parseLong(AWS_TEST_MQTT5_WS_MQTT_TLS_PORT));
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);

            TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
            tlsOptions.withVerifyPeer(false);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            builder.withTlsContext(tlsContext);
            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
            builder.withConnectOptions(connectBuilder.build());

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

            Mqtt5Client client = new Mqtt5Client(builder.build());

            Mqtt3Connect(client);
            Mqtt3ConnectionDisconnect();


            client.close();
            tlsContext.close();
            tlsOptions.close();
            elg.close();
            hr.close();
            bootstrap.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /****************************************************************
     * OPERATION TEST CASE
     ****************************************************************/
    @Test
    public void TestOperationSubUnsub() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5to3Adapter_Binding_Java_" + testUUID;
        String clientId = "test/MQTT5TO3Adapter_ClientId" + testUUID;
        String testPayload = "PUBLISH ME!";

        Consumer<MqttMessage> messageHandler = (message) -> {
            byte[] payload = message.getPayload();
            try {
                assertEquals(testTopic, message.getTopic());
                String contents = new String(payload, "UTF-8");
                assertEquals("Message is intact", testPayload, contents);
            } catch (Exception ex) {
                fail("Unable to decode payload: " + ex.getMessage());
            }
        };

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                    AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            PublishEvents_Futured publishEvents = new PublishEvents_Futured();
            builder.withPublishEvents(publishEvents);
            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId(clientId);
            builder.withConnectOptions(connectBuilder.build());

            try (Mqtt5Client client = new Mqtt5Client(builder.build());) {
                    MqttClientConnection connection = MqttClientConnection.NewConnection(client, null);
                connection.onMessage(messageHandler);
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                CompletableFuture<MqttMessage> receivedFuture = new CompletableFuture<>();
                Consumer<MqttMessage> subscriberMessageHandler = (message) -> {
                    receivedFuture.complete(message);
                };

                CompletableFuture<Integer> subscribed = connection.subscribe(testTopic, QualityOfService.AT_LEAST_ONCE,
                        subscriberMessageHandler);
                subscribed.thenApply(unused -> subsAcked++);
                int packetId = subscribed.get();

                assertNotSame(0, packetId);
                assertEquals("Single subscription", 1, subsAcked);

                MqttMessage message = new MqttMessage(testTopic, testPayload.getBytes(), QualityOfService.AT_LEAST_ONCE,
                        false);
                CompletableFuture<Integer> published = connection.publish(message);
                published.thenApply(unused -> pubsAcked++);
                packetId = published.get();

                assertNotSame(0, packetId);
                assertEquals("Published", 1, pubsAcked);

                published = connection.publish(message);
                published.thenApply(unused -> pubsAcked++);
                packetId = published.get();

                assertNotSame(0, packetId);
                assertEquals("Published", 2, pubsAcked);

                MqttMessage received = receivedFuture.get();
                assertEquals("Received", message.getTopic(), received.getTopic());
                assertArrayEquals("Received", message.getPayload(), received.getPayload());
                assertEquals("Received", message.getQos(), received.getQos());
                assertEquals("Received", message.getRetain(), received.getRetain());

                CompletableFuture<Integer> unsubscribed = connection.unsubscribe(testTopic);
                unsubscribed.thenApply(unused -> subsAcked--);
                packetId = unsubscribed.get();

                assertNotSame(0, packetId);

                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /****************************************************************
     * MQTT311 LIFECYCLE CALLBACK TEST CASE
     ****************************************************************/
    @Test
    public void TestConnectionSuccessCallback() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                    TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                            AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
                    TlsContext tlsContext = new TlsContext(tlsOptions);) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);
                ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
                connectBuilder.withClientId("test/MQTT5to3Adapter" + UUID.randomUUID().toString());
                builder.withConnectOptions(connectBuilder.build());
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    connection = MqttClientConnection.NewConnection(client, this.events);
                    connection.connect();
                    onConnectionSuccessFuture.get();
                    Mqtt3ConnectionDisconnect();

                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void TestConnectionFailureCallback() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST,
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                    TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                            AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
                    TlsContext tlsContext = new TlsContext(tlsOptions);) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder("badhost",
                        8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);
                ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
                connectBuilder.withClientId("test/MQTT5to3Adapter" +
                        UUID.randomUUID().toString());
                builder.withConnectOptions(connectBuilder.build());
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    connection = MqttClientConnection.NewConnection(client, this.events);
                    connection.connect();
                    onConnectionFailureFuture.get();
                    Mqtt3ConnectionDisconnect();

                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /****************************************************************
     * MQTT311 ADAPTER TEST CASE
     ****************************************************************/

    @Test
    public void TestMultipleAdapter() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
        String testUUID = UUID.randomUUID().toString();
        String testTopic1 = "test/MQTT5to3Adapter1" + testUUID;
        String testTopic2 = "test/MQTT5to3Adapter2" + testUUID;
        String clientId = "test/MQTT5TO3Adapter_ClientId" + testUUID;

        String testPayload = "PUBLISH ME!";

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                    AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            PublishEvents_Futured publishEvents = new PublishEvents_Futured();
            builder.withPublishEvents(publishEvents);
            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId(clientId);
            builder.withConnectOptions(connectBuilder.build());

            try (Mqtt5Client client = new Mqtt5Client(builder.build());) {
                // Connect
                client.start();

                MqttClientConnection connection1 = MqttClientConnection.NewConnection(client, null);
                MqttClientConnection connection2 = MqttClientConnection.NewConnection(client, null);
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                CompletableFuture<MqttMessage> receivedFuture1 = new CompletableFuture<>();
                Consumer<MqttMessage> subscriberMessageHandler1 = (message) -> {
                    receivedFuture1.complete(message);
                };
                CompletableFuture<MqttMessage> receivedFuture2 = new CompletableFuture<>();
                Consumer<MqttMessage> subscriberMessageHandler2 = (message) -> {
                    receivedFuture2.complete(message);
                };

                CompletableFuture<Integer> subscribed = connection1.subscribe(testTopic1,
                        QualityOfService.AT_LEAST_ONCE,
                        subscriberMessageHandler1);
                int packetId = subscribed.get();
                assertNotSame(0, packetId);

                subscribed = connection2.subscribe(testTopic2, QualityOfService.AT_LEAST_ONCE,
                        subscriberMessageHandler2);
                packetId = subscribed.get();
                assertNotSame(0, packetId);

                MqttMessage message1 = new MqttMessage(testTopic1, testPayload.getBytes(),
                        QualityOfService.AT_LEAST_ONCE,
                        false);
                CompletableFuture<Integer> published = connection1.publish(message1);
                packetId = published.get();

                assertNotSame(0, packetId);

                MqttMessage message2 = new MqttMessage(testTopic2, testPayload.getBytes(),
                        QualityOfService.AT_LEAST_ONCE,
                        false);
                published = connection2.publish(message2);
                packetId = published.get();

                assertNotSame(0, packetId);

                assertEquals("Received", message1.getTopic(), receivedFuture1.get().getTopic());
                assertArrayEquals("Received", message1.getPayload(), receivedFuture1.get().getPayload());
                assertEquals("Received", message2.getQos(), receivedFuture2.get().getQos());
                assertEquals("Received", message2.getRetain(), receivedFuture2.get().getRetain());

                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

};
