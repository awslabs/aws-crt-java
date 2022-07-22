/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.CrtResource;

import static org.junit.Assert.*;

import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.io.*;
import software.amazon.awssdk.crt.mqtt.*;
import software.amazon.awssdk.crt.mqtt.MqttException;

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


public class CustomKeyOpsTest extends CustomKeyOpsFixture {
    public CustomKeyOpsTest() {
    }

    static RSAPrivateKey loadPrivateKey(String filepath) {
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

    static class TestKeyOperationHandler implements TlsKeyOperationHandler.TlsKeyOperationHandlerEvents {
        RSAPrivateKey key;
        boolean throwException;
        boolean performExtraCloses;
        boolean cleanupCalled;

        TestKeyOperationHandler(String keyPath, boolean throwException, boolean performExtraCloses) {
            this.key = loadPrivateKey(keyPath);
            this.throwException = throwException;
            this.performExtraCloses = performExtraCloses;
            this.cleanupCalled = false;
        }

        public void performOperation(TlsKeyOperation operation) {
            if (this.throwException == true) {
                throw new RuntimeException("Test Exception!");
            }

            try {
                if (this.performExtraCloses == true) {
                    operation.close();
                }

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

                if (this.performExtraCloses == true) {
                    operation.close();
                }

            } catch (Exception ex) {
                if (this.performExtraCloses == true) {
                    operation.close();
                }

                operation.completeExceptionally(ex);

                if (this.performExtraCloses == true) {
                    operation.close();
                }
            }
        }
        public void onCleanup() {
            this.cleanupCalled = true;
        }
    }

    @Test
    public void testHappyPath() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(TEST_PRIVATEKEY != null && TEST_PRIVATEKEY != "");

        TestKeyOperationHandler myKeyOperationHandler = new TestKeyOperationHandler(TEST_PRIVATEKEY, false, false);
        TlsKeyOperationHandler keyOperationHandler = new TlsKeyOperationHandler(myKeyOperationHandler);
        TlsContextCustomKeyOperationOptions keyOperationOptions = new TlsContextCustomKeyOperationOptions(keyOperationHandler);
        System.out.println("\n ABOUT TO CONNECT... \n");
        try {
            connect(keyOperationOptions);
        }
        catch (Exception ex) {
            fail("Exception during connect: " + ex.toString());
        }
        disconnect();
        close();

        if (myKeyOperationHandler.cleanupCalled != true) {
            fail("Cleanup function was not called!");
        }
    }

    @Test
    public void testExceptionFailurePath() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(TEST_PRIVATEKEY != null && TEST_PRIVATEKEY != "");

        TestKeyOperationHandler myKeyOperationHandler = new TestKeyOperationHandler(TEST_PRIVATEKEY, true, false);
        TlsKeyOperationHandler keyOperationHandler = new TlsKeyOperationHandler(myKeyOperationHandler);
        TlsContextCustomKeyOperationOptions keyOperationOptions = new TlsContextCustomKeyOperationOptions(keyOperationHandler);
        try {
            connect(keyOperationOptions);
        }
        catch (Exception ex) {
            close();
            return;
        }
        close();
        fail("Connection did not fail!");
    }

    @Test
    public void testExtraClosesHappy() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(TEST_PRIVATEKEY != null && TEST_PRIVATEKEY != "");

        TestKeyOperationHandler myKeyOperationHandler = new TestKeyOperationHandler(TEST_PRIVATEKEY, false, true);
        TlsKeyOperationHandler keyOperationHandler = new TlsKeyOperationHandler(myKeyOperationHandler);
        TlsContextCustomKeyOperationOptions keyOperationOptions = new TlsContextCustomKeyOperationOptions(keyOperationHandler);
        try {
            connect(keyOperationOptions);
        }
        catch (Exception ex) {
            fail("Exception during connect: " + ex.toString());
        }
        disconnect();
        close();
    }

    @Test
    public void testExceptionExtraClosesFailurePath() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(TEST_PRIVATEKEY != null && TEST_PRIVATEKEY != "");

        TestKeyOperationHandler myKeyOperationHandler = new TestKeyOperationHandler(TEST_PRIVATEKEY, true, true);
        TlsKeyOperationHandler keyOperationHandler = new TlsKeyOperationHandler(myKeyOperationHandler);
        TlsContextCustomKeyOperationOptions keyOperationOptions = new TlsContextCustomKeyOperationOptions(keyOperationHandler);
        try {
            connect(keyOperationOptions);
        }
        catch (Exception ex) {
            close();
            return;
        }
        close();
        fail("Connection did not fail!");
    }
};
