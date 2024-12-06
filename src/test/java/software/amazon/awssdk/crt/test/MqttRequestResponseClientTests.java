/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.iot.MqttRequestResponse;
import software.amazon.awssdk.crt.iot.MqttRequestResponseClient;
import software.amazon.awssdk.crt.iot.MqttRequestResponseClientBuilder;
import software.amazon.awssdk.crt.iot.RequestResponseOperation;
import software.amazon.awssdk.crt.iot.ResponsePath;
import software.amazon.awssdk.crt.mqtt.*;
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions;
import software.amazon.awssdk.crt.mqtt5.OnAttemptingConnectReturn;
import software.amazon.awssdk.crt.mqtt5.OnConnectionFailureReturn;
import software.amazon.awssdk.crt.mqtt5.OnConnectionSuccessReturn;
import software.amazon.awssdk.crt.mqtt5.OnDisconnectionReturn;
import software.amazon.awssdk.crt.mqtt5.OnStoppedReturn;
import software.amazon.awssdk.crt.mqtt5.packets.ConnectPacket;


import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;


public class MqttRequestResponseClientTests extends CrtTestFixture {

    public enum MqttVersion {
        Mqtt5,
        Mqtt311
    }

    public class TestContext {
        public Mqtt5Client mqtt5Client;
        public MqttClientConnection mqtt311Client;
        public MqttRequestResponseClient rrClient;

        public TestContext(MqttVersion version, MqttRequestResponseClientBuilder builder) {
            if (builder == null) {
                builder = createRequestResponseClientBuilder();
            }

            if (version == MqttVersion.Mqtt5) {
                try (Mqtt5Client protocolClient = createMqtt5Client();
                     MqttRequestResponseClient rrClient = builder.build(protocolClient)) {
                    this.mqtt5Client = protocolClient;
                    this.rrClient = rrClient;
                    this.mqtt5Client.addRef();
                    this.rrClient.addRef();
                }
            } else {
                try (MqttClientConnection protocolClient = createMqtt311Client();
                     MqttRequestResponseClient rrClient = builder.build(protocolClient)) {
                    this.mqtt311Client = protocolClient;
                    this.rrClient = rrClient;
                    this.mqtt311Client.addRef();
                    this.rrClient.addRef();
                }
            }
        }

        public void close() {
            if (this.rrClient != null) {
                rrClient.close();
            }

            if (this.mqtt5Client != null) {
                this.mqtt5Client.stop();
                this.mqtt5Client.close();
            }

            if (this.mqtt311Client != null) {
                this.mqtt311Client.disconnect();
                this.mqtt311Client.close();
            }
        }
    }

    public TestContext context;

    static final boolean AWS_GRAALVM_CI = System.getProperty("AWS_GRAALVM_CI") != null;

    static final String AWS_TEST_MQTT5_IOT_CORE_HOST = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_HOST");
    static final String AWS_TEST_MQTT5_IOT_CORE_RSA_CERT = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_RSA_CERT");
    static final String AWS_TEST_MQTT5_IOT_CORE_RSA_KEY = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_RSA_KEY");

