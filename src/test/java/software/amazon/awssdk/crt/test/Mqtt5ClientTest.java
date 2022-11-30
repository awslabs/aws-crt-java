package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.http.HttpProxyOptions.HttpProxyConnectionType;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
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
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Mqtt5ClientTest extends CrtTestFixture {

    // Codebuild environment variables
    private String mqtt5DirectMqttHost;
    private Long mqtt5DirectMqttPort;
    private String mqtt5DirectMqttBasicAuthHost;
    private Long mqtt5DirectMqttBasicAuthPort;
    private String mqtt5DirectMqttTlsHost;
    private Long mqtt5DirectMqttTlsPort;
    private String mqtt5WSMqttHost;
    private Long mqtt5WSMqttPort;
    private String mqtt5WSMqttBasicAuthHost;
    private Long mqtt5WSMqttBasicAuthPort;
    private String mqtt5WSMqttTlsHost;
    private Long mqtt5WSMqttTlsPort;
    private String mqtt5BasicAuthUsername;
    private String mqtt5BasicAuthPassword;
    private String mqtt5ProxyHost;
    private Long mqtt5ProxyPort;
    private String mqtt5CertificateFile;
    private String mqtt5KeyFile;
    // IoT Core environment variables
    private String mqtt5IoTCoreMqttHost;
    private Long mqtt5IoTCoreMqttPort;
    private String mqtt5IoTCoreMqttCertificateFile;
    private String mqtt5IoTCoreMqttKeyFile;

    private void populateTestingEnvironmentVariables() {
        mqtt5DirectMqttHost = System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_HOST");
        if (System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_PORT") != null) {
            mqtt5DirectMqttPort = Long.parseLong(System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_PORT"));
        }
        mqtt5DirectMqttBasicAuthHost = System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST");
        if (System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT") != null) {
            mqtt5DirectMqttBasicAuthPort = Long.parseLong(System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT"));
        }
        mqtt5DirectMqttTlsHost = System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST");
        if (System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT") != null) {
            mqtt5DirectMqttTlsPort = Long.parseLong(System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT"));
        }
        mqtt5WSMqttHost = System.getenv("AWS_TEST_MQTT5_WS_MQTT_HOST");
        if (System.getenv("AWS_TEST_MQTT5_WS_MQTT_PORT") != null) {
            mqtt5WSMqttPort = Long.parseLong(System.getenv("AWS_TEST_MQTT5_WS_MQTT_PORT"));
        }
        mqtt5WSMqttBasicAuthHost = System.getenv("AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST");
        if (System.getenv("AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT") != null) {
            mqtt5WSMqttBasicAuthPort = Long.parseLong(System.getenv("AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT"));
        }
        mqtt5WSMqttTlsHost = System.getenv("AWS_TEST_MQTT5_WS_MQTT_TLS_HOST");
        if (System.getenv("AWS_TEST_MQTT5_WS_MQTT_TLS_PORT") != null) {
            mqtt5WSMqttTlsPort = Long.parseLong(System.getenv("AWS_TEST_MQTT5_WS_MQTT_TLS_PORT"));
        }
        mqtt5BasicAuthUsername = System.getenv("AWS_TEST_MQTT5_BASIC_AUTH_USERNAME");
        mqtt5BasicAuthPassword = System.getenv("AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD");
        mqtt5ProxyHost = System.getenv("AWS_TEST_MQTT5_PROXY_HOST");
        if (System.getenv("AWS_TEST_MQTT5_PROXY_PORT") != null) {
            mqtt5ProxyPort = Long.parseLong(System.getenv("AWS_TEST_MQTT5_PROXY_PORT"));
        }
        mqtt5CertificateFile = System.getenv("AWS_TEST_MQTT5_CERTIFICATE_FILE");
        mqtt5KeyFile = System.getenv("AWS_TEST_MQTT5_KEY_FILE");

        mqtt5IoTCoreMqttHost = System.getenv("AWS_TEST_MQTT5_IOT_CORE_MQTT_HOST");
        if (System.getenv("AWS_TEST_MQTT5_IOT_CORE_MQTT_PORT") != null) {
            mqtt5IoTCoreMqttPort = Long.parseLong(System.getenv("AWS_TEST_MQTT5_IOT_CORE_MQTT_PORT"));
        }
        mqtt5IoTCoreMqttCertificateFile = System.getenv("AWS_TEST_MQTT5_IOT_CORE_MQTT_CERTIFICATE_FILE");
        mqtt5IoTCoreMqttKeyFile = System.getenv("AWS_TEST_MQTT5_IOT_CORE_MQTT_KEY_FILE");
    }

    public Mqtt5ClientTest() {
        populateTestingEnvironmentVariables();
    }

    /**
     * ============================================================
     * TEST HELPER FUNCTIONS
     * ============================================================
     */

    static final class LifecycleEvents_Futured implements Mqtt5ClientOptions.LifecycleEvents {
        CompletableFuture<Void> connectedFuture = new CompletableFuture<>();
        CompletableFuture<Void> stopFuture = new CompletableFuture<>();

        ConnAckPacket connectSuccessPacket = null;
        NegotiatedSettings connectSuccessSettings = null;

        int connectFailureCode = 0;
        ConnAckPacket connectFailurePacket = null;

        int disconnectFailureCode = 0;
        DisconnectPacket disconnectPacket = null;

        @Override
        public void onAttemptingConnect(Mqtt5Client client, OnAttemptingConnectReturn onAttemptingConnectReturn) {}

        @Override
        public void onConnectionSuccess(Mqtt5Client client, OnConnectionSuccessReturn onConnectionSuccessReturn) {
            ConnAckPacket connAckData = onConnectionSuccessReturn.getConnAckPacket();
            NegotiatedSettings negotiatedSettings = onConnectionSuccessReturn.getNegotiatedSettings();
            connectSuccessPacket = connAckData;
            connectSuccessSettings = negotiatedSettings;
            connectedFuture.complete(null);
        }

        @Override
        public void onConnectionFailure(Mqtt5Client client, OnConnectionFailureReturn onConnectionFailureReturn) {
            connectFailureCode = onConnectionFailureReturn.getErrorCode();
            connectFailurePacket = onConnectionFailureReturn.getConnAckPacket();
            connectedFuture.completeExceptionally(new Exception("Could not connect!"));
        }

        @Override
        public void onDisconnection(Mqtt5Client client, OnDisconnectionReturn onDisconnectionReturn) {
            disconnectFailureCode = onDisconnectionReturn.getErrorCode();
            disconnectPacket = onDisconnectionReturn.getDisconnectPacket();
        }

        @Override
        public void onStopped(Mqtt5Client client, OnStoppedReturn onStoppedReturn) {
            stopFuture.complete(null);
        }
    }

    static final class PublishEvents_Futured implements PublishEvents {
        CompletableFuture<Void> publishReceivedFuture = new CompletableFuture<>();
        PublishPacket publishPacket = null;

        @Override
        public void onMessageReceived(Mqtt5Client client, PublishReturn result) {
            publishPacket = result.getPublishPacket();
            publishReceivedFuture.complete(null);
        }
    }

    static final class PublishEvents_Futured_Counted implements PublishEvents {
        CompletableFuture<Void> publishReceivedFuture = new CompletableFuture<>();
        int currentPublishCount = 0;
        int desiredPublishCount = 0;
        List<PublishPacket> publishPacketsRecieved = new ArrayList<PublishPacket>();

        @Override
        public void onMessageReceived(Mqtt5Client client, PublishReturn result) {
            currentPublishCount += 1;
            if (currentPublishCount == desiredPublishCount) {
                publishReceivedFuture.complete(null);
            } else if (currentPublishCount > desiredPublishCount) {
                publishReceivedFuture.completeExceptionally(new Throwable("Too many publish packets received"));
            }

            if (publishPacketsRecieved.contains(result)) {
                publishReceivedFuture.completeExceptionally(new Throwable("Duplicate publish packet received!"));
            }
            publishPacketsRecieved.add(result.getPublishPacket());
        }
    }

    private boolean checkMinimumDirectHostAndPort() {
        if (mqtt5IoTCoreMqttHost != null && mqtt5IoTCoreMqttPort != null) {
            return true;
        } else if (mqtt5DirectMqttHost != null && mqtt5DirectMqttPort != null) {
            return true;
        }
        return false;
    }

    private String getMinimumDirectHost() {
        if (mqtt5IoTCoreMqttHost != null) {
            return mqtt5IoTCoreMqttHost;
        } else {
            return mqtt5DirectMqttHost;
        }
    }

    private Long getMinimumDirectPort() {
        if (mqtt5IoTCoreMqttPort != null) {
            return mqtt5IoTCoreMqttPort;
        } else {
            return mqtt5DirectMqttPort;
        }
    }

    private String getMinimumDirectCert() {
        if (mqtt5IoTCoreMqttCertificateFile != null) {
            return mqtt5IoTCoreMqttCertificateFile;
        } else {
            return mqtt5CertificateFile;
        }
    }

    private String getMinimumDirectKey() {
        if (mqtt5IoTCoreMqttKeyFile != null) {
            return mqtt5IoTCoreMqttKeyFile;
        } else {
            return mqtt5KeyFile;
        }
    }

    /**
     * ============================================================
     * CREATION TEST CASES
     * ============================================================
     */

    /* Happy path. Minimal creation and cleanup */
    @Test
    public void New_UC1() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            Mqtt5Client client = new Mqtt5Client(builder.build());
            assertNotNull(client);
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Maximum creation and cleanup */
    @Test
    public void New_UC2() {
        Assume.assumeTrue(mqtt5DirectMqttHost != null);
        Assume.assumeTrue(mqtt5DirectMqttPort != null);
        Assume.assumeTrue(mqtt5BasicAuthPassword != null);
        Assume.assumeTrue(mqtt5BasicAuthUsername != null);
        Assume.assumeTrue(mqtt5ProxyHost != null);
        Assume.assumeTrue(mqtt5ProxyPort != null);

        try {
            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            SocketOptions socketOptions = new SocketOptions();

            PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder();
            willPacketBuilder.withQOS(QOS.AT_LEAST_ONCE).withPayload("Hello World".getBytes()).withTopic("test/topic");

            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId("MQTT5 CRT")
            .withKeepAliveIntervalSeconds(1000L)
            .withMaximumPacketSizeBytes(1000L)
            .withPassword(mqtt5BasicAuthPassword.getBytes())
            .withReceiveMaximum(1000L)
            .withRequestProblemInformation(true)
            .withRequestResponseInformation(true)
            .withSessionExpiryIntervalSeconds(1000L)
            .withUsername(mqtt5BasicAuthUsername)
            .withWill(willPacketBuilder.build())
            .withWillDelayIntervalSeconds(1000L);

            ArrayList<UserProperty> userProperties = new ArrayList<UserProperty>();
            userProperties.add(new UserProperty("Hello", "World"));
            connectBuilder.withUserProperties(userProperties);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5DirectMqttHost, mqtt5DirectMqttPort);
            builder.withBootstrap(bootstrap)
            .withConnackTimeoutMs(100L)
            .withConnectOptions(connectBuilder.build())
            .withExtendedValidationAndFlowControlOptions(ExtendedValidationAndFlowControlOptions.NONE)
            .withHostName(mqtt5DirectMqttHost)
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
            .withPort(mqtt5DirectMqttPort)
            .withPublishEvents(new PublishEvents() {
                @Override
                public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn) {}
            })
            .withRetryJitterMode(JitterMode.Default)
            .withSessionBehavior(ClientSessionBehavior.CLEAN)
            .withSocketOptions(socketOptions);
            // Skip websocket, proxy options, and TLS options - those are all different tests

            Mqtt5Client client = new Mqtt5Client(builder.build());
            assertNotNull(client);

            elg.close();
            hr.close();
            bootstrap.close();
            socketOptions.close();
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Minimal memory check */
    @Test
    public void New_UC3() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            Mqtt5Client client = new Mqtt5Client(builder.build());
            assertNotNull(client);
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        CrtResource.waitForNoResources();
    }

    /* Maximum memory test */
    @Test
    public void New_UC4() {
        Assume.assumeTrue(mqtt5DirectMqttHost != null);
        Assume.assumeTrue(mqtt5DirectMqttPort != null);
        Assume.assumeTrue(mqtt5BasicAuthPassword != null);
        Assume.assumeTrue(mqtt5BasicAuthUsername != null);

        try {
            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            SocketOptions socketOptions = new SocketOptions();

            PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder();
            willPacketBuilder.withQOS(QOS.AT_LEAST_ONCE).withPayload("Hello World".getBytes()).withTopic("test/topic");

            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId("MQTT5 CRT");
            connectBuilder.withKeepAliveIntervalSeconds(1000L);
            connectBuilder.withMaximumPacketSizeBytes(1000L);
            connectBuilder.withPassword(mqtt5BasicAuthPassword.getBytes());
            connectBuilder.withReceiveMaximum(1000L);
            connectBuilder.withRequestProblemInformation(true);
            connectBuilder.withRequestResponseInformation(true);
            connectBuilder.withSessionExpiryIntervalSeconds(1000L);
            connectBuilder.withUsername(mqtt5BasicAuthUsername);
            connectBuilder.withWill(willPacketBuilder.build());
            connectBuilder.withWillDelayIntervalSeconds(1000L);

            ArrayList<UserProperty> userProperties = new ArrayList<UserProperty>();
            userProperties.add(new UserProperty("Hello", "World"));
            connectBuilder.withUserProperties(userProperties);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5DirectMqttHost, mqtt5DirectMqttPort);
            builder.withBootstrap(bootstrap)
            .withConnackTimeoutMs(1000L)
            .withConnectOptions(connectBuilder.build())
            .withExtendedValidationAndFlowControlOptions(ExtendedValidationAndFlowControlOptions.NONE)
            .withHostName(mqtt5DirectMqttHost)
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
            .withPort(mqtt5DirectMqttPort)
            .withPublishEvents(new PublishEvents() {
                @Override
                public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn) {}
            })
            .withRetryJitterMode(JitterMode.Default)
            .withSessionBehavior(ClientSessionBehavior.CLEAN)
            .withSocketOptions(socketOptions);
            // Skip websocket, proxy options, and TLS options - those are all different tests

            Mqtt5Client client = new Mqtt5Client(builder.build());
            assertNotNull(client);

            bootstrap.close();
            hr.close();
            elg.close();
            socketOptions.close();
            client.close();

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

    /* Happy path. Direct connection with minimal configuration */
    @Test
    public void ConnDC_UC1() {
        Assume.assumeTrue(mqtt5DirectMqttHost != null);
        Assume.assumeTrue(mqtt5DirectMqttPort != null);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5DirectMqttHost, mqtt5DirectMqttPort);
            builder.withLifecycleEvents(events);
            builder.withPort(mqtt5DirectMqttPort);
            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Direct connection with basic authentication */
    @Test
    public void ConnDC_UC2() {
        Assume.assumeTrue(mqtt5DirectMqttBasicAuthHost != null);
        Assume.assumeTrue(mqtt5DirectMqttBasicAuthPort != null);
        Assume.assumeTrue(mqtt5BasicAuthUsername != null);
        Assume.assumeTrue(mqtt5BasicAuthPassword != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5DirectMqttBasicAuthHost, mqtt5DirectMqttBasicAuthPort);
            builder.withLifecycleEvents(events);

            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();
            connectOptions.withUsername(mqtt5BasicAuthUsername).withPassword(mqtt5BasicAuthPassword.getBytes());
            builder.withConnectOptions(connectOptions.build());

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Direct connection with TLS */
    @Test
    public void ConnDC_UC3() {
        Assume.assumeTrue(mqtt5DirectMqttTlsHost != null);
        Assume.assumeTrue(mqtt5DirectMqttTlsPort != null);
        Assume.assumeTrue(mqtt5CertificateFile != null);
        Assume.assumeTrue(mqtt5KeyFile != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
            tlsOptions.withVerifyPeer(false);
            TlsContext tlsContext = new TlsContext(tlsOptions);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5DirectMqttTlsHost, mqtt5DirectMqttTlsPort);
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());

            tlsContext.close();
            tlsOptions.close();
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Direct connection with mTLS */
    @Test
    public void ConnDC_UC4() {
        Assume.assumeTrue(mqtt5IoTCoreMqttHost != null);
        Assume.assumeTrue(mqtt5IoTCoreMqttPort != null);
        Assume.assumeTrue(mqtt5IoTCoreMqttCertificateFile != null);
        Assume.assumeTrue(mqtt5IoTCoreMqttKeyFile != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(mqtt5IoTCoreMqttCertificateFile, mqtt5IoTCoreMqttKeyFile);
            TlsContext tlsContext = new TlsContext(tlsOptions);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5IoTCoreMqttHost, mqtt5IoTCoreMqttPort);
            builder.withLifecycleEvents(events);
            builder.withTlsContext(tlsContext);

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());

            tlsContext.close();
            tlsOptions.close();
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Direct connection with HttpProxyOptions */
    @Test
    public void ConnDC_UC5() {
        Assume.assumeTrue(mqtt5DirectMqttTlsHost != null);
        Assume.assumeTrue(mqtt5DirectMqttTlsPort != null);
        Assume.assumeTrue(mqtt5ProxyHost != null);
        Assume.assumeTrue(mqtt5ProxyPort != null);

        try {
            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5DirectMqttTlsHost, mqtt5DirectMqttTlsPort);
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);

            HttpProxyOptions proxyOptions = new HttpProxyOptions();
            proxyOptions.setHost(mqtt5ProxyHost);
            proxyOptions.setPort((mqtt5ProxyPort.intValue()));
            proxyOptions.setConnectionType(HttpProxyConnectionType.Tunneling);

            TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
            tlsOptions.withVerifyPeer(false);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            builder.withTlsContext(tlsContext);

            builder.withHttpProxyOptions(proxyOptions);

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());

            elg.close();
            hr.close();
            bootstrap.close();
            tlsContext.close();
            tlsOptions.close();
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Maximum options set connection test */
    @Test
    public void ConnDC_UC6() {
        Assume.assumeTrue(mqtt5DirectMqttHost != null);
        Assume.assumeTrue(mqtt5DirectMqttPort != null);
        Assume.assumeTrue(mqtt5BasicAuthPassword != null);
        Assume.assumeTrue(mqtt5BasicAuthUsername != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            SocketOptions socketOptions = new SocketOptions();

            PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder();
            willPacketBuilder.withQOS(QOS.AT_LEAST_ONCE).withPayload("Hello World".getBytes()).withTopic("test/topic");

            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId("MQTT5 CRT");
            connectBuilder.withKeepAliveIntervalSeconds(1000L);
            connectBuilder.withMaximumPacketSizeBytes(1000L);
            connectBuilder.withPassword(mqtt5BasicAuthPassword.getBytes());
            connectBuilder.withReceiveMaximum(1000L);
            connectBuilder.withRequestProblemInformation(true);
            connectBuilder.withRequestResponseInformation(true);
            connectBuilder.withSessionExpiryIntervalSeconds(1000L);
            connectBuilder.withUsername(mqtt5BasicAuthUsername);
            connectBuilder.withWill(willPacketBuilder.build());
            connectBuilder.withWillDelayIntervalSeconds(1000L);

            ArrayList<UserProperty> userProperties = new ArrayList<UserProperty>();
            userProperties.add(new UserProperty("Hello", "World"));
            connectBuilder.withUserProperties(userProperties);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5DirectMqttHost, mqtt5DirectMqttPort);
            builder.withBootstrap(bootstrap)
            .withConnackTimeoutMs(1000L)
            .withConnectOptions(connectBuilder.build())
            .withExtendedValidationAndFlowControlOptions(ExtendedValidationAndFlowControlOptions.NONE)
            .withHostName(mqtt5DirectMqttHost)
            .withLifecycleEvents(events)
            .withMaxReconnectDelayMs(1000L)
            .withMinConnectedTimeToResetReconnectDelayMs(1000L)
            .withMinReconnectDelayMs(1000L)
            .withOfflineQueueBehavior(ClientOfflineQueueBehavior.FAIL_ALL_ON_DISCONNECT)
            .withAckTimeoutSeconds(1000L)
            .withPingTimeoutMs(1000L)
            .withPort(mqtt5DirectMqttPort)
            .withPublishEvents(new PublishEvents() {
                @Override
                public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn) {}
            })
            .withRetryJitterMode(JitterMode.Default)
            .withSessionBehavior(ClientSessionBehavior.CLEAN)
            .withSocketOptions(socketOptions);
            // Skip websocket, proxy options, and TLS options - those are all different tests

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());

            bootstrap.close();
            hr.close();
            elg.close();
            socketOptions.close();
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        CrtResource.waitForNoResources();
    }

    /**
     * ============================================================
     * WEBSOCKET CONNECT TEST CASES
     * ============================================================
     */

    /* Happy path. Websocket connection with minimal configuration */
    @Test
    public void ConnWS_UC1() {
        Assume.assumeTrue(mqtt5WSMqttHost != null);
        Assume.assumeTrue(mqtt5WSMqttPort != null);
        try {
            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5WSMqttHost, mqtt5WSMqttPort);
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);

            Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                @Override
                public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                    t.complete(t.getHttpRequest());
                }
            };
            builder.withWebsocketHandshakeTransform(websocketTransform);
            builder.withPort(mqtt5WSMqttPort);

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());

            elg.close();
            hr.close();
            bootstrap.close();
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Websocket connection with basic authentication */
    @Test
    public void ConnWS_UC2() {
        Assume.assumeTrue(mqtt5WSMqttBasicAuthHost != null);
        Assume.assumeTrue(mqtt5WSMqttBasicAuthPort != null);
        Assume.assumeTrue(mqtt5BasicAuthUsername != null);
        Assume.assumeTrue(mqtt5BasicAuthPassword != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5WSMqttBasicAuthHost, mqtt5WSMqttBasicAuthPort);
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
            connectOptions.withUsername(mqtt5BasicAuthUsername).withPassword(mqtt5BasicAuthPassword.getBytes());
            builder.withConnectOptions(connectOptions.build());

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());

            elg.close();
            hr.close();
            bootstrap.close();
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Websocket connection with TLS */
    @Test
    public void ConnWS_UC3() {
        Assume.assumeTrue(mqtt5WSMqttTlsHost != null);
        Assume.assumeTrue(mqtt5WSMqttTlsPort != null);
        Assume.assumeTrue(mqtt5CertificateFile != null);
        Assume.assumeTrue(mqtt5KeyFile != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);

            TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
            tlsOptions.withVerifyPeer(false);
            TlsContext tlsContext = new TlsContext(tlsOptions);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5WSMqttTlsHost, mqtt5WSMqttTlsPort);
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

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());

            elg.close();
            hr.close();
            bootstrap.close();
            tlsOptions.close();
            tlsContext.close();
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Websocket connection with HttpProxyOptions */
    /* TODO - get this test working in Codebuild CI */
    // @Test
    // public void ConnWS_UC5() {
    //     Assume.assumeTrue(mqtt5ProxyHost != null);
    //     Assume.assumeTrue(mqtt5ProxyPort != null);
    //     Assume.assumeTrue(mqtt5WSMqttTlsHost != null);
    //     Assume.assumeTrue(mqtt5WSMqttTlsPort != null);
    //     Assume.assumeTrue(mqtt5CertificateFile != null);
    //     Assume.assumeTrue(mqtt5KeyFile != null);

    //     try {
    //         LifecycleEvents_Futured events = new LifecycleEvents_Futured();

    //         EventLoopGroup elg = new EventLoopGroup(1);
    //         HostResolver hr = new HostResolver(elg);
    //         ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);

    //         Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5WSMqttTlsHost, mqtt5WSMqttTlsPort);
    //         builder.withLifecycleEvents(events);
    //         builder.withBootstrap(bootstrap);

    //         TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
    //         tlsOptions.withVerifyPeer(false);
    //         TlsContext tlsContext = new TlsContext(tlsOptions);
    //         builder.withTlsContext(tlsContext);

    //         Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
    //             @Override
    //             public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
    //                 t.complete(t.getHttpRequest());
    //             }
    //         };
    //         builder.withWebsocketHandshakeTransform(websocketTransform);

    //         HttpProxyOptions proxyOptions = new HttpProxyOptions();
    //         proxyOptions.setHost(mqtt5ProxyHost);
    //         proxyOptions.setPort(mqtt5ProxyPort.intValue());
    //         proxyOptions.setConnectionType(HttpProxyConnectionType.Tunneling);
    //         builder.withHttpProxyOptions(proxyOptions);

    //         Mqtt5Client client = new Mqtt5Client(builder.build());

    //         client.start();
    //         events.connectedFuture.get(60, TimeUnit.SECONDS);
    //         DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
    //         client.stop(disconnect.build());

    //         client.close();
    //         tlsContext.close();
    //         tlsOptions.close();
    //         elg.close();
    //         hr.close();
    //         bootstrap.close();

    //     } catch (Exception ex) {
    //         fail(ex.getMessage());
    //     }
    // }

    /* Websocket connection with all options set */
    @Test
    public void ConnWS_UC6() {
        Assume.assumeTrue(mqtt5WSMqttHost != null);
        Assume.assumeTrue(mqtt5WSMqttPort != null);
        Assume.assumeTrue(mqtt5BasicAuthPassword != null);
        Assume.assumeTrue(mqtt5BasicAuthUsername != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            SocketOptions socketOptions = new SocketOptions();

            PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder();
            willPacketBuilder.withQOS(QOS.AT_LEAST_ONCE).withPayload("Hello World".getBytes()).withTopic("test/topic");

            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId("MQTT5 CRT");
            connectBuilder.withKeepAliveIntervalSeconds(1000L);
            connectBuilder.withMaximumPacketSizeBytes(1000L);
            connectBuilder.withPassword(mqtt5BasicAuthPassword.getBytes());
            connectBuilder.withReceiveMaximum(1000L);
            connectBuilder.withRequestProblemInformation(true);
            connectBuilder.withRequestResponseInformation(true);
            connectBuilder.withSessionExpiryIntervalSeconds(1000L);
            connectBuilder.withUsername(mqtt5BasicAuthUsername);
            connectBuilder.withWill(willPacketBuilder.build());
            connectBuilder.withWillDelayIntervalSeconds(1000L);

            ArrayList<UserProperty> userProperties = new ArrayList<UserProperty>();
            userProperties.add(new UserProperty("Hello", "World"));
            connectBuilder.withUserProperties(userProperties);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5WSMqttHost, mqtt5WSMqttPort);
            builder.withBootstrap(bootstrap)
            .withConnackTimeoutMs(1000L)
            .withConnectOptions(connectBuilder.build())
            .withExtendedValidationAndFlowControlOptions(ExtendedValidationAndFlowControlOptions.NONE)
            .withHostName(mqtt5WSMqttHost)
            .withLifecycleEvents(events)
            .withMaxReconnectDelayMs(1000L)
            .withMinConnectedTimeToResetReconnectDelayMs(1000L)
            .withMinReconnectDelayMs(1000L)
            .withOfflineQueueBehavior(ClientOfflineQueueBehavior.FAIL_ALL_ON_DISCONNECT)
            .withAckTimeoutSeconds(1000L)
            .withPingTimeoutMs(1000L)
            .withPort(mqtt5WSMqttPort)
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

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());

            bootstrap.close();
            hr.close();
            elg.close();
            socketOptions.close();
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
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
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder("_test", getMinimumDirectPort());
            builder.withLifecycleEvents(events);

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();

            try {
                events.connectedFuture.get(60, TimeUnit.SECONDS);
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

            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Client connect with invalid, nonexistent port for direct connection */
    @Test
    public void ConnNegativeID_UC2() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), 65535L);
            builder.withLifecycleEvents(events);
            Mqtt5Client client = new Mqtt5Client(builder.build());
            client.start();

            try {
                System.out.println("NOTE: Exception due to using incorrect port may be printed below!");
                events.connectedFuture.get(60, TimeUnit.SECONDS);
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

            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Client connect with invalid protocol port for direct connection */
    @Test
    public void ConnNegativeID_UC2_ALT() {
        Assume.assumeTrue(mqtt5DirectMqttHost != null);
        Assume.assumeTrue(mqtt5WSMqttPort != null);
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5DirectMqttHost, mqtt5WSMqttPort);
            builder.withLifecycleEvents(events);
            Mqtt5Client client = new Mqtt5Client(builder.build());
            client.start();

            try {
                System.out.println("NOTE: Exception due to invalid port for protocol used may be printed below!");
                events.connectedFuture.get(60, TimeUnit.SECONDS);
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

            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Client connect with invalid, nonexistent port for websocket connection */
    @Test
    public void ConnNegativeID_UC3() {
        Assume.assumeTrue(mqtt5WSMqttHost != null);
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5WSMqttHost, 444L);
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);

            Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                @Override
                public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                    t.complete(t.getHttpRequest());
                }
            };
            builder.withWebsocketHandshakeTransform(websocketTransform);

            Mqtt5Client client = new Mqtt5Client(builder.build());
            client.start();

            try {
                System.out.println("NOTE: Exception due to using non-existent port may be printed below!");
                events.connectedFuture.get(60, TimeUnit.SECONDS);
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

            elg.close();
            hr.close();
            bootstrap.close();
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Client connect with invalid protocol port for websocket connection */
    @Test
    public void ConnNegativeID_UC3_ALT() {
        Assume.assumeTrue(mqtt5WSMqttHost != null);
        Assume.assumeTrue(mqtt5DirectMqttPort != null);
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5WSMqttHost, mqtt5DirectMqttPort);
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);

            Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                @Override
                public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                    t.complete(t.getHttpRequest());
                }
            };
            builder.withWebsocketHandshakeTransform(websocketTransform);

            Mqtt5Client client = new Mqtt5Client(builder.build());
            client.start();

            try {
                System.out.println("NOTE: Exception due to invalid port may be printed below!");
                events.connectedFuture.get(60, TimeUnit.SECONDS);
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

            elg.close();
            hr.close();
            bootstrap.close();
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Client connect with socket timeout */
    @Test
    public void ConnNegativeID_UC4() {
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder("www.example.com", 81L);
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);

            SocketOptions options = new SocketOptions();
            options.connectTimeoutMs = 100;
            builder.withSocketOptions(options);

            Mqtt5Client client = new Mqtt5Client(builder.build());
            client.start();

            try {
                System.out.println("NOTE: Exception due to socket timeout may be printed below!");
                events.connectedFuture.get(60, TimeUnit.SECONDS);
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

            elg.close();
            hr.close();
            bootstrap.close();
            options.close();
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Websocket handshake failure test */
    @Test
    public void ConnNegativeID_UC6() {
        Assume.assumeTrue(mqtt5WSMqttHost != null);
        Assume.assumeTrue(mqtt5WSMqttPort != null);
        boolean foundExpectedError = false;
        boolean exceptionOccurred = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5WSMqttHost, mqtt5WSMqttPort);
            builder.withLifecycleEvents(events);
            builder.withBootstrap(bootstrap);

            Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                @Override
                public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                    t.completeExceptionally(new Throwable("Intentional failure!"));
                }
            };
            builder.withWebsocketHandshakeTransform(websocketTransform);
            builder.withPort(mqtt5WSMqttPort);

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();

            try {
                System.out.println("NOTE: Exception due to websocket handshake failure may be printed below!");
                events.connectedFuture.get(60, TimeUnit.SECONDS);
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

            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
            client.stop(disconnect.build());

            elg.close();
            hr.close();
            bootstrap.close();
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* For the double client ID test */
    static final class LifecycleEvents_DoubleClientID implements Mqtt5ClientOptions.LifecycleEvents {
        CompletableFuture<Void> connectedFuture = new CompletableFuture<>();
        CompletableFuture<Void> disconnectedFuture = new CompletableFuture<>();

        @Override
        public void onAttemptingConnect(Mqtt5Client client, OnAttemptingConnectReturn onAttemptingConnectReturn) {}

        @Override
        public void onConnectionSuccess(Mqtt5Client client, OnConnectionSuccessReturn onConnectionSuccessReturn) {
            connectedFuture.complete(null);
        }

        @Override
        public void onConnectionFailure(Mqtt5Client client, OnConnectionFailureReturn onConnectionFailureReturn) {
            connectedFuture.completeExceptionally(new Exception("Could not connect!"));
        }

        @Override
        public void onDisconnection(Mqtt5Client client, OnDisconnectionReturn onDisconnectionReturn) {
            disconnectedFuture.complete(null);
        }

        @Override
        public void onStopped(Mqtt5Client client, OnStoppedReturn onStoppedReturn) {}
    }

    /* Double Client ID failure test */
    @Test
    public void ConnNegativeID_UC7() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        String testUUID = UUID.randomUUID().toString();

        try {
            LifecycleEvents_DoubleClientID events = new LifecycleEvents_DoubleClientID();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            builder.withLifecycleEvents(events);
            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder().withClientId("test/MQTT5_Binding_Java_" + testUUID);
            builder.withConnectOptions(connectOptions.build());

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            Mqtt5Client clientOne = new Mqtt5Client(builder.build());
            Mqtt5Client clientTwo = new Mqtt5Client(builder.build());

            clientOne.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            clientTwo.start();
            events.connectedFuture = new CompletableFuture<>();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            // Make sure a disconnection happened
            events.disconnectedFuture.get(60, TimeUnit.SECONDS);

            // Stop the clients from disconnecting each other. If we do not do this, then the clients will
            // attempt to reconnect endlessly, making a never ending loop.
            DisconnectPacket disconnect = new DisconnectPacketBuilder().build();
            clientOne.stop(disconnect);
            clientTwo.stop(disconnect);

            clientOne.close();
            clientTwo.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Double Client ID disconnect and then reconnect test */
    @Test
    public void ConnNegativeID_UC7_ALT() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        String testUUID = UUID.randomUUID().toString();

        try {
            LifecycleEvents_DoubleClientID events = new LifecycleEvents_DoubleClientID();
            LifecycleEvents_DoubleClientID eventsTwo = new LifecycleEvents_DoubleClientID();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            builder.withLifecycleEvents(events);
            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder().withClientId("test/MQTT5_Binding_Java_" + testUUID);
            builder.withConnectOptions(connectOptions.build());

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            Mqtt5Client clientOne = new Mqtt5Client(builder.build());

            Mqtt5ClientOptionsBuilder builderTwo = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            builderTwo.withLifecycleEvents(eventsTwo);
            builderTwo.withConnectOptions(connectOptions.build());

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builderTwo.withTlsContext(tlsContext);
            }

            Mqtt5Client clientTwo = new Mqtt5Client(builderTwo.build());

            clientOne.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            clientTwo.start();
            eventsTwo.connectedFuture.get(60, TimeUnit.SECONDS);

            // Make sure the first client was disconnected
            events.disconnectedFuture.get(60, TimeUnit.SECONDS);
            // Disconnect the second client so the first can reconnect
            clientTwo.stop(new DisconnectPacketBuilder().build());

            // Wait until the first client has reconnected
            events.connectedFuture = new CompletableFuture<>();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            assertTrue(clientOne.getIsConnected() == true);

            // Stop the clients from disconnecting each other. If we do not do this, then the clients will
            // attempt to reconnect endlessly, making a never ending loop.
            DisconnectPacket disconnect = new DisconnectPacketBuilder().build();
            clientOne.stop(disconnect);

            clientOne.close();
            clientTwo.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * ============================================================
     * Negative Data Input Tests
     * ============================================================
     */

    /* Negative Connect Packet Properties */
    @Test
    public void NewNegative_UC1() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean clientCreationFailed = false;

        try {
            Mqtt5Client client;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();

            connectOptions.withKeepAliveIntervalSeconds(-100L);
            builder.withConnectOptions(connectOptions.build());
            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }
            clientCreationFailed = false;
            try {
                client = new Mqtt5Client(builder.build());
            } catch (Exception ex) {
                System.out.println("NOTE: Exception due to negative keep alive may be printed below!");
                clientCreationFailed = true;
            }
            if (clientCreationFailed == false) {
                fail("Client creation did not fail with negative KeepAliveIntervalSeconds");
            }
            connectOptions.withKeepAliveIntervalSeconds(100L);

            connectOptions.withSessionExpiryIntervalSeconds(-100L);
            builder.withConnectOptions(connectOptions.build());
            clientCreationFailed = false;
            try {
                System.out.println("NOTE: Exception due to negative session expiry interval may be printed below!");
                client = new Mqtt5Client(builder.build());
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
            try {
                System.out.println("NOTE: Exception due to negative receive maximum may be printed below!");
                client = new Mqtt5Client(builder.build());
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
            try {
                System.out.println("NOTE: Exception due to negative maximum packet byte size may be printed below!");
                client = new Mqtt5Client(builder.build());
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
            try {
                System.out.println("NOTE: Exception due to negative will delay may be printed below!");
                client = new Mqtt5Client(builder.build());
            } catch (Exception ex) {
                clientCreationFailed = true;
            }
            if (clientCreationFailed == false) {
                fail("Client creation did not fail with negative willDelayIntervalSeconds");
            }
            connectOptions.withWillDelayIntervalSeconds(100L);

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Overflow Connect Packet Properties */
    @Test
    public void NewNegative_UC1_ALT() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean clientCreationFailed = false;

        try {
            Mqtt5Client client;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();

            connectOptions.withKeepAliveIntervalSeconds(2147483647L);
            builder.withConnectOptions(connectOptions.build());
            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }
            clientCreationFailed = false;
            try {
                System.out.println("NOTE: Exception due to connect keep alive being too large may be printed below!");
                client = new Mqtt5Client(builder.build());
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
            try {
                System.out.println("NOTE: Exception due to socket expiry interval being too large may be printed below!");
                client = new Mqtt5Client(builder.build());
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
            try {
                System.out.println("NOTE: Exception due to receive maximum being too large may be printed below!");
                client = new Mqtt5Client(builder.build());
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
            try {
                System.out.println("NOTE: Exception due to maximum packet size being too large may be printed below!");
                client = new Mqtt5Client(builder.build());
            } catch (Exception ex) {
                clientCreationFailed = true;
            }
            if (clientCreationFailed == false) {
                fail("Client creation did not fail with overflow MaximumPacketSizeBytes");
            }
            connectOptions.withMaximumPacketSizeBytes(100L);

            // WillDelayIntervalSeconds is an unsigned 64 bit Long, so it cannot overflow it from Java

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Negative Disconnect Packet Properties */
    @Test
    public void NewNegative_UC2() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean clientDisconnectFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            builder.withLifecycleEvents(events);
            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }
            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            DisconnectPacketBuilder disconnectBuilder = new DisconnectPacketBuilder();
            disconnectBuilder.withSessionExpiryIntervalSeconds(-100L);
            try {
                System.out.println("NOTE: Exception due to negative session expiry may be printed below!");
                client.stop(disconnectBuilder.build());
            } catch (Exception ex) {
                clientDisconnectFailed = true;
            }

            if (clientDisconnectFailed == false) {
                fail("Client disconnect packet creation did not fail!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Overflow Disconnect Packet Properties */
    @Test
    public void NewNegative_UC2_ALT() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean clientDisconnectFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            builder.withLifecycleEvents(events);
            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }
            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            DisconnectPacketBuilder disconnectBuilder = new DisconnectPacketBuilder();
            disconnectBuilder.withSessionExpiryIntervalSeconds(9223372036854775807L);
            try {
                System.out.println("NOTE: Exception due to session expiry interval being too large may be printed below!");
                client.stop(disconnectBuilder.build());
            } catch (Exception ex) {
                clientDisconnectFailed = true;
            }

            if (clientDisconnectFailed == false) {
                fail("Client disconnect did not fail!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Negative Publish Packet Properties */
    @Test
    public void NewNegative_UC3() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean clientPublishFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            builder.withLifecycleEvents(events);
            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }
            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            PublishPacketBuilder publishBuilder = new PublishPacketBuilder();
            publishBuilder.withPayload("Hello World".getBytes()).withTopic("test/topic");
            publishBuilder.withMessageExpiryIntervalSeconds(-100L);
            try {
                System.out.println("NOTE: Exception due to negative interval seconds may be printed below!");
                CompletableFuture<PublishResult> future = client.publish(publishBuilder.build());
                future.get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                clientPublishFailed = true;
            }

            if (clientPublishFailed == false) {
                fail("Client publish did not fail!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Overflow Publish Packet Properties */
    @Test
    public void NewNegative_UC3_ALT() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean clientPublishFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            builder.withLifecycleEvents(events);
            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }
            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            PublishPacketBuilder publishBuilder = new PublishPacketBuilder();
            publishBuilder.withPayload("Hello World".getBytes()).withTopic("test/topic");
            publishBuilder.withMessageExpiryIntervalSeconds(9223372036854775807L);
            try {
                System.out.println("NOTE: Exception due to expiry interval seconds being too large may be printed below!");
                CompletableFuture<PublishResult> future = client.publish(publishBuilder.build());
                future.get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                clientPublishFailed = true;
            }

            if (clientPublishFailed == false) {
                fail("Client publish did not fail!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Negative Subscribe Packet Properties */
    @Test
    public void NewNegative_UC4() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean clientSubscribeFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            builder.withLifecycleEvents(events);
            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }
            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            SubscribePacketBuilder subscribeBuilder = new SubscribePacketBuilder();
            subscribeBuilder.withSubscription("test/topic", QOS.AT_LEAST_ONCE);
            subscribeBuilder.withSubscriptionIdentifier(-100L);
            try {
                System.out.println("NOTE: Exception due to negative subscription identifier may be printed below!");
                CompletableFuture<SubAckPacket> future = client.subscribe(subscribeBuilder.build());
                future.get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                clientSubscribeFailed = true;
            }

            if (clientSubscribeFailed == false) {
                fail("Client subscribe did not fail!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Overflow Subscribe Packet Properties */
    @Test
    public void NewNegative_UC4_ALT() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean clientSubscribeFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            builder.withLifecycleEvents(events);
            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }
            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            SubscribePacketBuilder subscribeBuilder = new SubscribePacketBuilder();
            subscribeBuilder.withSubscription("test/topic", QOS.AT_LEAST_ONCE);
            subscribeBuilder.withSubscriptionIdentifier(9223372036854775807L);
            try {
                System.out.println("NOTE: Exception due to subscription identifier being too large may be printed below!");
                CompletableFuture<SubAckPacket> future = client.subscribe(subscribeBuilder.build());
                future.get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                clientSubscribeFailed = true;
            }

            if (clientSubscribeFailed == false) {
                fail("Client subscribe did not fail!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * ============================================================
     * Negotiated Settings Tests
     * ============================================================
     */

    /* Happy path, minimal success test */
    @Test
    public void Negotiated_UC1() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }
            ConnectPacketBuilder optionsBuilder = new ConnectPacketBuilder();
            optionsBuilder.withSessionExpiryIntervalSeconds(600000L);
            builder.withConnectOptions(optionsBuilder.build());

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            assertEquals(
                "Negotiated Settings session expiry interval does not match sent session expiry interval",
                events.connectSuccessSettings.getSessionExpiryInterval(),
                600000L);

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Maximum success test */
    @Test
    public void Negotiated_UC2() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        String testUUID = UUID.randomUUID().toString();

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);
            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            ConnectPacketBuilder optionsBuilder = new ConnectPacketBuilder();
            optionsBuilder.withClientId("test/MQTT5_Binding_Java_" + testUUID);
            optionsBuilder.withSessionExpiryIntervalSeconds(0L);
            optionsBuilder.withKeepAliveIntervalSeconds(360L);
            builder.withConnectOptions(optionsBuilder.build());

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            assertEquals(
                "Negotiated Settings client ID does not match sent client ID",
                events.connectSuccessSettings.getAssignedClientID(),
                "test/MQTT5_Binding_Java_" + testUUID);
            assertEquals(
                "Negotiated Settings session expiry interval does not match sent session expiry interval",
                events.connectSuccessSettings.getSessionExpiryInterval(),
                0L);
            assertEquals(
                "Negotiated Settings keep alive result does not match sent keep alive",
                events.connectSuccessSettings.getServerKeepAlive(),
                360);

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * ============================================================
     * Operation Tests
     * ============================================================
     */

    /* Sub-UnSub happy path */
    @Test
    public void Op_UC1() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            PublishEvents_Futured publishEvents = new PublishEvents_Futured();
            builder.withPublishEvents(publishEvents);

            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
            publishPacketBuilder.withTopic(testTopic);
            publishPacketBuilder.withPayload("Hello World".getBytes());
            publishPacketBuilder.withQOS(QOS.AT_LEAST_ONCE);

            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
            subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE);

            UnsubscribePacketBuilder unsubscribePacketBuilder = new UnsubscribePacketBuilder();
            unsubscribePacketBuilder.withSubscription(testTopic);

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            client.subscribe(subscribePacketBuilder.build()).get(60, TimeUnit.SECONDS);

            client.publish(publishPacketBuilder.build()).get(60, TimeUnit.SECONDS);
            publishEvents.publishReceivedFuture.get(60, TimeUnit.SECONDS);

            publishEvents.publishReceivedFuture = new CompletableFuture<>();
            publishEvents.publishPacket = null;
            client.unsubscribe(unsubscribePacketBuilder.build()).get(60, TimeUnit.SECONDS);
            client.publish(publishPacketBuilder.build()).get(60, TimeUnit.SECONDS);

            assertEquals(
                "Publish after unsubscribe still arrived!",
                publishEvents.publishPacket,
                null);

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Sub-UnSub happy path */
    @Test
    public void Op_UC2() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();
            PublishPacketBuilder willPacket = new PublishPacketBuilder();
            willPacket.withTopic(testTopic);
            willPacket.withQOS(QOS.AT_LEAST_ONCE);
            willPacket.withPayload("Hello World".getBytes());
            connectOptions.withWill(willPacket.build());
            connectOptions.withWillDelayIntervalSeconds(0L);
            builder.withConnectOptions(connectOptions.build());

            Mqtt5Client clientOne = new Mqtt5Client(builder.build());

            Mqtt5ClientOptionsBuilder builderTwo = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured eventsTwo = new LifecycleEvents_Futured();
            builderTwo.withLifecycleEvents(eventsTwo);
            PublishEvents_Futured publishEvents = new PublishEvents_Futured();
            builderTwo.withPublishEvents(publishEvents);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builderTwo.withTlsContext(tlsContext);
            }

            SubscribePacketBuilder subscribeOptions = new SubscribePacketBuilder();
            subscribeOptions.withSubscription(testTopic, QOS.AT_LEAST_ONCE);
            Mqtt5Client clientTwo = new Mqtt5Client(builderTwo.build());

            clientOne.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            clientTwo.start();
            eventsTwo.connectedFuture.get(60, TimeUnit.SECONDS);

            clientTwo.subscribe(subscribeOptions.build()).get(60, TimeUnit.SECONDS);

            DisconnectPacketBuilder disconnectOptions = new DisconnectPacketBuilder();
            disconnectOptions.withReasonCode(DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE);
            clientOne.stop(disconnectOptions.build());

            // Did we get a publish message?
            publishEvents.publishReceivedFuture.get(60, TimeUnit.SECONDS);
            assertTrue(publishEvents.publishPacket != null);

            clientTwo.stop(new DisconnectPacketBuilder().build());
            clientOne.close();
            clientTwo.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Binary Publish Test */
    @Test
    public void Op_UC3() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            PublishEvents_Futured publishEvents = new PublishEvents_Futured();
            builder.withPublishEvents(publishEvents);

            // Make random binary
            byte[] randomBytes = new byte[256];
            Random random = new Random();
            random.nextBytes(randomBytes);

            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
            publishPacketBuilder.withTopic(testTopic).withPayload(randomBytes).withQOS(QOS.AT_LEAST_ONCE);

            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
            subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE);

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            client.subscribe(subscribePacketBuilder.build()).get(60, TimeUnit.SECONDS);

            client.publish(publishPacketBuilder.build()).get(60, TimeUnit.SECONDS);
            publishEvents.publishReceivedFuture.get(60, TimeUnit.SECONDS);

            assertTrue(java.util.Arrays.equals(publishEvents.publishPacket.getPayload(), randomBytes));

            client.stop(new DisconnectPacketBuilder().build());
            events.stopFuture.get(60, TimeUnit.SECONDS);
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * ============================================================
     * Error Operation Tests
     * ============================================================
     */

    /* Null Publish Test */
    @Test
    public void ErrorOp_UC1() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            try {
                System.out.println("NOTE: Exception due to null publish packet may be printed below!");
                client.publish(null).get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                didExceptionOccur = true;
            }

            if (didExceptionOccur == false) {
                fail("Null publish packet did not cause exception with error!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Publish with empty builder test */
    @Test
    public void ErrorOp_UC1_ALT() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            try {
                System.out.println("NOTE: Exception due to empty publish packet may be printed below!");
                client.publish(new PublishPacketBuilder().build()).get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                didExceptionOccur = true;
            }

            if (didExceptionOccur == false) {
                fail("Empty publish packet did not cause exception with error!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Null Subscribe Test */
    @Test
    public void ErrorOp_UC2() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            try {
                System.out.println("NOTE: Exception due to null subscribe packet may be printed below!");
                client.subscribe(null).get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                didExceptionOccur = true;
            }

            if (didExceptionOccur == false) {
                fail("Null subscribe packet did not cause exception with error!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Empty Subscribe Test */
    @Test
    public void ErrorOp_UC2_ALT() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            try {
                System.out.println("NOTE: Exception due to empty subscribe packet may be printed below!");
                client.subscribe(new SubscribePacketBuilder().build()).get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                didExceptionOccur = true;
            }

            if (didExceptionOccur == false) {
                fail("Empty subscribe packet did not cause exception with error!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Null Unsubscribe Test */
    @Test
    public void ErrorOp_UC3() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            try {
                System.out.println("NOTE: Exception due to null unsubscribe packet may be printed below!");
                client.unsubscribe(null).get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                didExceptionOccur = true;
            }

            if (didExceptionOccur == false) {
                fail("Null unsubscribe packet did not cause exception with error!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Empty Unsubscribe Test */
    @Test
    public void ErrorOp_UC3_ALT() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            Mqtt5Client client = new Mqtt5Client(builder.build());

            client.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);

            try {
                System.out.println("NOTE: Exception due to empty unsubscribe packet may be printed below!");
                client.unsubscribe(new UnsubscribePacketBuilder().build()).get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                didExceptionOccur = true;
            }

            if (didExceptionOccur == false) {
                fail("Empty unsubscribe packet did not cause exception with error!");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* TODO: Adjust and enable this test using IoT Core. */
    /* Unsupported Connect packet data sent */
    /*
    @Test
    public void ErrorOp_UC4() {
        Assume.assumeTrue(mqtt5DirectMqttHost != null);
        Assume.assumeTrue(mqtt5DirectMqttPort != null);
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(mqtt5DirectMqttHost, mqtt5DirectMqttPort);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();
            String clientIDString = "";
            for (int i = 0; i < 130; i++) {
                clientIDString += "a";
            }
            connectOptions.withClientId(clientIDString);
            builder.withConnectOptions(connectOptions.build());

            Mqtt5Client client = new Mqtt5Client(builder.build());

            try {
                client.start();
                events.connectedFuture.get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                didExceptionOccur = true;
            }

            if (didExceptionOccur == false) {
                fail("Was able to connect with Client ID longer than 128 characters (AWS_IOT_CORE_MAXIMUM_CLIENT_ID_LENGTH)");
            }

            client.stop(new DisconnectPacketBuilder().build());
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
    */

    /**
     * ============================================================
     * QoS1 Tests
     * ============================================================
     */

    /* Happy path. No drop in connection, no retry, no reconnect */
    @Test
    public void QoS1_UC1() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        int messageCount = 10;
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builder.withTlsContext(tlsContext);
            }

            Mqtt5Client publisher = new Mqtt5Client(builder.build());

            Mqtt5ClientOptionsBuilder builderTwo = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());
            LifecycleEvents_Futured eventsTwo = new LifecycleEvents_Futured();
            builderTwo.withLifecycleEvents(eventsTwo);
            PublishEvents_Futured_Counted publishEvents = new PublishEvents_Futured_Counted();
            publishEvents.desiredPublishCount = messageCount;
            builderTwo.withPublishEvents(publishEvents);

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                builderTwo.withTlsContext(tlsContext);
            }

            Mqtt5Client subscriber = new Mqtt5Client(builderTwo.build());

            publisher.start();
            events.connectedFuture.get(60, TimeUnit.SECONDS);
            subscriber.start();
            eventsTwo.connectedFuture.get(60, TimeUnit.SECONDS);

            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
            subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE);
            subscriber.subscribe(subscribePacketBuilder.build()).get(60, TimeUnit.SECONDS);

            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
            publishPacketBuilder.withTopic(testTopic);
            publishPacketBuilder.withPayload("Hello World".getBytes());
            publishPacketBuilder.withQOS(QOS.AT_LEAST_ONCE);

            for (int i = 0; i < messageCount; i++) {
                publisher.publish(publishPacketBuilder.build()).get(60, TimeUnit.SECONDS);
            }

            // Did we get all the messages?
            publishEvents.publishReceivedFuture.get(60, TimeUnit.SECONDS);

            subscriber.stop(new DisconnectPacketBuilder().build());
            publisher.stop(new DisconnectPacketBuilder().build());
            subscriber.close();
            publisher.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * ============================================================
     * Retain Tests
     * ============================================================
     */

     /* Happy path. No drop in connection, no retry, no reconnect */
    @Test
    public void Retain_UC1() {
        Assume.assumeTrue(checkMinimumDirectHostAndPort());
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/retained_topic/MQTT5_Binding_Java_" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder clientBuilder = new Mqtt5ClientOptionsBuilder(getMinimumDirectHost(), getMinimumDirectPort());

            // Only needed for IoT Core
            if (getMinimumDirectCert() == mqtt5IoTCoreMqttCertificateFile && mqtt5IoTCoreMqttCertificateFile != null) {
                Assume.assumeTrue(getMinimumDirectCert() != null);
                Assume.assumeTrue(getMinimumDirectKey() != null);
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(getMinimumDirectCert(), getMinimumDirectKey());
                TlsContext tlsContext = new TlsContext(tlsOptions);
                clientBuilder.withTlsContext(tlsContext);
            }

            LifecycleEvents_Futured publisherEvents = new LifecycleEvents_Futured();
            clientBuilder.withLifecycleEvents(publisherEvents);
            Mqtt5Client publisher = new Mqtt5Client(clientBuilder.build());

            LifecycleEvents_Futured successSubscriberEvents = new LifecycleEvents_Futured();
            PublishEvents_Futured successSubscriberPublishEvents = new PublishEvents_Futured();
            clientBuilder.withLifecycleEvents(successSubscriberEvents);
            clientBuilder.withPublishEvents(successSubscriberPublishEvents);
            Mqtt5Client successSubscriber = new Mqtt5Client(clientBuilder.build());

            LifecycleEvents_Futured unsuccessfulSubscriberEvents = new LifecycleEvents_Futured();
            PublishEvents_Futured unsuccessfulSubscriberPublishEvents = new PublishEvents_Futured();
            clientBuilder.withLifecycleEvents(unsuccessfulSubscriberEvents);
            clientBuilder.withPublishEvents(unsuccessfulSubscriberPublishEvents);
            Mqtt5Client unsuccessfulSubscriber = new Mqtt5Client(clientBuilder.build());

            // Connect and publish a retained message
            publisher.start();
            publisherEvents.connectedFuture.get(60, TimeUnit.SECONDS);
            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
            publishPacketBuilder.withTopic(testTopic)
                .withPayload("Hello World".getBytes())
                .withQOS(QOS.AT_LEAST_ONCE)
                .withRetain(true);
            publisher.publish(publishPacketBuilder.build()).get(60, TimeUnit.SECONDS);

            // Setup for clearing the retained message
            publishPacketBuilder.withPayload(null);

            // Connect the successful subscriber
            successSubscriber.start();
            try {
                successSubscriberEvents.connectedFuture.get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                // Clear the retained message
                publisher.publish(publishPacketBuilder.build()).get(60, TimeUnit.SECONDS);
                fail("Success subscriber could not connect!");
            }

            // Subscribe and verify the retained message
            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
            subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE, false, true, RetainHandlingType.SEND_ON_SUBSCRIBE);
            try {
                successSubscriber.subscribe(subscribePacketBuilder.build()).get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                // Clear the retained message
                publisher.publish(publishPacketBuilder.build()).get(60, TimeUnit.SECONDS);
                fail("Success subscriber could not subscribe!");
            }
            try {
                successSubscriberPublishEvents.publishReceivedFuture.get(60, TimeUnit.SECONDS);
            } catch (Exception ex) {
                // Clear the retained message
                publisher.publish(publishPacketBuilder.build()).get(60, TimeUnit.SECONDS);
                fail("Success subscriber did not get retained message!");
            }

            // Clear the retained message
            publisher.publish(publishPacketBuilder.build()).get(60, TimeUnit.SECONDS);

            // Wait 5 seconds to give the server time to clear everything out
            Thread.sleep(5000);

            // Connect the unsuccessful subscriber
            unsuccessfulSubscriber.start();
            unsuccessfulSubscriberEvents.connectedFuture.get(60, TimeUnit.SECONDS);
            unsuccessfulSubscriber.subscribe(subscribePacketBuilder.build()).get(60, TimeUnit.SECONDS);
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
            DisconnectPacketBuilder disconnectPacketBuilder = new DisconnectPacketBuilder();
            publisher.stop(disconnectPacketBuilder.build());
            publisherEvents.stopFuture.get(60, TimeUnit.SECONDS);
            successSubscriber.stop(disconnectPacketBuilder.build());
            successSubscriberEvents.stopFuture.get(60, TimeUnit.SECONDS);
            unsuccessfulSubscriber.stop(disconnectPacketBuilder.build());
            unsuccessfulSubscriberEvents.stopFuture.get(60, TimeUnit.SECONDS);

            // Close all clients
            publisher.close();;
            successSubscriber.close();
            unsuccessfulSubscriber.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }


}
