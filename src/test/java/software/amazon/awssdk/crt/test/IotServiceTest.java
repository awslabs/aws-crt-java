/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR connectionS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import static org.junit.Assert.*;
import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.mqtt.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;

import software.amazon.awssdk.crt.test.MqttConnectionFixture;;

public class IotServiceTest extends MqttConnectionFixture {
    public IotServiceTest() {
    }

    static final String TEST_TOPIC = "sdk/test/java";
    static final String TEST_ENDPOINT = "ajndtdnbudvd1-ats.iot.us-east-2.amazonaws.com";
    static final short TEST_PORT = 8883;
    static final short TEST_PORT_ALPN = 443;
    static final String TEST_CLIENTID = "sdk-java";

    int subsAcked = 0;

    Path pathToCert = null;
    Path pathToKey = null;
    Path pathToCa = null;

    private boolean extractCredentials() {
        try {
            String workingDir = Paths.get("").toAbsolutePath().toString();
            File credentialsDir = new File(Paths.get(workingDir, "src/test/resources/credentials").toString());
            File[] files = credentialsDir.listFiles();
            for (File file: files) {
                String entry = file.getAbsolutePath();
                if (entry.endsWith(".pem.crt")) {
                    pathToCert = Paths.get(entry);
                } else if (entry.endsWith("private.pem.key")) {
                    pathToKey = Paths.get(entry);
                } else if (entry.endsWith("AmazonRootCA1.pem")) {
                    pathToCa = Paths.get(entry);
                }
                if (pathToCert != null && pathToKey != null && pathToCa != null) {
                    return true;
                }
            }
        } catch (Exception ex) {
            System.out.println("Exception thrown during credential resolve: " + ex);
            return false;
        }
        return false;
    }

    @Test
    public void testIotService() throws CrtRuntimeException {
        if (!extractCredentials()) {
            System.out.println("No credentials present, skipping test");
            return;
        }

        short port = TEST_PORT;
        TlsContextOptions tlsOptions = TlsContextOptions.createWithMTLS(pathToCert.toString(), pathToKey.toString());
        tlsOptions.overrideDefaultTrustStore(null, pathToCa.toString());
        if (tlsOptions.isAlpnSupported()) {
            tlsOptions.setAlpnList("x-amzn-mqtt-ca");
            port = TEST_PORT_ALPN;
        }
        TlsContext tls = new TlsContext(tlsOptions);
        
        connect(TEST_ENDPOINT, port, TEST_CLIENTID, true, (short)0, tls);

        Consumer<MqttMessage> messageHandler = (message) -> {};

        try {
            CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QoS.AT_LEAST_ONCE, messageHandler);
            subscribed.thenApply(packetId -> subsAcked++);
            subscribed.get();

            assertEquals("Single subscription", 1, subsAcked);

            CompletableFuture<Integer> unsubscribed = connection.unsubscribe(TEST_TOPIC);
            unsubscribed.thenApply(packetId -> subsAcked--);
            unsubscribed.get();

            assertEquals("No Subscriptions", 0, subsAcked);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
        
        disconnect();       
    }
};
