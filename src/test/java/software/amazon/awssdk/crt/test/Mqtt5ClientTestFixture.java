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
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/* For environment variable setup, see SetupCrossCICrtEnvironment in the CRT builder */
public class Mqtt5ClientTestFixture extends CrtTestFixture {

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
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_LIB = System.getenv("AWS_TEST_PKCS11_LIB");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_TOKEN_LABEL = System.getenv("AWS_TEST_PKCS11_TOKEN_LABEL");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_PIN = System.getenv("AWS_TEST_PKCS11_PIN");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_PKEY_LABEL = System.getenv("AWS_TEST_PKCS11_PKEY_LABEL");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_CERT_FILE = System.getenv("AWS_TEST_PKCS11_CERT_FILE");
    // MQTT5 X509
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_CERT = System.getenv("AWS_TEST_MQTT5_IOT_CORE_X509_CERT");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_KEY = System.getenv("AWS_TEST_MQTT5_IOT_CORE_X509_KEY");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT = System.getenv("AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS = System.getenv("AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME = System.getenv("AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME");
    // MQTT5 Windows Cert Store
    static final String AWS_TEST_MQTT5_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS = System.getenv("AWS_TEST_MQTT5_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS");
    static final String AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE = System.getenv("AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE");

    protected int OPERATION_TIMEOUT_TIME = 30;

    public Mqtt5ClientTestFixture() {
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

}
