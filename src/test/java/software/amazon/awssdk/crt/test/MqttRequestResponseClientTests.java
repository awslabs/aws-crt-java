package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.iot.MqttRequestResponseClient;
import software.amazon.awssdk.crt.iot.MqttRequestResponseClientBuilder;
import software.amazon.awssdk.crt.mqtt.*;
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions;
import software.amazon.awssdk.crt.mqtt5.OnAttemptingConnectReturn;
import software.amazon.awssdk.crt.mqtt5.OnConnectionFailureReturn;
import software.amazon.awssdk.crt.mqtt5.OnConnectionSuccessReturn;
import software.amazon.awssdk.crt.mqtt5.OnDisconnectionReturn;
import software.amazon.awssdk.crt.mqtt5.OnStoppedReturn;
import software.amazon.awssdk.crt.mqtt5.packets.ConnectPacket;


import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class MqttRequestResponseClientTests extends CrtTestFixture {

    static final String AWS_TEST_MQTT5_IOT_CORE_HOST = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_HOST");
    static final String AWS_TEST_MQTT5_IOT_CORE_RSA_CERT = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_RSA_CERT");
    static final String AWS_TEST_MQTT5_IOT_CORE_RSA_KEY = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_RSA_KEY");

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

    @Test
    public void CreateDestroyMqtt5() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        try (Mqtt5Client protocolClient = createMqtt5Client()) {
            MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder();
            rrBuilder.withMaxRequestResponseSubscriptions(4)
                    .withMaxStreamingSubscriptions(2)
                    .withOperationTimeoutSeconds(30);

            MqttRequestResponseClient rrClient = rrBuilder.build(protocolClient);
            rrClient.close();
            protocolClient.stop();
        }
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt5CreateFailureBadMaxRequestResponseSubscriptions() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        try (Mqtt5Client protocolClient = createMqtt5Client()) {
            MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder();
            rrBuilder.withMaxRequestResponseSubscriptions(0)
                    .withMaxStreamingSubscriptions(2)
                    .withOperationTimeoutSeconds(30);

           rrBuilder.build(protocolClient);
        }
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt5CreateFailureBadMaxStreamingSubscriptions() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        try (Mqtt5Client protocolClient = createMqtt5Client()) {
            MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder();
            rrBuilder.withMaxRequestResponseSubscriptions(4)
                    .withMaxStreamingSubscriptions(-1)
                    .withOperationTimeoutSeconds(30);

            rrBuilder.build(protocolClient);
        }
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt5CreateFailureBadOperationTimeout() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        try (Mqtt5Client protocolClient = createMqtt5Client()) {
            MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder();
            rrBuilder.withMaxRequestResponseSubscriptions(4)
                    .withMaxStreamingSubscriptions(2)
                    .withOperationTimeoutSeconds(-5);

            rrBuilder.build(protocolClient);
        }
    }

    @Test
    public void CreateDestroyMqtt311() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        try (MqttClientConnection protocolClient = createMqtt311Client()) {
            MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder();
            rrBuilder.withMaxRequestResponseSubscriptions(4)
                    .withMaxStreamingSubscriptions(2)
                    .withOperationTimeoutSeconds(30);

            MqttRequestResponseClient rrClient = rrBuilder.build(protocolClient);
            rrClient.close();

            protocolClient.disconnect();
        }
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt311CreateFailureBadMaxRequestResponseSubscriptions() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        try (MqttClientConnection protocolClient = createMqtt311Client()) {
            MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder();
            rrBuilder.withMaxRequestResponseSubscriptions(0)
                    .withMaxStreamingSubscriptions(2)
                    .withOperationTimeoutSeconds(30);

            rrBuilder.build(protocolClient);
        }
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt311CreateFailureBadMaxStreamingSubscriptions() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        try (MqttClientConnection protocolClient = createMqtt311Client()) {
            MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder();
            rrBuilder.withMaxRequestResponseSubscriptions(4)
                    .withMaxStreamingSubscriptions(-1)
                    .withOperationTimeoutSeconds(30);

            rrBuilder.build(protocolClient);
        }
    }

    @Test(expected = CrtRuntimeException.class)
    public void Mqtt311CreateFailureBadOperationTimeout() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
                AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT5_IOT_CORE_RSA_KEY);

        try (MqttClientConnection protocolClient = createMqtt311Client()) {
            MqttRequestResponseClientBuilder rrBuilder = new MqttRequestResponseClientBuilder();
            rrBuilder.withMaxRequestResponseSubscriptions(4)
                    .withMaxStreamingSubscriptions(2)
                    .withOperationTimeoutSeconds(-5);

            rrBuilder.build(protocolClient);
        }
    }
}
