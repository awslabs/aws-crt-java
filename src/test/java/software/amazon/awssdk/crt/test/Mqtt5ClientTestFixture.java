package software.amazon.awssdk.crt.test;


import software.amazon.awssdk.crt.*;

import software.amazon.awssdk.crt.mqtt5.*;

import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.PublishEvents;
import software.amazon.awssdk.crt.mqtt5.packets.*;


import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import java.util.concurrent.CompletableFuture;


/* For environment variable setup, see SetupCrossCICrtEnvironment in the CRT builder */
public class Mqtt5ClientTestFixture extends CrtTestFixture {

    // MQTT5 Codebuild/Direct connections data
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_HOST = System.getProperty("AWS_TEST_MQTT5_DIRECT_MQTT_HOST");
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_PORT = System.getProperty("AWS_TEST_MQTT5_DIRECT_MQTT_PORT");
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST = System.getProperty("AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST");
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT = System.getProperty("AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT");
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST = System.getProperty("AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST");
    static final String AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT = System.getProperty("AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT");
    // MQTT5 Codebuild/Websocket connections data
    static final String AWS_TEST_MQTT5_WS_MQTT_HOST = System.getProperty("AWS_TEST_MQTT5_WS_MQTT_HOST");
    static final String AWS_TEST_MQTT5_WS_MQTT_PORT = System.getProperty("AWS_TEST_MQTT5_WS_MQTT_PORT");
    static final String AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST = System.getProperty("AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST");
    static final String AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT = System.getProperty("AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT");
    static final String AWS_TEST_MQTT5_WS_MQTT_TLS_HOST = System.getProperty("AWS_TEST_MQTT5_WS_MQTT_TLS_HOST");
    static final String AWS_TEST_MQTT5_WS_MQTT_TLS_PORT = System.getProperty("AWS_TEST_MQTT5_WS_MQTT_TLS_PORT");
    // MQTT5 Codebuild misc connections data
    static final String AWS_TEST_MQTT5_BASIC_AUTH_USERNAME = System.getProperty("AWS_TEST_MQTT5_BASIC_AUTH_USERNAME");
    static final String AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD = System.getProperty("AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD");
    static final String AWS_TEST_MQTT5_CERTIFICATE_FILE = System.getProperty("AWS_TEST_MQTT5_CERTIFICATE_FILE");
    static final String AWS_TEST_MQTT5_KEY_FILE = System.getProperty("AWS_TEST_MQTT5_KEY_FILE");
    // MQTT5 Proxy
    static final String AWS_TEST_MQTT5_PROXY_HOST = System.getProperty("AWS_TEST_MQTT5_PROXY_HOST");
    static final String AWS_TEST_MQTT5_PROXY_PORT = System.getProperty("AWS_TEST_MQTT5_PROXY_PORT");
    // MQTT5 Endpoint/Host credentials
    static final String AWS_TEST_MQTT5_IOT_CORE_HOST = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_HOST");
    static final String AWS_TEST_MQTT5_IOT_CORE_REGION = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_REGION");
    static final String AWS_TEST_MQTT5_IOT_CORE_RSA_CERT = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_RSA_CERT");
    static final String AWS_TEST_MQTT5_IOT_CORE_RSA_KEY = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_RSA_KEY");
    // MQTT5 Static credential related
    static final String AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY = System.getProperty("AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY");
    static final String AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY = System.getProperty("AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY");
    static final String AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN = System.getProperty("AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN");
    // MQTT5 Cognito
    static final String AWS_TEST_MQTT5_COGNITO_ENDPOINT = System.getProperty("AWS_TEST_MQTT5_COGNITO_ENDPOINT");
    static final String AWS_TEST_MQTT5_COGNITO_IDENTITY = System.getProperty("AWS_TEST_MQTT5_COGNITO_IDENTITY");
    // MQTT5 Keystore
    static final String AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FORMAT = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FORMAT");
    static final String AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FILE = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FILE");
    static final String AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_PASSWORD = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_PASSWORD");
    static final String AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_ALIAS = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_ALIAS");
    static final String AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_PASSWORD = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_PASSWORD");
    // MQTT5 PKCS12
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY_PASSWORD = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY_PASSWORD");
    // MQTT5 PKCS11
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_LIB = System.getProperty("AWS_TEST_PKCS11_LIB");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_TOKEN_LABEL = System.getProperty("AWS_TEST_PKCS11_TOKEN_LABEL");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_PIN = System.getProperty("AWS_TEST_PKCS11_PIN");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_PKEY_LABEL = System.getProperty("AWS_TEST_PKCS11_PKEY_LABEL");
    static final String AWS_TEST_MQTT5_IOT_CORE_PKCS11_CERT_FILE = System.getProperty("AWS_TEST_PKCS11_CERT_FILE");
    // MQTT5 X509
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_CERT = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_X509_CERT");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_KEY = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_X509_KEY");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS");
    static final String AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME");
    // MQTT5 Windows Cert Store
    static final String AWS_TEST_MQTT5_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS");
    static final String AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE = System.getProperty("AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE");

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
        // The "main" future which is intended to be used for waiting for all publishes.
        CompletableFuture<Void> publishReceivedFuture = new CompletableFuture<>();
        // Additional future which helps with ensuring that no publishes beyond expected were received. Getting timeout
        // on waiting for this future means success.
        CompletableFuture<Void> afterCompletionFuture = new CompletableFuture<>();

        int currentPublishCount = 0;
        int desiredPublishCount = 0;
        List<PublishPacket> publishPacketsReceived = new ArrayList<PublishPacket>();
        HashMap<Mqtt5Client, Integer> clientsReceived = new HashMap<Mqtt5Client, Integer>();

        @Override
        public void onMessageReceived(Mqtt5Client client, PublishReturn result) {
            currentPublishCount += 1;
            if (currentPublishCount == desiredPublishCount) {
                publishReceivedFuture.complete(null);
            } else if (currentPublishCount > desiredPublishCount) {
                afterCompletionFuture.completeExceptionally(new Throwable("Too many publish packets received"));
            }

            if (publishPacketsReceived.contains(result)) {
                publishReceivedFuture.completeExceptionally(new Throwable("Duplicate publish packet received!"));
                afterCompletionFuture.completeExceptionally(new Throwable("Too many publish packets received"));
            }
            publishPacketsReceived.add(result.getPublishPacket());

            clientsReceived.merge(client, 1, Integer::sum);
        }
    }
}
