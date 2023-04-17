/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

 package software.amazon.awssdk.crt.test;

 import org.junit.Assume;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;

 import software.amazon.awssdk.crt.io.TlsContext;
 import software.amazon.awssdk.crt.io.TlsContextOptions;
 import software.amazon.awssdk.crt.mqtt.MqttMessage;
 import software.amazon.awssdk.crt.mqtt.QualityOfService;

 import java.util.UUID;
 import java.util.concurrent.CompletableFuture;
 import java.util.function.*;

 public class IotServiceTest extends MqttClientConnectionFixture {
     public IotServiceTest() {
     }

     static final String TEST_TOPIC = "sdk/test/java/" + UUID.randomUUID().toString();
     int subsAcked = 0;

     @Test
     public void testIotService() {
         skipIfNetworkUnavailable();
         Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_HOST != null);
         Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_RSA_CERT != null);
         Assume.assumeTrue(AWS_TEST_MQTT311_IOT_CORE_RSA_KEY != null);
         Consumer<MqttMessage> messageHandler = (message) -> {};
         int port = 8883;

         try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                 AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
                 AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);)
             {
                 if (TlsContextOptions.isAlpnSupported()) {
                     contextOptions.withAlpnList("x-amzn-mqtt-ca");
                     port = TEST_PORT_ALPN;
                 }
                 try (TlsContext context = new TlsContext(contextOptions);)
                 {
                     connectDirectWithConfig(
                         context,
                         AWS_TEST_MQTT311_IOT_CORE_HOST,
                         port,
                         null,
                         null,
                         null);

                     CompletableFuture<Integer> subscribed = connection.subscribe(TEST_TOPIC, QualityOfService.AT_LEAST_ONCE, messageHandler);
                     subscribed.thenApply(packetId -> subsAcked++);
                     subscribed.get();

                     assertEquals("Single subscription", 1, subsAcked);

                     CompletableFuture<Integer> unsubscribed = connection.unsubscribe(TEST_TOPIC);
                     unsubscribed.thenApply(packetId -> subsAcked--);
                     unsubscribed.get();

                     assertEquals("No Subscriptions", 0, subsAcked);

                     disconnect();
                     close();
                 }
                 catch (Exception ex)
                 {
                     fail(ex.getMessage());
                 }
             }
     }
 };
