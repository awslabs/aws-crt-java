/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;

import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.crt.mqtt5.Mqtt5Client;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.LifecycleEvents;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.Mqtt5ClientOptionsBuilder;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


/* For environment variable setup, see SetupCrossCICrtEnvironment in the CRT builder */
public class CustomKeyOpsTest extends MqttClientConnectionFixture {
    public CustomKeyOpsTest() {
    }

    static RSAPrivateKey loadPrivateKey(String filepath) {
        /* Adapted from: https://stackoverflow.com/a/27621696
         * NOTE - this only works with PKCS#8 keys. See sample for more info */
        try {
            /* Read the BASE64-encoded contents of the private key file */
            StringBuilder pemBase64 = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Strip off PEM header and footer
                    if (line.startsWith("---")) {
                        if (line.contains("RSA")) {
                            throw new RuntimeException("private key must be converted from PKCS#1 to PKCS#8");
                        }
                        continue;
                    }
                    pemBase64.append(line);
                }
            }

            String pemBase64String = pemBase64.toString();
            byte[] der = Base64.getDecoder().decode(pemBase64String);

            /* Create PrivateKey instance */
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(der);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            return (RSAPrivateKey)privateKey;

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static class TestKeyOperationHandler implements TlsKeyOperationHandler {
        RSAPrivateKey key;
        boolean throwException;
        boolean performExtraComplete;

        TestKeyOperationHandler(String keyPath, boolean throwException, boolean performExtraComplete) {
            this.key = loadPrivateKey(keyPath);
            this.throwException = throwException;
            this.performExtraComplete = performExtraComplete;
        }

        public void performOperation(TlsKeyOperation operation) {
            if (this.throwException == true) {
                throw new RuntimeException("Test Exception!");
            }

            try {
                if (operation.getType() != TlsKeyOperation.Type.SIGN) {
                    throw new RuntimeException("Test only handles SIGN operations");
                }
                if (operation.getSignatureAlgorithm() != TlsSignatureAlgorithm.RSA) {
                    throw new RuntimeException("Test only handles RSA keys");
                }
                if (operation.getDigestAlgorithm() != TlsHashAlgorithm.SHA256) {
                    throw new RuntimeException("Test only handles SHA256 digests");
                }

                // A SIGN operation's inputData is the 32bytes of the SHA-256 digest.
                // Before doing the RSA signature, we need to construct a PKCS1 v1.5 DigestInfo.
                // See https://datatracker.ietf.org/doc/html/rfc3447#section-9.2
                byte[] digest = operation.getInput();

                // These are the appropriate bytes for the SHA-256 AlgorithmIdentifier:
                // https://tools.ietf.org/html/rfc3447#page-43
                byte[] sha256DigestAlgorithm = { 0x30, 0x31, 0x30, 0x0d, 0x06, 0x09, 0x60, (byte)0x86, 0x48, 0x01,
                        0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00, 0x04, 0x20 };

                ByteArrayOutputStream digestInfoStream = new ByteArrayOutputStream();
                digestInfoStream.write(sha256DigestAlgorithm);
                digestInfoStream.write(digest);
                byte[] digestInfo = digestInfoStream.toByteArray();

                // Sign the DigestInfo
                Signature rsaSign = Signature.getInstance("NONEwithRSA");
                rsaSign.initSign(key);
                rsaSign.update(digestInfo);
                byte[] signatureBytes = rsaSign.sign();

                operation.complete(signatureBytes);

                if (this.performExtraComplete == true) {
                    operation.complete(signatureBytes);
                }

            } catch (Exception ex) {
                operation.completeExceptionally(ex);

                if (this.performExtraComplete == true) {
                    operation.completeExceptionally(ex);
                }
            }
        }
    }

