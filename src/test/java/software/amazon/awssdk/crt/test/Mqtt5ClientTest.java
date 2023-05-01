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
import software.amazon.awssdk.crt.mqtt5.Mqtt5ListenerOptions;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ListenerOptions.ListenerPublishEvents;
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

    // MQTT5 Codebuild/Direct connections data
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_HOST = System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_HOST");
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_PORT = System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_PORT");
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST = System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST");
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT = System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT");
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST = System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST");
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT = System.getenv("AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT");
    // MQTT5 Codebuild/Websocket connections data
    static final String AWS_TEST_MQTT5_WS_MQTT_HOST = System.getenv("AWS_TEST_MQTT5_WS_MQTT_HOST");
    static final String AWS_TEST_MQTT5_WS_MQTT_PORT = System.getenv("AWS_TEST_MQTT5_WS_MQTT_PORT");
    static final String AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST = System.getenv("AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST");
    static final String AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT = System.getenv("AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT");
    static final String AWS_TEST_MQTT5_WS_MQTT_TLS_HOST = System.getenv("AWS_TEST_MQTT5_WS_MQTT_TLS_HOST");
    static final String AWS_TEST_MQTT5_WS_MQTT_TLS_PORT = System.getenv("AWS_TEST_MQTT5_WS_MQTT_TLS_PORT");
    // MQTT5 Codebuild misc connections data
    static final String AWS_TEST_MQTT5_BASIC_AUTH_USERNAME = System.getenv("AWS_TEST_MQTT5_BASIC_AUTH_USERNAME");
    static final String AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD = System.getenv("AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD");
    static final String AWS_TEST_MQTT5_CERTIFICATE_FILE = System.getenv("AWS_TEST_MQTT5_CERTIFICATE_FILE");
    static final String AWS_TEST_MQTT5_KEY_FILE = System.getenv("AWS_TEST_MQTT5_KEY_FILE");
    // MQTT5 Proxy
    static final String AWS_TEST_MQTT5_PROXY_HOST = System.getenv("AWS_TEST_MQTT5_PROXY_HOST");
    static final String AWS_TEST_MQTT5_PROXY_PORT = System.getenv("AWS_TEST_MQTT5_PROXY_PORT");
    // MQTT5 Endpoint/Host credentials
    static final String AWS_TEST_MQTT5_IOT_CORE_HOST = System.getenv("AWS_TEST_MQTT5_IOT_CORE_HOST");
    static final String AWS_TEST_MQTT5_IOT_CORE_REGION = System.getenv("AWS_TEST_MQTT5_IOT_CORE_REGION");
    static final String AWS_TEST_MQTT5_IOT_CORE_RSA_CERT = System.getenv("AWS_TEST_MQTT5_IOT_CORE_RSA_CERT");
    static final String AWS_TEST_MQTT5_IOT_CORE_RSA_KEY = System.getenv("AWS_TEST_MQTT5_IOT_CORE_RSA_KEY");
    // MQTT5 Static credential related
    static final String AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY = System.getenv("AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY");
    static final String AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY = System.getenv("AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY");
    static final String AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN = System.getenv("AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN");
    // MQTT5 Cognito
    static final String AWS_TEST_MQTT5_COGNITO_ENDPOINT = System.getenv("AWS_TEST_MQTT5_COGNITO_ENDPOINT");
    static final String AWS_TEST_MQTT5_COGNITO_IDENTITY = System.getenv("AWS_TEST_MQTT5_COGNITO_IDENTITY");
    // MQTT5 Keystore
    static final String AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FORMAT = System.getenv("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FORMAT");
    static final String AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FILE = System.getenv("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FILE");
    static final String AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_PASSWORD = System.getenv("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_PASSWORD");
    static final String AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_ALIAS = System.getenv("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_ALIAS");
    static final String AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_PASSWORD = System.getenv("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_PASSWORD");
    // MQTT5 PKCS12
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY = System.getenv("AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY_PASSWORD = System.getenv("AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY_PASSWORD");
    // MQTT5 PKCS11
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_LIB = System.getenv("AWS_TEST_MQTT5_IOT_CORE_PKCS11_LIB");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_TOKEN_LABEL = System.getenv("AWS_TEST_MQTT5_IOT_CORE_PKCS11_TOKEN_LABEL");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_PIN = System.getenv("AWS_TEST_MQTT5_IOT_CORE_PKCS11_PIN");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_PKEY_LABEL = System.getenv("AWS_TEST_MQTT5_IOT_CORE_PKCS11_PKEY_LABEL");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_CERT_FILE = System.getenv("AWS_TEST_MQTT5_IOT_CORE_PKCS11_CERT_FILE");
    // MQTT5 X509
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_CERT = System.getenv("AWS_TEST_MQTT5_IOT_CORE_X509_CERT");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_KEY = System.getenv("AWS_TEST_MQTT5_IOT_CORE_X509_KEY");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT = System.getenv("AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS = System.getenv("AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME = System.getenv("AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME");
    // MQTT5 Windows Cert Store
    static final String AWS_TEST_MQTT5_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS = System.getenv("AWS_TEST_MQTT5_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS");
    static final String AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE = System.getenv("AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE");

    private int OPERATION_TIMEOUT_TIME = 30;

    public Mqtt5ClientTest() {
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
            connectedFuture.completeExceptionally(new Exception("Could not connect! Error name: " + CRT.awsErrorName(connectFailureCode)));
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
        List<PublishPacket> publishPacketsReceived = new ArrayList<PublishPacket>();

        @Override
        public void onMessageReceived(Mqtt5Client client, PublishReturn result) {
            currentPublishCount += 1;
            if (currentPublishCount == desiredPublishCount) {
                publishReceivedFuture.complete(null);
            } else if (currentPublishCount > desiredPublishCount) {
                publishReceivedFuture.completeExceptionally(new Throwable("Too many publish packets received"));
            }

            if (publishPacketsReceived.contains(result)) {
                publishReceivedFuture.completeExceptionally(new Throwable("Duplicate publish packet received!"));
            }
            publishPacketsReceived.add(result.getPublishPacket());
        }
    }


    static final class ListenerPublishEvents_Futured_Counted implements ListenerPublishEvents {
        CompletableFuture<Void> PublishProceedFuture = new CompletableFuture<>();
        CompletableFuture<Void> PublishSkippedFuture = new CompletableFuture<>();
        PublishPacket publishPacket = null;
        String listenerTopic = "";
        int proceedPublishCount = 0;
        int skippedPublishCount = 0;

        public ListenerPublishEvents_Futured_Counted(String topic)
        {
            super();
            listenerTopic = topic;
        }

        @Override
        public boolean onMessageReceived(Mqtt5Client client, PublishReturn result) {
            publishPacket = result.getPublishPacket();
            /* If we dont set the listener topic, always proceed the message. */
            if(listenerTopic.isEmpty() || listenerTopic.equals(publishPacket.getTopic()))
            {
                ++proceedPublishCount;
                PublishProceedFuture.complete(null);
                return true;
            }
            ++skippedPublishCount;
            PublishSkippedFuture.complete(null);
            return false;
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
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_PORT != null);
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
    }

    /* Maximum creation and cleanup */
    @Test
    public void New_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_PORT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_PROXY_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_PROXY_PORT != null);
        try {

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
                SocketOptions socketOptions = new SocketOptions();
            ) {

                PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder();
                willPacketBuilder.withQOS(QOS.AT_LEAST_ONCE).withPayload("Hello World".getBytes()).withTopic("test/topic");

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
    }

    /* Minimal memory check */
    @Test
    public void New_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_PORT != null);
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
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_PORT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_PROXY_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_PROXY_PORT != null);

        try {
            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
                SocketOptions socketOptions = new SocketOptions();
            ) {
                PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder();
                willPacketBuilder.withQOS(QOS.AT_LEAST_ONCE).withPayload("Hello World".getBytes()).withTopic("test/topic");

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

    /* Happy path. Direct connection with minimal configuration */
    @Test
    public void ConnDC_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_PORT != null);
        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                AWS_TEST_MQTT5_DIRECT_MQTT_HOST,
                Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_PORT));
            builder.withLifecycleEvents(events);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
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
    public void ConnDC_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD != null);

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
                DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                client.stop(disconnect.build());
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Direct connection with TLS */
    @Test
    public void ConnDC_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_CERTIFICATE_FILE != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_KEY_FILE != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient()) {
                tlsOptions.withVerifyPeer(false);
                try (TlsContext tlsContext = new TlsContext(tlsOptions)) {
                    Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                        AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST, Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT));
                    builder.withLifecycleEvents(events);
                    builder.withTlsContext(tlsContext);

                    try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                        client.start();
                        events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                        DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                        client.stop(disconnect.build());
                    }
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Direct connection with mTLS */
    @Test
    public void ConnDC_UC4() {
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                    AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
                TlsContext tlsContext = new TlsContext(tlsOptions);
            ) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
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

    /* Direct connection with HttpProxyOptions */
    @Test
    public void ConnDC_UC5() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_CERTIFICATE_FILE != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_KEY_FILE != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_PROXY_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_PROXY_PORT != null);

        try {

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            ) {
                LifecycleEvents_Futured events = new LifecycleEvents_Futured();
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(
                    AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST, Long.parseLong(AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT));
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);

                HttpProxyOptions proxyOptions = new HttpProxyOptions();
                proxyOptions.setHost(AWS_TEST_MQTT5_PROXY_HOST);
                proxyOptions.setPort(Integer.parseInt(AWS_TEST_MQTT5_PROXY_PORT));
                proxyOptions.setConnectionType(HttpProxyConnectionType.Tunneling);

                try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient()) {
                    tlsOptions.withVerifyPeer(false);
                    try (TlsContext tlsContext = new TlsContext(tlsOptions)) {
                        builder.withTlsContext(tlsContext);
                        builder.withHttpProxyOptions(proxyOptions);

                        try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                            client.start();
                            events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                            client.stop(disconnect.build());
                        }
                    }
                }
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Maximum options set connection test */
    @Test
    public void ConnDC_UC6() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
                SocketOptions socketOptions = new SocketOptions();
            ) {
                PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder();
                willPacketBuilder.withQOS(QOS.AT_LEAST_ONCE).withPayload("Hello World".getBytes()).withTopic("test/topic");

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
            }

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
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_PORT != null);
        try {

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            ) {

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
                    DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                    client.stop(disconnect.build());
                }
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Websocket connection with basic authentication */
    @Test
    public void ConnWS_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            ) {
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
                    DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                    client.stop(disconnect.build());
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Websocket connection with TLS */
    @Test
    public void ConnWS_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_TLS_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_TLS_PORT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_CERTIFICATE_FILE != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_KEY_FILE != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            ) {

                try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient()) {
                    tlsOptions.withVerifyPeer(false);
                    try (TlsContext tlsContext = new TlsContext(tlsOptions)) {
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
                            DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                            client.stop(disconnect.build());
                        }
                    }
                }
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Websocket connection with HttpProxyOptions */
    @Test
    public void ConnWS_UC5() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_TLS_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_TLS_PORT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_CERTIFICATE_FILE != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_KEY_FILE != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_PROXY_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_PROXY_PORT != null);

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

    /* Websocket connection with all options set */
    @Test
    public void ConnWS_UC6() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_USERNAME != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
                SocketOptions socketOptions = new SocketOptions();
            ) {
                PublishPacketBuilder willPacketBuilder = new PublishPacketBuilder();
                willPacketBuilder.withQOS(QOS.AT_LEAST_ONCE).withPayload("Hello World".getBytes()).withTopic("test/topic");

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
            }

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
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_PORT != null);
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
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_HOST != null);
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
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_PORT != null);
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
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_HOST != null);
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
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_DIRECT_MQTT_PORT != null);
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
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_WS_MQTT_PORT != null);
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

                    DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                    client.stop(disconnect.build());
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
            connectedFuture.completeExceptionally(new Exception(
                "[" + client_name + "] Could not connect! Error code is: " + onConnectionFailureReturn.getErrorCode()
            ));
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

    /* Double Client ID failure test */
    @Test
    public void ConnNegativeID_UC7() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();

        try {
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

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client clientOne = new Mqtt5Client(builder.build());
                Mqtt5Client clientTwo = new Mqtt5Client(builderTwo.build());) {
                clientOne.start();
                eventsOne.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                Thread.sleep(1100); // Sleep for 1.1 seconds to not hit IoT Core limits

                clientTwo.start();
                eventsTwo.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Make sure a disconnection for client 1 happened
                eventsOne.disconnectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Stop the clients from disconnecting each other. If we do not do this, then the clients will
                // attempt to reconnect endlessly, making a never ending loop.
                clientOne.stop(null);
                clientTwo.stop(null);
            }
            if (tlsContext != null) {
                tlsContext.close();
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Double Client ID disconnect and then reconnect test */
    @Test
    public void ConnNegativeID_UC7_ALT() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();
        try {
            LifecycleEvents_DoubleClientID eventsOne = new LifecycleEvents_DoubleClientID();
            LifecycleEvents_DoubleClientID eventsTwo = new LifecycleEvents_DoubleClientID();
            eventsOne.client_name = "client_one";
            eventsTwo.client_name = "client_two";
            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder().withClientId("test/MQTT5_Java_Double_ClientIDReconnect_" + testUUID);

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(eventsOne);
            builder.withConnectOptions(connectOptions.build());
            builder.withConnackTimeoutMs(30000l); // 30 seconds

            Mqtt5ClientOptionsBuilder builderTwo = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builderTwo.withLifecycleEvents(eventsTwo);
            builderTwo.withConnectOptions(connectOptions.build());
            builderTwo.withConnackTimeoutMs(30000l); // 30 seconds

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);
            builderTwo.withTlsContext(tlsContext);

            try (
                Mqtt5Client clientOne = new Mqtt5Client(builder.build());
                Mqtt5Client clientTwo = new Mqtt5Client(builderTwo.build());
            ) {
                clientOne.start();
                eventsOne.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                Thread.sleep(1100); // Sleep for 1.1 seconds to not hit IoT Core limits

                clientTwo.start();
                eventsTwo.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Make sure the first client was disconnected
                eventsOne.disconnectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                // Disconnect the second client so the first can reconnect
                clientTwo.stop(null);
                // Confirm the second client has stopped
                eventsTwo.stoppedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Wait until the first client has reconnected
                eventsOne.connectedFuture = new CompletableFuture<>();
                eventsOne.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                assertTrue(clientOne.getIsConnected() == true);
                clientOne.stop(null);
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

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
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean clientCreationFailed = false;

        try {
            Mqtt5Client client = null;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();

            connectOptions.withKeepAliveIntervalSeconds(-100L);
            builder.withConnectOptions(connectOptions.build());

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            clientCreationFailed = false;
            try {
                client = new Mqtt5Client(builder.build());
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
            try {
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
                client = new Mqtt5Client(builder.build());
            } catch (Exception ex) {
                clientCreationFailed = true;
            }
            if (clientCreationFailed == false) {
                fail("Client creation did not fail with negative willDelayIntervalSeconds");
            }
            connectOptions.withWillDelayIntervalSeconds(100L);

            // Make sure everything is closed
            if (client != null) {
                client.close();
            }
            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Overflow Connect Packet Properties */
    @Test
    public void NewNegative_UC1_ALT() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean clientCreationFailed = false;

        try {
            Mqtt5Client client = null;
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();

            connectOptions.withKeepAliveIntervalSeconds(2147483647L);
            builder.withConnectOptions(connectOptions.build());

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            clientCreationFailed = false;
            try {
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
                client = new Mqtt5Client(builder.build());
            } catch (Exception ex) {
                clientCreationFailed = true;
            }
            if (clientCreationFailed == false) {
                fail("Client creation did not fail with overflow MaximumPacketSizeBytes");
            }
            connectOptions.withMaximumPacketSizeBytes(100L);

            // WillDelayIntervalSeconds is an unsigned 64 bit Long, so it cannot overflow it from Java

            // Make sure everything is closed
            if (client != null) {
                client.close();
            }
            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Negative Disconnect Packet Properties */
    @Test
    public void NewNegative_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean clientDisconnectFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                DisconnectPacketBuilder disconnectBuilder = new DisconnectPacketBuilder();
                disconnectBuilder.withSessionExpiryIntervalSeconds(-100L);
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

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Overflow Disconnect Packet Properties */
    @Test
    public void NewNegative_UC2_ALT() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean clientDisconnectFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                DisconnectPacketBuilder disconnectBuilder = new DisconnectPacketBuilder();
                disconnectBuilder.withSessionExpiryIntervalSeconds(9223372036854775807L);
                try {
                    client.stop(disconnectBuilder.build());
                } catch (Exception ex) {
                    clientDisconnectFailed = true;
                }

                if (clientDisconnectFailed == false) {
                    fail("Client disconnect did not fail!");
                }

                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Negative Publish Packet Properties */
    @Test
    public void NewNegative_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean clientPublishFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                PublishPacketBuilder publishBuilder = new PublishPacketBuilder();
                publishBuilder.withPayload("Hello World".getBytes()).withTopic("test/topic");
                publishBuilder.withMessageExpiryIntervalSeconds(-100L);
                try {
                    CompletableFuture<PublishResult> future = client.publish(publishBuilder.build());
                    future.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    clientPublishFailed = true;
                }

                if (clientPublishFailed == false) {
                    fail("Client publish did not fail!");
                }

                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Overflow Publish Packet Properties */
    @Test
    public void NewNegative_UC3_ALT() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean clientPublishFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                PublishPacketBuilder publishBuilder = new PublishPacketBuilder();
                publishBuilder.withPayload("Hello World".getBytes()).withTopic("test/topic").withQOS(QOS.AT_LEAST_ONCE);
                publishBuilder.withMessageExpiryIntervalSeconds(9223372036854775807L);
                try {
                    CompletableFuture<PublishResult> future = client.publish(publishBuilder.build());
                    future.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    clientPublishFailed = true;
                }

                if (clientPublishFailed == false) {
                    fail("Client publish did not fail!");
                }

                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Negative Subscribe Packet Properties */
    @Test
    public void NewNegative_UC4() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean clientSubscribeFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                SubscribePacketBuilder subscribeBuilder = new SubscribePacketBuilder();
                subscribeBuilder.withSubscription("test/topic", QOS.AT_LEAST_ONCE);
                subscribeBuilder.withSubscriptionIdentifier(-100L);
                try {
                    CompletableFuture<SubAckPacket> future = client.subscribe(subscribeBuilder.build());
                    future.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    clientSubscribeFailed = true;
                }

                if (clientSubscribeFailed == false) {
                    fail("Client subscribe did not fail!");
                }

                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Overflow Subscribe Packet Properties */
    @Test
    public void NewNegative_UC4_ALT() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean clientSubscribeFailed = false;

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                SubscribePacketBuilder subscribeBuilder = new SubscribePacketBuilder();
                subscribeBuilder.withSubscription("test/topic", QOS.AT_LEAST_ONCE);
                subscribeBuilder.withSubscriptionIdentifier(9223372036854775807L);
                try {
                    CompletableFuture<SubAckPacket> future = client.subscribe(subscribeBuilder.build());
                    future.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    clientSubscribeFailed = true;
                }

                if (clientSubscribeFailed == false) {
                    fail("Client subscribe did not fail!");
                }

                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

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
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
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
                //     events.connectSuccessSettings.getSessionExpiryInterval(),
                //     600000L);

                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Maximum success test */
    @Test
    public void Negotiated_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
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
                    events.connectSuccessSettings.getSessionExpiryInterval(),
                    0L);
                assertEquals(
                    "Negotiated Settings keep alive result does not match sent keep alive",
                    events.connectSuccessSettings.getServerKeepAlive(),
                    360);
                assertEquals(
                        "Negotiated Settings rejoined session does not match expected value",
                        false,
                        events.connectSuccessSettings.getRejoinedSession());

                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Rejoin always session resumption test */
    @Test
    public void Negotiated_Rejoin_Always() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

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
                        events.connectSuccessSettings.getSessionExpiryInterval(),
                        3600L);
                assertEquals(
                        "Negotiated Settings keep alive result does not match sent keep alive",
                        events.connectSuccessSettings.getServerKeepAlive(),
                        360);
                assertEquals(
                        "Negotiated Settings rejoined session does not match expected value",
                        false,
                        events.connectSuccessSettings.getRejoinedSession());

                client.stop(new DisconnectPacketBuilder().build());
            }

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

                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

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
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

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

            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
            publishPacketBuilder.withTopic(testTopic);
            publishPacketBuilder.withPayload("Hello World".getBytes());
            publishPacketBuilder.withQOS(QOS.AT_LEAST_ONCE);

            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
            subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE);

            UnsubscribePacketBuilder unsubscribePacketBuilder = new UnsubscribePacketBuilder();
            unsubscribePacketBuilder.withSubscription(testTopic);

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

                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Sub-UnSub happy path */
    @Test
    public void Op_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            ConnectPacketBuilder connectOptions = new ConnectPacketBuilder();
            PublishPacketBuilder willPacket = new PublishPacketBuilder();
            willPacket.withTopic(testTopic);
            willPacket.withQOS(QOS.AT_LEAST_ONCE);
            willPacket.withPayload("Hello World".getBytes());
            connectOptions.withWill(willPacket.build());
            connectOptions.withWillDelayIntervalSeconds(0L);
            builder.withConnectOptions(connectOptions.build());

            Mqtt5ClientOptionsBuilder builderTwo = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured eventsTwo = new LifecycleEvents_Futured();
            builderTwo.withLifecycleEvents(eventsTwo);
            PublishEvents_Futured publishEvents = new PublishEvents_Futured();
            builderTwo.withPublishEvents(publishEvents);

            TlsContextOptions tlsOptionsTwo = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContextTwo = new TlsContext(tlsOptionsTwo);
            tlsOptionsTwo.close();
            builderTwo.withTlsContext(tlsContextTwo);

            SubscribePacketBuilder subscribeOptions = new SubscribePacketBuilder();
            subscribeOptions.withSubscription(testTopic, QOS.AT_LEAST_ONCE);

            try (
                Mqtt5Client clientOne = new Mqtt5Client(builder.build());
                Mqtt5Client clientTwo = new Mqtt5Client(builderTwo.build());
            ) {
                clientOne.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                clientTwo.start();
                eventsTwo.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                clientTwo.subscribe(subscribeOptions.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                clientOne.stop(null);

                // Did we get a publish message?
                publishEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                assertTrue(publishEvents.publishPacket != null);

                clientTwo.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
                tlsContextTwo.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Binary Publish Test */
    @Test
    public void Op_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

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

            // Make random binary
            byte[] randomBytes = new byte[256];
            Random random = new Random();
            random.nextBytes(randomBytes);

            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
            publishPacketBuilder.withTopic(testTopic).withPayload(randomBytes).withQOS(QOS.AT_LEAST_ONCE);

            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
            subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                client.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                client.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                publishEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                assertTrue(java.util.Arrays.equals(publishEvents.publishPacket.getPayload(), randomBytes));

                client.stop(new DisconnectPacketBuilder().build());
                events.stopFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Will test */
    @Test
    public void Op_UC4() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

        try {
            // Publisher
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            // Subscriber
            Mqtt5ClientOptionsBuilder builderTwo = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured eventsTwo = new LifecycleEvents_Futured();
            builderTwo.withLifecycleEvents(eventsTwo);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);
            builderTwo.withTlsContext(tlsContext);

            PublishEvents_Futured publishEvents = new PublishEvents_Futured();
            builderTwo.withPublishEvents(publishEvents);
            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
            subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE);

            ConnectPacketBuilder connectPacketBuilder = new ConnectPacketBuilder();
            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
            publishPacketBuilder.withTopic(testTopic);
            publishPacketBuilder.withPayload("Hello World".getBytes());
            publishPacketBuilder.withQOS(QOS.AT_LEAST_ONCE);
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

                publisher.stop(disconnectPacketBuilder.build());

                publishEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                subscriber.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

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
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    client.publish(null).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    didExceptionOccur = true;
                }

                if (didExceptionOccur == false) {
                    fail("Null publish packet did not cause exception with error!");
                }
                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Publish with empty builder test */
    @Test
    public void ErrorOp_UC1_ALT() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    client.publish(new PublishPacketBuilder().build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    didExceptionOccur = true;
                }

                if (didExceptionOccur == false) {
                    fail("Empty publish packet did not cause exception with error!");
                }
                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Null Subscribe Test */
    @Test
    public void ErrorOp_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    client.subscribe(null).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    didExceptionOccur = true;
                }

                if (didExceptionOccur == false) {
                    fail("Null subscribe packet did not cause exception with error!");
                }
                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Empty Subscribe Test */
    @Test
    public void ErrorOp_UC2_ALT() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    client.subscribe(new SubscribePacketBuilder().build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    didExceptionOccur = true;
                }

                if (didExceptionOccur == false) {
                    fail("Empty subscribe packet did not cause exception with error!");
                }
                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Null Unsubscribe Test */
    @Test
    public void ErrorOp_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    client.unsubscribe(null).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    didExceptionOccur = true;
                }

                if (didExceptionOccur == false) {
                    fail("Null unsubscribe packet did not cause exception with error!");
                }
                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Empty Unsubscribe Test */
    @Test
    public void ErrorOp_UC3_ALT() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    client.unsubscribe(new UnsubscribePacketBuilder().build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    didExceptionOccur = true;
                }

                if (didExceptionOccur == false) {
                    fail("Empty unsubscribe packet did not cause exception with error!");
                }
                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Unsupported Connect packet data sent (IoT Core only) */
    @Test
    public void ErrorOp_UC4() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        boolean didExceptionOccur = false;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
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
                client.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
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

    /* Happy path. No drop in connection, no retry, no reconnect */
    @Test
    public void QoS1_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        int messageCount = 10;
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            Mqtt5ClientOptionsBuilder builderTwo = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured eventsTwo = new LifecycleEvents_Futured();
            builderTwo.withLifecycleEvents(eventsTwo);
            PublishEvents_Futured_Counted publishEvents = new PublishEvents_Futured_Counted();
            publishEvents.desiredPublishCount = messageCount;
            builderTwo.withPublishEvents(publishEvents);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);
            builderTwo.withTlsContext(tlsContext);

            try (
                Mqtt5Client publisher = new Mqtt5Client(builder.build());
                Mqtt5Client subscriber = new Mqtt5Client(builderTwo.build());
            ) {
                publisher.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                subscriber.start();
                eventsTwo.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
                subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE);
                subscriber.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
                publishPacketBuilder.withTopic(testTopic);
                publishPacketBuilder.withPayload("Hello World".getBytes());
                publishPacketBuilder.withQOS(QOS.AT_LEAST_ONCE);

                for (int i = 0; i < messageCount; i++) {
                    publisher.publish(publishPacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                }

                // Did we get all the messages?
                publishEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                subscriber.stop(new DisconnectPacketBuilder().build());
                publisher.stop(new DisconnectPacketBuilder().build());
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

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
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/retained_topic/MQTT5_Binding_Java_" + testUUID;

        try {
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

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            publisherEventsBuilder.withTlsContext(tlsContext);
            successSubscriberBuilder.withTlsContext(tlsContext);
            unsuccessSubscriberBuilder.withTlsContext(tlsContext);

            try (
                Mqtt5Client publisher = new Mqtt5Client(publisherEventsBuilder.build());
                Mqtt5Client successSubscriber = new Mqtt5Client(successSubscriberBuilder.build());
                Mqtt5Client unsuccessfulSubscriber = new Mqtt5Client(unsuccessSubscriberBuilder.build());
            ) {
                // Connect and publish a retained message
                publisher.start();
                publisherEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
                publishPacketBuilder.withTopic(testTopic)
                    .withPayload("Hello World".getBytes())
                    .withQOS(QOS.AT_LEAST_ONCE)
                    .withRetain(true);
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
                DisconnectPacketBuilder disconnectPacketBuilder = new DisconnectPacketBuilder();
                publisher.stop(disconnectPacketBuilder.build());
                publisherEvents.stopFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                successSubscriber.stop(disconnectPacketBuilder.build());
                successSubscriberEvents.stopFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                unsuccessfulSubscriber.stop(disconnectPacketBuilder.build());
                unsuccessfulSubscriberEvents.stopFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * ============================================================
     * Operation Interrupt Tests
     * ============================================================
     */

    // Subscribe interrupt test
    @Test
    public void Interrupt_Sub_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
                subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE);

                try {
                    CompletableFuture<SubAckPacket> subscribeResult = client.subscribe(subscribePacketBuilder.build());
                    client.stop(new DisconnectPacketBuilder().build());
                    SubAckPacket packet = subscribeResult.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    if (ex.getCause().getClass() == CrtRuntimeException.class) {
                        CrtRuntimeException exCrt = (CrtRuntimeException)ex.getCause();
                        if (exCrt.errorCode != 5153) {
                            System.out.println("Exception ocurred when stopping subscribe" +
                                "but it was not AWS_ERROR_MQTT5_USER_REQUESTED_STOP like expected");
                        }
                    }
                }
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    // Unsubscribe interrupt test
    @Test
    public void Interrupt_Unsub_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                UnsubscribePacketBuilder unsubscribePacketBuilder = new UnsubscribePacketBuilder();
                unsubscribePacketBuilder.withSubscription(testTopic);

                try {
                    CompletableFuture<UnsubAckPacket> unsubscribeResult = client.unsubscribe(unsubscribePacketBuilder.build());
                    client.stop(new DisconnectPacketBuilder().build());
                    UnsubAckPacket packet = unsubscribeResult.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    if (ex.getCause().getClass() == CrtRuntimeException.class) {
                        CrtRuntimeException exCrt = (CrtRuntimeException)ex.getCause();
                        if (exCrt.errorCode != 5153) {
                            System.out.println("Exception ocurred when stopping unsubscribe" +
                                "but it was not AWS_ERROR_MQTT5_USER_REQUESTED_STOP like expected");
                        }
                    }
                }
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    // Publish interrupt test
    @Test
    public void Interrupt_Publish_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                client.start();
                events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
                publishPacketBuilder.withTopic(testTopic).withQOS(QOS.AT_LEAST_ONCE).withPayload("null".getBytes());

                try {
                    CompletableFuture<PublishResult> publishResult = client.publish(publishPacketBuilder.build());
                    client.stop(new DisconnectPacketBuilder().build());
                    PublishResult publishData = publishResult.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    if (ex.getCause().getClass() == CrtRuntimeException.class) {
                        CrtRuntimeException exCrt = (CrtRuntimeException)ex.getCause();
                        if (exCrt.errorCode != 5153) {
                            System.out.println("Exception ocurred when stopping publish" +
                                "but it was not AWS_ERROR_MQTT5_USER_REQUESTED_STOP like expected");
                        }
                    }
                }
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * ============================================================
     * Misc Tests
     * ============================================================
     */

    /* Happy path. Check statistics before, make some publishes, check it after */
    @Test
    public void OperationStatistics_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        int messageCount = 10;
        String testUUID = UUID.randomUUID().toString();
        String testTopic = "test/MQTT5_Binding_Java_" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            try (
                Mqtt5Client publisher = new Mqtt5Client(builder.build());
            ) {
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

                PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
                publishPacketBuilder.withTopic(testTopic);
                publishPacketBuilder.withPayload("Hello World".getBytes());
                publishPacketBuilder.withQOS(QOS.AT_LEAST_ONCE);

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

                publisher.stop(new DisconnectPacketBuilder().build());
                events.stopFuture.get(60, TimeUnit.SECONDS);
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * ============================================================
     * Mqtt5ListenerTests Tests
     * ============================================================
     */

    /* [LNew_UC1] Happy Path. Mqtt5Listener Minimal setup and cleanup */
    @Test
    public void LNew_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            Mqtt5Client client = new Mqtt5Client(builder.build());
            assertNotNull(client);
            Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder listenerBuilder = new Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder();
            assertNotNull(listenerBuilder);
            Mqtt5Listener listener = new Mqtt5Listener(listenerBuilder.build(), client);
            assertNotNull(listener);
            listener.close();
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* [LNEW-UC2] Happy Path. Full setup and cleanup */
    @Test
    public void LNew_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            Mqtt5Client client = new Mqtt5Client(builder.build());
            assertNotNull(client);
            Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder listenerBuilder = new Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder();
            assertNotNull(listenerBuilder);
            listenerBuilder.withLifecycleEvents(new LifecycleEvents_Futured());
            listenerBuilder.withListenerPublishEvents(new ListenerPublishEvents_Futured_Counted(""));
            Mqtt5Listener listener = new Mqtt5Listener(listenerBuilder.build(), client);
            assertNotNull(listener);
            listener.close();
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* [LNEW-UC3] Invalid Mqtt5ListenerOption */
    @Test
    public void LNew_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                try(Mqtt5Listener listener = new Mqtt5Listener(null, client)){}
                catch (CrtRuntimeException ex) {
                    System.out.println("EXCEPTION: " + ex);
                    System.out.println(ex.getMessage() + " \n");
                    if ( ex.errorCode == 34) {
                        System.out.println("Error code was AWS_ERROR_INVALID_ARGUMENT like expected!");
                    } else {
                        fail(ex.getMessage());
                    }
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* [LNEW-UC4] Invalid Mqtt5Client */
    @Test
    public void LNew_UC4() {
        skipIfNetworkUnavailable();
        try {
            Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder listenerBuilder = new Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder();
            assertNotNull(listenerBuilder);
            try
            {
                Mqtt5Listener listener = new Mqtt5Listener(listenerBuilder.build(), null);
            }
            catch (CrtRuntimeException ex) {
                System.out.println("EXCEPTION: " + ex);
                System.out.println(ex.getMessage() + " \n");
                if ( ex.errorCode == 34) {
                    System.out.println("Error code was AWS_ERROR_INVALID_ARGUMENT like expected!");
                } else {
                    fail(ex.getMessage());
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* Listener Functionality Test [LFT-UC]
     * Tests test ensure that the operations of mqtt5Listener can perform work correctly
     */

    /* [LFT-UC1] LifecycleEvent Callback Test */
    @Test
    public void LFT_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events1 = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events1);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            Mqtt5Client client = new Mqtt5Client(builder.build());
            assertNotNull(client);

            // Create Mqtt5 Listener
            Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder listenerBuilder = new Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder();
            LifecycleEvents_Futured events2 = new LifecycleEvents_Futured();
            listenerBuilder.withLifecycleEvents(events2);
            Mqtt5Listener listener = new Mqtt5Listener(listenerBuilder.build(), client);
            assertNotNull(listener);


            client.start();
            events2.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
            events1.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

            client.stop(new DisconnectPacketBuilder().build());

            events2.stopFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
            events1.stopFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

            if (tlsContext != null) {
                tlsContext.close();
            }
            listener.close();
            client.close();
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /* [LFT-UC2] Remove Mqtt5Listener for LifecycleEvent */
    @Test
    public void LFT_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);

        try {
            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured events1 = new LifecycleEvents_Futured();
            builder.withLifecycleEvents(events1);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            builder.withTlsContext(tlsContext);

            Mqtt5Client client = new Mqtt5Client(builder.build());
            assertNotNull(client);

            // Create Mqtt5 Listener
            Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder listenerBuilder = new Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder();
            LifecycleEvents_Futured events2 = new LifecycleEvents_Futured();
            listenerBuilder.withLifecycleEvents(events2);
            Mqtt5Listener listener = new Mqtt5Listener(listenerBuilder.build(), client);
            assertNotNull(listener);


            client.start();
            events2.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
            events1.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

            listener.close();
            client.stop(new DisconnectPacketBuilder().build());

            events1.stopFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

            // As the listener is removed, the stopFuture should not complete at this point
            assertTrue(!events2.stopFuture.isDone());

            if (tlsContext != null) {
                tlsContext.close();
            }
            client.close();

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }


    /* [LFT-UC3] ListenerPublishReceived Callback Test */
    @Test
    public void LFT_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();
        final String testTopic = "test/MQTT5_Binding_Java_Test_Topic" + testUUID;
        final String listenerTopic = "test/MQTT5_Binding_Java_Listener_Topic" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder subscriberBuilder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured subscriberLifecycleEvents = new LifecycleEvents_Futured();
            subscriberBuilder.withLifecycleEvents(subscriberLifecycleEvents);

            Mqtt5ClientOptionsBuilder publisherBuilder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured publisherLifecycleEvents = new LifecycleEvents_Futured();
            publisherBuilder.withLifecycleEvents(publisherLifecycleEvents);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            subscriberBuilder.withTlsContext(tlsContext);
            publisherBuilder.withTlsContext(tlsContext);

            PublishEvents_Futured subscriberOnMessageEvents = new PublishEvents_Futured();
            subscriberBuilder.withPublishEvents(subscriberOnMessageEvents);

            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
            publishPacketBuilder.withPayload("Hello World".getBytes());
            publishPacketBuilder.withQOS(QOS.AT_LEAST_ONCE);

            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
            subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE);
            subscribePacketBuilder.withSubscription(listenerTopic, QOS.AT_LEAST_ONCE);

            try (
                Mqtt5Client subscriber = new Mqtt5Client(subscriberBuilder.build());
                Mqtt5Client publisher = new Mqtt5Client(publisherBuilder.build());
            ){
                // Start subscriber
                subscriber.start();
                subscriberLifecycleEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                subscriber.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Create Mqtt5 Listener
                Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder listenerBuilder = new Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder();
                ListenerPublishEvents_Futured_Counted listenerOnMessageEvent = new ListenerPublishEvents_Futured_Counted(listenerTopic);
                listenerBuilder.withListenerPublishEvents(listenerOnMessageEvent);
                Mqtt5Listener listener = new Mqtt5Listener(listenerBuilder.build(), subscriber);
                assertNotNull(listener);

                // Start publisher
                publisher.start();
                publisherLifecycleEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                ////////////////////////////////////////////////////////////////
                // Publish to ListenerTopic
                publisher.publish(publishPacketBuilder.withTopic(listenerTopic).build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Listener should get the listener topic message
                listenerOnMessageEvent.PublishProceedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    /* As we already get message in the listener, it should not take long to invoke client callbacks,
                     * therefore we set a short timeout time. */
                    subscriberOnMessageEvents.publishReceivedFuture.get(10, TimeUnit.SECONDS);
                    fail("Error: The subscriber should no longer received the message on listener topic as it should be handled by the listener.");
                } catch (java.util.concurrent.TimeoutException timeout) {
                    // As the listener is setup, the subscriber should not invoke the publishReceivedEvent
                    System.out.println("The subscriber did not received the message as expected.");
                }
                catch (Exception other) {
                    fail(other.getMessage());
                }

                // Make sure the publish count is correct
                assertEquals(1, listenerOnMessageEvent.proceedPublishCount);
                assertEquals(0, listenerOnMessageEvent.skippedPublishCount);

                ////////////////////////////////////////////////////////////////
                // Publish to Test topic
                publisher.publish(publishPacketBuilder.withTopic(testTopic).build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Listener should skip the test topic message
                listenerOnMessageEvent.PublishSkippedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                assertEquals(1, listenerOnMessageEvent.proceedPublishCount);
                assertEquals(1, listenerOnMessageEvent.skippedPublishCount);

                // As the listener did not listen to testTopic, the subscriber should received the mssage
                subscriberOnMessageEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                listener.close();

            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }


    /* [LFT-UC4] Remove Mqtt5Listener for ListenerPublishReceived */
    @Test
    public void LFT_UC4() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_RSA_KEY != null);
        String testUUID = UUID.randomUUID().toString();
        final String testTopic = "test/MQTT5_Binding_Java_Test_Topic" + testUUID;

        try {
            Mqtt5ClientOptionsBuilder subscriberBuilder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured subscriberLifecycleEvents = new LifecycleEvents_Futured();
            subscriberBuilder.withLifecycleEvents(subscriberLifecycleEvents);

            Mqtt5ClientOptionsBuilder publisherBuilder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
            LifecycleEvents_Futured publisherLifecycleEvents = new LifecycleEvents_Futured();
            publisherBuilder.withLifecycleEvents(publisherLifecycleEvents);

            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT, AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
            TlsContext tlsContext = new TlsContext(tlsOptions);
            tlsOptions.close();
            subscriberBuilder.withTlsContext(tlsContext);
            publisherBuilder.withTlsContext(tlsContext);

            PublishEvents_Futured subscriberOnMessageEvents = new PublishEvents_Futured();
            subscriberBuilder.withPublishEvents(subscriberOnMessageEvents);

            PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
            publishPacketBuilder.withTopic(testTopic);
            publishPacketBuilder.withPayload("Hello World".getBytes());
            publishPacketBuilder.withQOS(QOS.AT_LEAST_ONCE);

            SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
            subscribePacketBuilder.withSubscription(testTopic, QOS.AT_LEAST_ONCE);

            try (
                Mqtt5Client subscriber = new Mqtt5Client(subscriberBuilder.build());
                Mqtt5Client publisher = new Mqtt5Client(publisherBuilder.build());
            ){
                // Start subscriber
                subscriber.start();
                subscriberLifecycleEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                subscriber.subscribe(subscribePacketBuilder.build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Create Mqtt5 Listener
                Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder listenerBuilder = new Mqtt5ListenerOptions.Mqtt5ListenerOptionsBuilder();
                ListenerPublishEvents_Futured_Counted listenerOnMessageEvent = new ListenerPublishEvents_Futured_Counted("");
                listenerBuilder.withListenerPublishEvents(listenerOnMessageEvent);
                Mqtt5Listener listener = new Mqtt5Listener(listenerBuilder.build(), subscriber);
                assertNotNull(listener);

                // Start publisher
                publisher.start();
                publisherLifecycleEvents.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                ////////////////////////////////////////////////////////////////
                // Publish to ListenerTopic
                publisher.publish(publishPacketBuilder.withTopic(testTopic).build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                // Listener should get the listener topic message
                listenerOnMessageEvent.PublishProceedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                try {
                    subscriberOnMessageEvents.publishReceivedFuture.get(10, TimeUnit.SECONDS);
                    fail("Error: The subscriber should no longer received the message on listener topic as it should be handled by the listener.");
                } catch (java.util.concurrent.TimeoutException timeout) {
                    // As the listener is removed, the stopFuture should never complete
                    System.out.println("The subscriber did not received the message as expected.");
                }
                catch (Exception other) {
                    fail(other.getMessage());
                }

                assertEquals(1, listenerOnMessageEvent.proceedPublishCount);
                assertEquals(0, listenerOnMessageEvent.skippedPublishCount);

                listener.close();

                ////////////////////////////////////////////////////////////////
                // Publish to Test topic
                publisher.publish(publishPacketBuilder.withTopic(testTopic).build()).get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);


                // The subscriber should receive the mssage
                subscriberOnMessageEvents.publishReceivedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);

                /* As the listener callback should be invoked before client, the listener should have proceed the
                 * publish message by this point. Make sure the message count does not change. */
                assertEquals(1, listenerOnMessageEvent.proceedPublishCount);
                assertEquals(0, listenerOnMessageEvent.skippedPublishCount);
            }

            if (tlsContext != null) {
                tlsContext.close();
            }

        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    /**
     * ============================================================
     * MQTT5 DIRECT IoT Core CONNECTION TEST CASES
     * ============================================================
     */

    /* MQTT5 ConnDC_Cred_UC1 - MQTT5 connect with Java Keystore */
    @Test
    public void ConnDC_Cred_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FORMAT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FILE != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_PASSWORD != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_ALIAS != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_PASSWORD != null);

        try {
            java.security.KeyStore keyStore = java.security.KeyStore.getInstance(AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FORMAT);
            java.io.FileInputStream keyStoreStream = new java.io.FileInputStream(AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FILE);
            keyStore.load(keyStoreStream, AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_PASSWORD.toCharArray());
            keyStoreStream.close();

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();

            try (
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsJavaKeystore(
                    keyStore,
                    AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_ALIAS,
                    AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_PASSWORD);
                TlsContext tlsContext = new TlsContext(tlsOptions);
            ) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
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

    /* MQTT5 ConnDC_Cred_UC2 - MQTT5 connect with PKCS12 Key */
    @Test
    public void ConnDC_Cred_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY_PASSWORD != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            try (
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsPkcs12(
                    AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY,
                    AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY_PASSWORD);
                TlsContext tlsContext = new TlsContext(tlsOptions);
            ) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
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

    /* MQTT5 ConnDC_Cred_UC3 - MQTT5 connect with Windows Cert Store */
    @Test
    public void ConnDC_Cred_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE != null);

        try {
            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            try (
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsWindowsCertStorePath(
                    AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE);
                TlsContext tlsContext = new TlsContext(tlsOptions);
            ) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
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

    /* MQTT5 ConnDC_Cred_UC4 - MQTT5 connect with PKCS11 */
    @Test
    public void ConnDC_Cred_UC4() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_PKCS11_LIB != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_PKCS11_TOKEN_LABEL != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_PKCS11_PIN != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_PKCS11_PKEY_LABEL != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_PKCS11_CERT_FILE != null);

        try {
            Pkcs11Lib pkcs11Lib = new Pkcs11Lib(AWS_TEST_MQTT5_IOT_CORE_PKCS11_LIB);
            TlsContextPkcs11Options pkcs11Options = new TlsContextPkcs11Options(pkcs11Lib);
            pkcs11Options.withTokenLabel(AWS_TEST_MQTT5_IOT_CORE_PKCS11_TOKEN_LABEL);
            pkcs11Options.withUserPin(AWS_TEST_MQTT5_IOT_CORE_PKCS11_PIN);
            pkcs11Options.withPrivateKeyObjectLabel(AWS_TEST_MQTT5_IOT_CORE_PKCS11_PKEY_LABEL);
            pkcs11Options.withCertificateFilePath(AWS_TEST_MQTT5_IOT_CORE_PKCS11_CERT_FILE);

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            try (
                TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsPkcs11(pkcs11Options);
                TlsContext tlsContext = new TlsContext(tlsOptions);
            ) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(tlsContext);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
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

    /**
     * ============================================================
     * MQTT5 WEBSOCKET IoT Core CONNECTION TEST CASES
     * ============================================================
     */

    /* MQTT5 ConnWS_Cred_UC1 - static credentials connect */
    @Test
    public void ConnWS_Cred_UC1() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_REGION != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN != null);

        CredentialsProvider provider = null;
        AwsSigningConfig signingConfig = new AwsSigningConfig();
        Mqtt5ClientTestSigv4HandshakeTransformer transformer = null;

        try {
            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
                TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsContext = new TlsContext(tlsOptions);
            ) {
                LifecycleEvents_Futured events = new LifecycleEvents_Futured();

                StaticCredentialsProviderBuilder credentialsBuilder = new StaticCredentialsProviderBuilder();
                credentialsBuilder.withAccessKeyId(AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY.getBytes());
                credentialsBuilder.withSecretAccessKey(AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY.getBytes());
                credentialsBuilder.withSessionToken(AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN.getBytes());

                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 443L);
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);
                builder.withTlsContext(tlsContext);

                provider = credentialsBuilder.build();
                signingConfig.setCredentialsProvider(provider);
                signingConfig.setAlgorithm(AwsSigningAlgorithm.SIGV4);
                signingConfig.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_QUERY_PARAMS);
                signingConfig.setRegion(AWS_TEST_MQTT5_IOT_CORE_REGION);
                signingConfig.setService("iotdevicegateway");
                signingConfig.setOmitSessionToken(true);
                transformer = new Mqtt5ClientTestSigv4HandshakeTransformer(signingConfig);

                builder.withWebsocketHandshakeTransform(transformer);
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                    client.stop(disconnect.build());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail(ex.getMessage());
        } finally {
            if (provider != null) {
                provider.close();
            }
            if (signingConfig != null) {
                signingConfig.close();
            }
            if (transformer != null) {
                transformer.close();
            }
        }
    }

    /* MQTT5 ConnWS_Cred_UC2 - default credentials connect */
    @Test
    public void ConnWS_Cred_UC2() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_REGION != null);

        CredentialsProvider provider = null;
        AwsSigningConfig signingConfig = new AwsSigningConfig();
        Mqtt5ClientTestSigv4HandshakeTransformer transformer = null;

        try {
            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
                TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsContext = new TlsContext(tlsOptions);
            ) {
                LifecycleEvents_Futured events = new LifecycleEvents_Futured();

                DefaultChainCredentialsProviderBuilder credentialsBuilder = new DefaultChainCredentialsProviderBuilder();
                credentialsBuilder.withClientBootstrap(bootstrap);

                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 443l);
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);
                builder.withTlsContext(tlsContext);

                provider = credentialsBuilder.build();
                signingConfig.setCredentialsProvider(provider);
                signingConfig.setAlgorithm(AwsSigningAlgorithm.SIGV4);
                signingConfig.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_QUERY_PARAMS);
                signingConfig.setRegion(AWS_TEST_MQTT5_IOT_CORE_REGION);
                signingConfig.setService("iotdevicegateway");
                signingConfig.setOmitSessionToken(true);
                transformer = new Mqtt5ClientTestSigv4HandshakeTransformer(signingConfig);

                builder.withWebsocketHandshakeTransform(transformer);
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                    client.stop(disconnect.build());
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        } finally {
            if (provider != null) {
                provider.close();
            }
            if (signingConfig != null) {
                signingConfig.close();
            }
            if (transformer != null) {
                transformer.close();
            }
        }
    }

    /**
     * MQTT5 ConnWS_Cred_UC3 - Cognito Identity credentials connect
     * TODO: Make another test that supports logins
     */
    @Test
    public void ConnWS_Cred_UC3() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_REGION != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_COGNITO_ENDPOINT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_COGNITO_IDENTITY != null);

        CredentialsProvider provider = null;
        AwsSigningConfig signingConfig = new AwsSigningConfig();
        Mqtt5ClientTestSigv4HandshakeTransformer transformer = null;

        try {
            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
                TlsContextOptions cognitoContextOptions = TlsContextOptions.createDefaultClient();
                TlsContext cognitoContext = new TlsContext(cognitoContextOptions);
                TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsContext = new TlsContext(tlsOptions);
            ) {
                LifecycleEvents_Futured events = new LifecycleEvents_Futured();

                CognitoCredentialsProviderBuilder credentialsBuilder = new CognitoCredentialsProviderBuilder();
                credentialsBuilder.withClientBootstrap(bootstrap);
                credentialsBuilder.withTlsContext(cognitoContext);
                credentialsBuilder.withEndpoint(AWS_TEST_MQTT5_COGNITO_ENDPOINT);
                credentialsBuilder.withIdentity(AWS_TEST_MQTT5_COGNITO_IDENTITY);

                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 443l);
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);
                builder.withTlsContext(tlsContext);

                provider = credentialsBuilder.build();
                signingConfig.setCredentialsProvider(provider);
                signingConfig.setAlgorithm(AwsSigningAlgorithm.SIGV4);
                signingConfig.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_QUERY_PARAMS);
                signingConfig.setRegion(AWS_TEST_MQTT5_IOT_CORE_REGION);
                signingConfig.setService("iotdevicegateway");
                signingConfig.setOmitSessionToken(true);
                transformer = new Mqtt5ClientTestSigv4HandshakeTransformer(signingConfig);

                builder.withWebsocketHandshakeTransform(transformer);
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                    client.stop(disconnect.build());
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        } finally {
            if (provider != null) {
                provider.close();
            }
            if (signingConfig != null) {
                signingConfig.close();
            }
            if (transformer != null) {
                transformer.close();
            }
        }
    }

    /* MQTT5 ConnWS_Cred_UC4 - X509 credentials connect */
    @Test
    public void ConnWS_Cred_UC4() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_HOST != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_REGION != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_X509_CERT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_X509_KEY != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS != null);
        Assume.assumeTrue(AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME != null);

        CredentialsProvider provider = null;
        AwsSigningConfig signingConfig = new AwsSigningConfig();
        Mqtt5ClientTestSigv4HandshakeTransformer transformer = null;

        try {
            try (
                EventLoopGroup elg = new EventLoopGroup(1);
                HostResolver hr = new HostResolver(elg);
                ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
                TlsContextOptions x509ContextOptions = TlsContextOptions.createWithMtlsFromPath(
                    AWS_TEST_MQTT5_IOT_CORE_X509_CERT, AWS_TEST_MQTT5_IOT_CORE_X509_KEY);
                TlsContext x509Context = new TlsContext(x509ContextOptions);
                TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsContext = new TlsContext(tlsOptions);
            ) {
                LifecycleEvents_Futured events = new LifecycleEvents_Futured();

                X509CredentialsProviderBuilder credentialsBuilder = new X509CredentialsProviderBuilder();
                credentialsBuilder.withClientBootstrap(bootstrap);
                credentialsBuilder.withTlsContext(x509Context);
                credentialsBuilder.withEndpoint(AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT);
                credentialsBuilder.withRoleAlias(AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS);
                credentialsBuilder.withThingName(AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME);

                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 443l);
                builder.withLifecycleEvents(events);
                builder.withBootstrap(bootstrap);
                builder.withTlsContext(tlsContext);

                provider = credentialsBuilder.build();
                signingConfig.setCredentialsProvider(provider);
                signingConfig.setAlgorithm(AwsSigningAlgorithm.SIGV4);
                signingConfig.setSignatureType(AwsSigningConfig.AwsSignatureType.HTTP_REQUEST_VIA_QUERY_PARAMS);
                signingConfig.setRegion(AWS_TEST_MQTT5_IOT_CORE_REGION);
                signingConfig.setService("iotdevicegateway");
                signingConfig.setOmitSessionToken(true);
                transformer = new Mqtt5ClientTestSigv4HandshakeTransformer(signingConfig);

                builder.withWebsocketHandshakeTransform(transformer);
                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    events.connectedFuture.get(OPERATION_TIMEOUT_TIME, TimeUnit.SECONDS);
                    DisconnectPacketBuilder disconnect = new DisconnectPacketBuilder();
                    client.stop(disconnect.build());
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        } finally {
            if (provider != null) {
                provider.close();
            }
            if (signingConfig != null) {
                signingConfig.close();
            }
            if (transformer != null) {
                transformer.close();
            }
        }
    }
}