    public MqttRequestResponseClientTests() {
        /**
         * Disable test for native image, because:
         * - On MacOS, when cert and private key used for TLS, it will be imported to KeyChain,
         *      and KeyChain will restrict other application to use the private key
         * - For GraalVM test, Java will run the tests firstly, and import the mTLS private key.
         *      After that, when native test runs, it's a different application than Java,
         *      which will use the same key, and MacOS blocks the usage and result in hanging.
         * - Locally, you can either put in your password to allow the usage, or delete the key from the KeyChain,
         *      But, in CI, it's very complicated, and decided to not support MQTT tests for now.
         */
        Assume.assumeFalse(AWS_GRAALVM_CI && CRT.getOSIdentifier() == "osx");
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);
    }

    static private Mqtt5Client createMqtt5Client() {
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);) {
            try (TlsContext tlsContext = new TlsContext(contextOptions);) {
                CompletableFuture<Boolean> connected = new CompletableFuture<Boolean>();

                String clientId = "aws-crt-java-" + (UUID.randomUUID()).toString();
                ConnectPacket.ConnectPacketBuilder connectBuilder = new ConnectPacket.ConnectPacketBuilder();
                connectBuilder.withClientId(clientId);

                Mqtt5ClientOptions.Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptions.Mqtt5ClientOptionsBuilder(
                        AWS_TEST_MQTT5_IOT_CORE_HOST,
                        (long) 8883);

                builder.withLifecycleEvents(new Mqtt5ClientOptions.LifecycleEvents() {
                    @Override
                    public void onAttemptingConnect(Mqtt5Client client, OnAttemptingConnectReturn onAttemptingConnectReturn) {}

                    @Override
                    public void onConnectionSuccess(Mqtt5Client client, OnConnectionSuccessReturn onConnectionSuccessReturn) {
                        connected.complete(true);
                    }

                    @Override
                    public void onConnectionFailure(Mqtt5Client client, OnConnectionFailureReturn onConnectionFailureReturn) {}

                    @Override
                    public void onDisconnection(Mqtt5Client client, OnDisconnectionReturn onDisconnectionReturn) {}

                    @Override
                    public void onStopped(Mqtt5Client client, OnStoppedReturn onStoppedReturn) {}
                });
                builder.withTlsContext(tlsContext);
                builder.withConnectOptions(connectBuilder.build());

                Mqtt5Client client = new Mqtt5Client(builder.build());
                client.start();

                try {
                    connected.get();
                } catch (Exception e) {
                    client.close();
                    return null;
                }

                return client;
            }
        }
    }

    static private MqttClientConnection createMqtt311Client() {
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);) {
            try (TlsContext tlsContext = new TlsContext(contextOptions);) {
                String clientId = "aws-crt-java-" + (UUID.randomUUID()).toString();

                try (MqttClient client = new MqttClient(tlsContext);
                    MqttConnectionConfig config = new MqttConnectionConfig()) {
                    config.setMqttClient(client);
                    config.setClientId(clientId);
                    config.setEndpoint(AWS_TEST_MQTT5_IOT_CORE_HOST);
                    config.setPort(8883);

                    MqttClientConnection connection = new MqttClientConnection(config);

                    CompletableFuture<Boolean> connected = connection.connect();
                    try {
                        connected.get();
                    } catch (Exception e) {
                        connection.close();
                        return null;
                    }

                    return connection;
                }
            }
        }
    }

    static public MqttRequestResponseClientBuilder createRequestResponseClientBuilder() {
        MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder();
        rrBuilder.withMaxRequestResponseSubscriptions(4)
                .withMaxStreamingSubscriptions(2)
                .withOperationTimeoutSeconds(30);

        return rrBuilder;
    }

    @After
    public void cleanupContext() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @Test
    public void CreateDestroyMqtt5() {
        skipIfNetworkUnavailable();

        this.context = new TestContext(MqttVersion.Mqtt5, null);
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt5CreateFailureBadMaxRequestResponseSubscriptions() {
        skipIfNetworkUnavailable();

        MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder()
            .withMaxRequestResponseSubscriptions(0)
            .withMaxStreamingSubscriptions(2)
            .withOperationTimeoutSeconds(30);

        this.context = new TestContext(MqttVersion.Mqtt5, rrBuilder);
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt5CreateFailureBadMaxStreamingSubscriptions() {
        skipIfNetworkUnavailable();

        MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder()
            .withMaxRequestResponseSubscriptions(4)
            .withMaxStreamingSubscriptions(-1)
            .withOperationTimeoutSeconds(30);

        this.context = new TestContext(MqttVersion.Mqtt5, rrBuilder);
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt5CreateFailureBadOperationTimeout() {
        skipIfNetworkUnavailable();

        MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder()
            .withMaxRequestResponseSubscriptions(4)
            .withMaxStreamingSubscriptions(2)
            .withOperationTimeoutSeconds(-5);

        this.context = new TestContext(MqttVersion.Mqtt5, rrBuilder);
    }

    @Test
    public void CreateDestroyMqtt311() {
        skipIfNetworkUnavailable();

        this.context = new TestContext(MqttVersion.Mqtt311, null);
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt311CreateFailureBadMaxRequestResponseSubscriptions() {
        skipIfNetworkUnavailable();

        MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder()
            .withMaxRequestResponseSubscriptions(0)
            .withMaxStreamingSubscriptions(2)
            .withOperationTimeoutSeconds(30);

        this.context = new TestContext(MqttVersion.Mqtt311, rrBuilder);
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt311CreateFailureBadMaxStreamingSubscriptions() {
        skipIfNetworkUnavailable();

        MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder()
            .withMaxRequestResponseSubscriptions(4)
            .withMaxStreamingSubscriptions(-1)
            .withOperationTimeoutSeconds(30);

        this.context = new TestContext(MqttVersion.Mqtt311, rrBuilder);
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt311CreateFailureBadOperationTimeout() {
        skipIfNetworkUnavailable();

        MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder()
                .withMaxRequestResponseSubscriptions(4)
                .withMaxStreamingSubscriptions(2)
                .withOperationTimeoutSeconds(-5);

        this.context = new TestContext(MqttVersion.Mqtt311, rrBuilder);
    }

    RequestResponseOperation createGetNamedShadowRequest(String thing, String shadow, boolean withCorrelationToken) {
        String acceptedTopic = String.format("$aws/things/%s/shadow/name/%s/get/accepted", thing, shadow);
        String rejectedTopic = String.format("$aws/things/%s/shadow/name/%s/get/rejected", thing, shadow);

        RequestResponseOperation.RequestResponseOperationBuilder builder = RequestResponseOperation.builder()
            .withSubscription(String.format("$aws/things/%s/shadow/name/%s/get/+", thing, shadow))
            .withPublishTopic(String.format("$aws/things/%s/shadow/name/%s/get", thing, shadow));

        if (withCorrelationToken) {
            String correlationToken = (UUID.randomUUID()).toString();
            String payloadAsString = String.format("{\"clientToken\":\"%s\"}", correlationToken);

            builder.withCorrelationToken(correlationToken);
            builder.withPayload(payloadAsString.getBytes(StandardCharsets.UTF_8));
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(acceptedTopic).withCorrelationTokenJsonPath("clientToken").build());
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(rejectedTopic).withCorrelationTokenJsonPath("clientToken").build());
        } else {
            builder.withPayload("{}".getBytes(StandardCharsets.UTF_8));
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(acceptedTopic).build());
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(rejectedTopic).build());
        }

        return builder.build();
    }

    public static String REJECTED_TOPIC_SUBSTRING = "rejected";
    public static String ACCEPTED_TOPIC_SUBSTRING = "accepted";
    public static String NO_SUCH_SHADOW_SUBSTRING = "No shadow exists with name";

    void doGetNamedShadow(String thing, String shadow, boolean withCorrelationToken, String expectedTopicSubstring, String expectedPayloadSubstring) {
        RequestResponseOperation operation = createGetNamedShadowRequest(thing, shadow, withCorrelationToken);
        CompletableFuture<MqttRequestResponse> responseFuture = this.context.rrClient.submitRequest(operation);
        MqttRequestResponse response = null;

        try {
            response = responseFuture.get();
        } catch (Exception e) {
            ;
        }

        Assert.assertNotNull(response);

        String payloadAsString = new String(response.getPayload(), StandardCharsets.UTF_8);
        Assert.assertTrue(payloadAsString.contains(expectedPayloadSubstring));
        Assert.assertTrue(response.getTopic().contains(expectedTopicSubstring));
    }

    @Test
    public void GetNamedShadowFailureNoSuchShadowWithCorrelationMqtt5() {
        skipIfNetworkUnavailable();
        this.context = new TestContext(MqttVersion.Mqtt5, null);
        doGetNamedShadow("NoSuchThing", "Derp", true, REJECTED_TOPIC_SUBSTRING, NO_SUCH_SHADOW_SUBSTRING);
    }

    @Test
    public void GetNamedShadowFailureNoSuchShadowWithoutCorrelationMqtt5() {
        skipIfNetworkUnavailable();
        this.context = new TestContext(MqttVersion.Mqtt5, null);
        doGetNamedShadow("NoSuchThing", "Derp", false, REJECTED_TOPIC_SUBSTRING, NO_SUCH_SHADOW_SUBSTRING);
    }

    @Test
    public void GetNamedShadowFailureNoSuchShadowWithCorrelationMqtt311() {
        skipIfNetworkUnavailable();
        this.context = new TestContext(MqttVersion.Mqtt311, null);
        doGetNamedShadow("NoSuchThing", "Derp", true, REJECTED_TOPIC_SUBSTRING, NO_SUCH_SHADOW_SUBSTRING);
    }

    @Test
    public void GetNamedShadowFailureNoSuchShadowWithoutCorrelationMqtt311() {
        skipIfNetworkUnavailable();
        this.context = new TestContext(MqttVersion.Mqtt311, null);
        doGetNamedShadow("NoSuchThing", "Derp", false, REJECTED_TOPIC_SUBSTRING, NO_SUCH_SHADOW_SUBSTRING);
    }

    RequestResponseOperation createUpdateNamedShadowRequest(String thing, String shadow, boolean withCorrelationToken) {
        String acceptedTopic = String.format("$aws/things/%s/shadow/name/%s/update/accepted", thing, shadow);
        String rejectedTopic = String.format("$aws/things/%s/shadow/name/%s/update/rejected", thing, shadow);

        RequestResponseOperation.RequestResponseOperationBuilder builder = RequestResponseOperation.builder()
            .withSubscription(acceptedTopic)
            .withSubscription(rejectedTopic)
            .withPublishTopic(String.format("$aws/things/%s/shadow/name/%s/update", thing, shadow));

        String desiredChange = "{\"Uff\":\"Dah\"}";

        if (withCorrelationToken) {
            String correlationToken = (UUID.randomUUID()).toString();
            String payloadAsString = String.format("{\"clientToken\":\"%s\",\"state\":{\"desired\":%s}}", correlationToken, desiredChange);

            builder.withCorrelationToken(correlationToken);
            builder.withPayload(payloadAsString.getBytes(StandardCharsets.UTF_8));
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(acceptedTopic).withCorrelationTokenJsonPath("clientToken").build());
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(rejectedTopic).withCorrelationTokenJsonPath("clientToken").build());
        } else {
            builder.withPayload(String.format("{\"state\":{\"desired\":%s}}", desiredChange).getBytes(StandardCharsets.UTF_8));
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(acceptedTopic).build());
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(rejectedTopic).build());
        }

        return builder.build();
    }

    void doUpdateNamedShadowSuccess(String thing, String shadow, boolean withCorrelationToken, String expectedTopicSubstring, String expectedPayloadSubstring) {
        RequestResponseOperation operation = createUpdateNamedShadowRequest(thing, shadow, withCorrelationToken);
        CompletableFuture<MqttRequestResponse> responseFuture = this.context.rrClient.submitRequest(operation);
        MqttRequestResponse response = null;

        try {
            response = responseFuture.get();
        } catch (Exception e) {
            ;
        }

        Assert.assertNotNull(response);

        String payloadAsString = new String(response.getPayload(), StandardCharsets.UTF_8);
        Assert.assertTrue(payloadAsString.contains(expectedPayloadSubstring));
        Assert.assertTrue(response.getTopic().contains(expectedTopicSubstring));
    }

    RequestResponseOperation createDeleteNamedShadowRequest(String thing, String shadow, boolean withCorrelationToken) {
        String acceptedTopic = String.format("$aws/things/%s/shadow/name/%s/delete/accepted", thing, shadow);
        String rejectedTopic = String.format("$aws/things/%s/shadow/name/%s/delete/rejected", thing, shadow);

        RequestResponseOperation.RequestResponseOperationBuilder builder = RequestResponseOperation.builder()
            .withSubscription(acceptedTopic)
            .withSubscription(rejectedTopic)
            .withPublishTopic(String.format("$aws/things/%s/shadow/name/%s/delete", thing, shadow));

        if (withCorrelationToken) {
            String correlationToken = (UUID.randomUUID()).toString();
            String payloadAsString = String.format("{\"clientToken\":\"%s\"}", correlationToken);

            builder.withCorrelationToken(correlationToken);
            builder.withPayload(payloadAsString.getBytes(StandardCharsets.UTF_8));
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(acceptedTopic).withCorrelationTokenJsonPath("clientToken").build());
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(rejectedTopic).withCorrelationTokenJsonPath("clientToken").build());
        } else {
            builder.withPayload("{}".getBytes(StandardCharsets.UTF_8));
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(acceptedTopic).build());
            builder.withResponsePath(ResponsePath.builder().withResponseTopic(rejectedTopic).build());
        }

        return builder.build();
    }

    void doDeleteNamedShadowSuccess(String thing, String shadow, boolean withCorrelationToken, String expectedTopicSubstring, String expectedPayloadSubstring) {
        RequestResponseOperation operation = createDeleteNamedShadowRequest(thing, shadow, true);
        CompletableFuture<MqttRequestResponse> responseFuture = this.context.rrClient.submitRequest(operation);
        MqttRequestResponse response = null;

        try {
            response = responseFuture.get();
        } catch (Exception e) {
            ;
        }

        Assert.assertNotNull(response);

        String payloadAsString = new String(response.getPayload(), StandardCharsets.UTF_8);
        Assert.assertTrue(payloadAsString.contains(expectedPayloadSubstring));
        Assert.assertTrue(response.getTopic().contains(expectedTopicSubstring));
    }

    public static String METADATA_SUBSTRING = "metadata";
    public static String UFF_SUBSTRING = "Uff";
    public static String VERSION_SUBSTRING = "version";

    public void doUpdateNamedShadowSuccessTest(MqttVersion version, boolean useCorrelationToken) {
        this.context = new TestContext(version, null);

        String thing = (UUID.randomUUID()).toString();
        String shadow = (UUID.randomUUID()).toString();

        doGetNamedShadow(thing, shadow, useCorrelationToken, REJECTED_TOPIC_SUBSTRING, NO_SUCH_SHADOW_SUBSTRING);
        doUpdateNamedShadowSuccess(thing, shadow, useCorrelationToken, ACCEPTED_TOPIC_SUBSTRING, METADATA_SUBSTRING);
        doGetNamedShadow(thing, shadow, useCorrelationToken, ACCEPTED_TOPIC_SUBSTRING, UFF_SUBSTRING);
        doDeleteNamedShadowSuccess(thing, shadow, useCorrelationToken, ACCEPTED_TOPIC_SUBSTRING, VERSION_SUBSTRING);
    }

    @Test
    public void UpdateNamedShadowSuccessWithCorrelationMqtt5() {
        skipIfNetworkUnavailable();
        doUpdateNamedShadowSuccessTest(MqttVersion.Mqtt5, true);
    }

    @Test
    public void UpdateNamedShadowSuccessWithoutCorrelationMqtt5() {
        skipIfNetworkUnavailable();
        doUpdateNamedShadowSuccessTest(MqttVersion.Mqtt5, false);
    }

    @Test
    public void UpdateNamedShadowSuccessWithCorrelationMqtt311() {
        skipIfNetworkUnavailable();
        doUpdateNamedShadowSuccessTest(MqttVersion.Mqtt311, true);
    }

    @Test
    public void UpdateNamedShadowSuccessWithoutCorrelationMqtt311() {
        skipIfNetworkUnavailable();
        doUpdateNamedShadowSuccessTest(MqttVersion.Mqtt311, false);
    }

    public void doBadGetNamedShadowTest(Supplier<RequestResponseOperation> builderConstructor) {
        this.context = new TestContext(MqttVersion.Mqtt5, null);

        RequestResponseOperation operation = builderConstructor.get();
        CompletableFuture<MqttRequestResponse> responseFuture = this.context.rrClient.submitRequest(operation);

        Assert.assertTrue(false);
    }

    @Test(expected = CrtRuntimeException.class)
    public void GetNamedShadowFailureNullOperation() {
        skipIfNetworkUnavailable();
        doBadGetNamedShadowTest(() -> null);
    }

    @Test(expected = CrtRuntimeException.class)
    public void GetNamedShadowFailureNullPublishTopic() {
        skipIfNetworkUnavailable();
        doBadGetNamedShadowTest(() -> {
            return RequestResponseOperation.builder()
                .withSubscription("hello/world/accepted")
                .withResponsePath(ResponsePath.builder().withResponseTopic("hello/world/accepted").build())
                .withPayload("{}".getBytes(StandardCharsets.UTF_8))
                .build();
        });
    }

    @Test(expected = CrtRuntimeException.class)
    public void GetNamedShadowFailureNoSubscriptions() {
        skipIfNetworkUnavailable();
        doBadGetNamedShadowTest(() -> {
            return RequestResponseOperation.builder()
                .withPublishTopic("hello/world")
                .withResponsePath(ResponsePath.builder().withResponseTopic("hello/world/accepted").build())
                .withPayload("{}".getBytes(StandardCharsets.UTF_8))
                .build();
        });
    }

    @Test(expected = CrtRuntimeException.class)
    public void GetNamedShadowFailureNullSubscriptions() {
        skipIfNetworkUnavailable();
        doBadGetNamedShadowTest(() -> {
            return RequestResponseOperation.builder()
                .withPublishTopic("hello/world")
                .withSubscription(null)
                .withResponsePath(ResponsePath.builder().withResponseTopic("hello/world/accepted").build())
                .withPayload("{}".getBytes(StandardCharsets.UTF_8))
                .build();
        });
    }

    @Test(expected = CrtRuntimeException.class)
    public void GetNamedShadowFailureNoResponsePaths() {
        skipIfNetworkUnavailable();
        doBadGetNamedShadowTest(() -> {
            return RequestResponseOperation.builder()
                .withPublishTopic("hello/world")
                .withSubscription("hello/world/accepted")
                .withPayload("{}".getBytes(StandardCharsets.UTF_8))
                .build();
        });
    }

    @Test(expected = CrtRuntimeException.class)
    public void GetNamedShadowFailureNullResponsePath() {
        skipIfNetworkUnavailable();
        doBadGetNamedShadowTest(() -> {
            return RequestResponseOperation.builder()
                .withPublishTopic("hello/world")
                .withResponsePath(null)
                .withSubscription("hello/world/accepted")
                .withPayload("{}".getBytes(StandardCharsets.UTF_8))
                .build();
        });
    }

    @Test(expected = CrtRuntimeException.class)
    public void GetNamedShadowFailureResponsePathNullTopic() {
        skipIfNetworkUnavailable();
        doBadGetNamedShadowTest(() -> {
            return RequestResponseOperation.builder()
                .withPublishTopic("hello/world")
                .withResponsePath(ResponsePath.builder().withResponseTopic(null).build())
                .withSubscription("hello/world/accepted")
                .withPayload("{}".getBytes(StandardCharsets.UTF_8))
                .build();
        });
    }

    @Test(expected = CrtRuntimeException.class)
    public void GetNamedShadowFailureNullPayload() {
        skipIfNetworkUnavailable();
        doBadGetNamedShadowTest(() -> {
            return RequestResponseOperation.builder()
                .withPublishTopic("hello/world")
                .withSubscription("hello/world/accepted")
                .withResponsePath(ResponsePath.builder().withResponseTopic("hello/world/accepted").build())
                .withPayload(null)
                .build();
        });
    }

    public void doGetNamedShadowFailureTimeoutTest(MqttVersion version) {
        MqttRequestResponseClientBuilder clientBuilder = new MqttRequestResponseClientBuilder()
            .withMaxRequestResponseSubscriptions(4)
            .withMaxStreamingSubscriptions(2)
            .withOperationTimeoutSeconds(2);
        this.context = new TestContext(version, clientBuilder);

        String badAcceptedTopic = "hello/world/accepted";
        String badRejectedTopic = "hello/world/rejected";
        String correlationToken = (UUID.randomUUID()).toString();
        String payloadAsString = String.format("{\"clientToken\":\"%s\"}", correlationToken);

        RequestResponseOperation operation = RequestResponseOperation.builder()
            .withSubscription(badAcceptedTopic)
            .withPublishTopic("uff/dah")
            .withCorrelationToken(correlationToken)
            .withPayload(payloadAsString.getBytes(StandardCharsets.UTF_8))
            .withResponsePath(ResponsePath.builder().withResponseTopic(badAcceptedTopic).withCorrelationTokenJsonPath("clientToken").build())
            .withResponsePath(ResponsePath.builder().withResponseTopic(badRejectedTopic).withCorrelationTokenJsonPath("clientToken").build())
            .build();

        CompletableFuture<MqttRequestResponse> responseFuture = this.context.rrClient.submitRequest(operation);

        try {
            responseFuture.get();
        } catch (InterruptedException ie) {
            Assert.assertTrue(false);
        } catch (ExecutionException ex) {
            String message = ex.getMessage();
            Assert.assertTrue(message.contains("timeout"));
        }
    }

    @Test
    public void GetNamedShadowFailureTimeoutMqtt5() {
        skipIfNetworkUnavailable();

        doGetNamedShadowFailureTimeoutTest(MqttVersion.Mqtt5);
    }

    @Test
    public void GetNamedShadowFailureTimeoutMqtt311() {
        skipIfNetworkUnavailable();

        doGetNamedShadowFailureTimeoutTest(MqttVersion.Mqtt311);
    }
}