    @Test
    public void testHappyPath()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT,
            AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY);
        TestKeyOperationHandler myKeyOperationHandler = new TestKeyOperationHandler(AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY, false, false);
        TlsContextCustomKeyOperationOptions keyOperationOptions = new TlsContextCustomKeyOperationOptions(myKeyOperationHandler);
        keyOperationOptions.withCertificateFilePath(AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT);

        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsCustomKeyOperations(keyOperationOptions);
                TlsContext context = new TlsContext(contextOptions);)
            {
                connectDirectWithConfig(
                    context,
                    AWS_TEST_MQTT311_IOT_CORE_HOST,
                    8883,
                    null,
                    null,
                    null);
                disconnect();
                close();
            }
    }

    // Not ideal, but I don't see the point of making a new file just for a single MQTT5 Custom Key Ops test.
    static final class LifecycleEvents_Futured implements LifecycleEvents {
        CompletableFuture<Void> connectedFuture = new CompletableFuture<>();
        @Override
        public void onAttemptingConnect(Mqtt5Client client, software.amazon.awssdk.crt.mqtt5.OnAttemptingConnectReturn onAttemptingConnectReturn) {}
        @Override
        public void onConnectionSuccess(Mqtt5Client client, software.amazon.awssdk.crt.mqtt5.OnConnectionSuccessReturn onConnectionSuccessReturn) {
            connectedFuture.complete(null);
        }
        @Override
        public void onConnectionFailure(Mqtt5Client client, software.amazon.awssdk.crt.mqtt5.OnConnectionFailureReturn onConnectionFailureReturn) {
            connectedFuture.completeExceptionally(new Exception("Could not connect!"));
        }
        @Override
        public void onDisconnection(Mqtt5Client client, software.amazon.awssdk.crt.mqtt5.OnDisconnectionReturn onDisconnectionReturn) {}
        @Override
        public void onStopped(Mqtt5Client client, software.amazon.awssdk.crt.mqtt5.OnStoppedReturn onStoppedReturn) {}
    }

    @Test
    public void testHappyPathMQTT5() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT5_IOT_CORE_HOST, AWS_TEST_MQTT5_CUSTOM_KEY_OPS_CERT,
            AWS_TEST_MQTT5_CUSTOM_KEY_OPS_KEY);
        try {
            TestKeyOperationHandler myKeyOperationHandler = new TestKeyOperationHandler(AWS_TEST_MQTT5_CUSTOM_KEY_OPS_KEY, false, false);
            TlsContextCustomKeyOperationOptions keyOperationOptions = new TlsContextCustomKeyOperationOptions(myKeyOperationHandler);
            keyOperationOptions.withCertificateFilePath(AWS_TEST_MQTT5_CUSTOM_KEY_OPS_CERT);

            LifecycleEvents_Futured events = new LifecycleEvents_Futured();
            try (
                TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsCustomKeyOperations(keyOperationOptions);
                TlsContext context = new TlsContext(contextOptions);
            ) {
                Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder(AWS_TEST_MQTT5_IOT_CORE_HOST, 8883l);
                builder.withLifecycleEvents(events);
                builder.withTlsContext(context);

                try (Mqtt5Client client = new Mqtt5Client(builder.build())) {
                    client.start();
                    events.connectedFuture.get(180, TimeUnit.SECONDS);
                    client.stop(null);
                }
            }
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testExceptionFailurePath()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT,
            AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY);
        TestKeyOperationHandler myKeyOperationHandler = new TestKeyOperationHandler(AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY, true, false);
        TlsContextCustomKeyOperationOptions keyOperationOptions = new TlsContextCustomKeyOperationOptions(myKeyOperationHandler);
        keyOperationOptions.withCertificateFilePath(AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT);

        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsCustomKeyOperations(keyOperationOptions);
            TlsContext context = new TlsContext(contextOptions);)
        {
            connectDirectWithConfigThrows(
                context,
                AWS_TEST_MQTT311_IOT_CORE_HOST,
                8883,
                null,
                null,
                null);
        }
        catch (Exception ex) {
            close();
            return;
        }
        close();
        fail("Connection did not fail!");
    }

    @Test
    public void testExtraCompleteHappy() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT,
            AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY);
        TestKeyOperationHandler myKeyOperationHandler = new TestKeyOperationHandler(AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY, false, true);
        TlsContextCustomKeyOperationOptions keyOperationOptions = new TlsContextCustomKeyOperationOptions(myKeyOperationHandler);
        keyOperationOptions.withCertificateFilePath(AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT);

        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsCustomKeyOperations(keyOperationOptions);
            TlsContext context = new TlsContext(contextOptions);)
        {
            connectDirectWithConfig(
                context,
                AWS_TEST_MQTT311_IOT_CORE_HOST,
                8883,
                null,
                null,
                null);
            disconnect();
            close();
        }
        catch (Exception ex) {
            fail("Exception during connect: " + ex.toString());
        }
        disconnect();
        close();
    }

    @Test
    public void testExceptionExtraCompleteFailurePath() {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT,
            AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY);
        TestKeyOperationHandler myKeyOperationHandler = new TestKeyOperationHandler(AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY, true, true);
        TlsContextCustomKeyOperationOptions keyOperationOptions = new TlsContextCustomKeyOperationOptions(myKeyOperationHandler);
        keyOperationOptions.withCertificateFilePath(AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT);

        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsCustomKeyOperations(keyOperationOptions);
            TlsContext context = new TlsContext(contextOptions);)
        {
            connectDirectWithConfigThrows(
                context,
                AWS_TEST_MQTT311_IOT_CORE_HOST,
                8883,
                null,
                null,
                null);
        }
        catch (Exception ex) {
            close();
            return;
        }
        close();
        fail("Connection did not fail!");
    }
};
