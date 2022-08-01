/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package com.example.customkeyops;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.crt.mqtt.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

/**
 * A sample for testing the custom private key operations. See the Java V2 SDK sample for a more in-depth
 * sample with additional options that can be configured via the terminal. This is for testing primarily.
 */
public class CustomKeyOps {
    static String clientId = "test-" + UUID.randomUUID().toString();
    static String caFilePath;
    static String certPath;
    static String keyPath;
    static String endpoint;
    static boolean showHelp = false;
    static int port = 8883;

    static void printUsage() {
        System.out.println(
                "Usage:\n"+
                "  --help            This message\n"+
                "  -e|--endpoint     AWS IoT service endpoint hostname\n"+
                "  -p|--port         Port to connect to on the endpoint\n"+
                "  -r|--ca_file      Path to the root certificate\n"+
                "  -c|--cert         Path to the IoT thing certificate\n"+
                "  -k|--key          Path to the IoT thing private key in PKCS8 format\n"
        );
    }

    static void parseCommandLine(String[] args) {
        for (int idx = 0; idx < args.length; ++idx) {
            switch (args[idx]) {
                case "--help":
                    showHelp = true;
                    break;
                case "-e":
                case "--endpoint":
                    if (idx + 1 < args.length) {
                        endpoint = args[++idx];
                    }
                    break;
                case "-p":
                case "--port":
                    if (idx + 1 < args.length) {
                        port = Integer.parseInt(args[++idx]);
                    }
                    break;
                case "-r":
                case "--ca_file":
                    if (idx + 1 < args.length) {
                        caFilePath = args[++idx];
                    }
                    break;
                case "-c":
                case "--cert":
                    if (idx + 1 < args.length) {
                        certPath = args[++idx];
                    }
                    break;
                case "-k":
                case "--key":
                    if (idx + 1 < args.length) {
                        keyPath = args[++idx];
                    }
                    break;
                default:
                    System.out.println("Unrecognized argument: " + args[idx]);
            }
        }
    }

    static class MyKeyOperationHandler implements TlsKeyOperationHandler {
        RSAPrivateKey key;

        MyKeyOperationHandler(String keyPath) {
            key = loadPrivateKey(keyPath);
        }

        public void performOperation(TlsKeyOperation operation) {
            try {
                System.out.println("MyKeyOperationHandler.performOperation" + operation.getType().name());

                if (operation.getType() != TlsKeyOperation.Type.SIGN) {
                    throw new RuntimeException("Simple sample only handles SIGN operations");
                }

                if (operation.getSignatureAlgorithm() != TlsSignatureAlgorithm.RSA) {
                    throw new RuntimeException("Simple sample only handles RSA keys");
                }

                if (operation.getDigestAlgorithm() != TlsHashAlgorithm.SHA256) {
                    throw new RuntimeException("Simple sample only handles SHA256 digests");
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

            } catch (Exception ex) {
                System.out.println("Error during key operation:" + ex);
                operation.completeExceptionally(ex);
            }
        }

        RSAPrivateKey loadPrivateKey(String filepath) {
            /* Adapted from: https://stackoverflow.com/a/27621696
             * You probably need to convert your private key file from PKCS#1
             * to PKCS#8 to get it working with this sample:
             *
             * $ openssl pkcs8 -topk8 -in my-private.pem.key -out my-private-pk8.pem.key -nocrypt
             *
             * IoT Core vends keys as PKCS#1 by default,
             * but Java only seems to have this PKCS8EncodedKeySpec class */
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
    }

    public static void main(String[] args) {

        parseCommandLine(args);
        if (showHelp || endpoint == null || certPath == null || keyPath == null) {
            printUsage();
            return;
        }

        MqttClientConnectionEvents callbacks = new MqttClientConnectionEvents() {
            @Override
            public void onConnectionInterrupted(int errorCode) {
                if (errorCode != 0) {
                    System.out.println("Connection interrupted: " + errorCode + ": " + CRT.awsErrorString(errorCode));
                }
            }

            @Override
            public void onConnectionResumed(boolean sessionPresent) {
                System.out.println("Connection resumed: " + (sessionPresent ? "existing session" : "clean session"));
            }
        };

        MyKeyOperationHandler myKeyOperationHandler = new MyKeyOperationHandler(keyPath);
        TlsContextCustomKeyOperationOptions keyOperationOptions = new TlsContextCustomKeyOperationOptions(myKeyOperationHandler)
                .withCertificateFilePath(certPath);

        try {

            // ========================================================================================================
            // Connection building part of sample
            EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
            HostResolver resolver = new HostResolver(eventLoopGroup);
            ClientBootstrap clientBootstrap = new ClientBootstrap(eventLoopGroup, resolver);

            // Build a connection similar to how it is built in SDK
            TlsContextOptions tlsOptions = TlsContextOptions.createWithMtlsCustomKeyOperations(keyOperationOptions);
            MqttConnectionConfig config = new MqttConnectionConfig();
            // Set all the settings
            tlsOptions.overrideDefaultTrustStoreFromPath(null, caFilePath);
            config.setEndpoint(endpoint);
            config.setPort((short)port);
            config.setClientId(clientId);
            config.setCleanSession(true);
            config.setProtocolOperationTimeoutMs(60000);
            config.setConnectionCallbacks(callbacks);
            // Build the client and connection
            ClientTlsContext clientTlsContext = new ClientTlsContext(tlsOptions);
            MqttClient client = new MqttClient(clientBootstrap, clientTlsContext);
            config.setMqttClient(client);
            config.setUsername("?SDK=JavaV2&Version=1.0.0-SNAPSHOT");
            MqttClientConnection connection = new MqttClientConnection(config);

            // ========================================================================================================
            // Connection/Disconnection part of sample:

            CompletableFuture<Boolean> connected = connection.connect();
            try {
                boolean sessionPresent = connected.get();
                System.out.println("Connected to " + (!sessionPresent ? "new" : "existing") + " session!");
            } catch (Exception ex) {
                // Comment this out to test MyKeyOperationHandler exception routes.
                throw new RuntimeException("Exception occurred during connect", ex);
            }

            CompletableFuture<Void> disconnected = connection.disconnect();
            disconnected.get();

            // ========================================================================================================
            // Close all the stuff
            connection.close();
            client.close();
            config.close();
            clientTlsContext.close();
            tlsOptions.close();
            clientBootstrap.close();
            resolver.close();
            eventLoopGroup.close();

        } catch (CrtRuntimeException | InterruptedException | ExecutionException ex) {
            throw new RuntimeException("CustomKeyOpsPubSub execution failure", ex);
        }

        CrtResource.waitForNoResources();
        System.out.println("Complete!");
    }
}
