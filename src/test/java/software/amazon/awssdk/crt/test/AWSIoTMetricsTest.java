/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import org.junit.Test;
import static org.junit.Assert.*;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.iot.AWSIoTMetrics;
import software.amazon.awssdk.crt.iot.IoTMetricsMetadata;
import software.amazon.awssdk.crt.mqtt.MqttClient;
import software.amazon.awssdk.crt.mqtt.MqttConnectionConfig;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.Mqtt5ClientOptionsBuilder;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.ClientSessionBehavior;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.ClientOfflineQueueBehavior;
import software.amazon.awssdk.crt.mqtt5.TopicAliasingOptions;
import software.amazon.awssdk.crt.mqtt5.TopicAliasingOptions.OutboundTopicAliasBehaviorType;
import software.amazon.awssdk.crt.mqtt5.TopicAliasingOptions.InboundTopicAliasBehaviorType;
import software.amazon.awssdk.crt.io.ExponentialBackoffRetryOptions.JitterMode;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;

import java.util.ArrayList;
import java.util.List;

public class AWSIoTMetricsTest extends CrtTestFixture {

    public AWSIoTMetricsTest() {}

    private MqttConnectionConfig createMqtt3Config(AWSIoTMetrics userMetrics) {
        MqttConnectionConfig config = new MqttConnectionConfig();
        if (userMetrics != null) {
            config.setMetrics(userMetrics);
        }
        return config;
    }

    // ======================== Minimal Options Encoding ========================

    @Test
    public void testMqtt5Minimal() {
        Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder("localhost", 8883L);
        builder.withDisableMetrics(true);
        Mqtt5ClientOptions options = builder.build();

        AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt5(options);
        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

        assertTrue(feature.contains("F/5"));
        assertTrue(feature.contains("G/"));
        assertFalse(feature.contains("A/"));
        assertFalse(feature.contains("B/"));
        assertFalse(feature.contains("C/"));
        assertFalse(feature.contains("D/"));
        assertFalse(feature.contains("E/"));
    }

    @Test
    public void testMqtt3Minimal() {
        try (MqttConnectionConfig config = createMqtt3Config(null)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);
            String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

            assertTrue(feature.contains("F/3"));
            assertTrue(feature.contains("G/"));
        }
    }

    // ======================== Non-Default Features Encoding ========================

    @Test
    public void testMqtt5WithNonDefaultFeatures() {
        TopicAliasingOptions topicAliasing = new TopicAliasingOptions()
            .withOutboundBehavior(OutboundTopicAliasBehaviorType.LRU)
            .withInboundBehavior(InboundTopicAliasBehaviorType.Enabled);

        Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder("localhost", 8883L);
        builder.withSessionBehavior(ClientSessionBehavior.CLEAN);
        builder.withOfflineQueueBehavior(ClientOfflineQueueBehavior.FAIL_ALL_ON_DISCONNECT);
        builder.withRetryJitterMode(JitterMode.Full);
        builder.withTopicAliasingOptions(topicAliasing);
        builder.withDisableMetrics(true);
        Mqtt5ClientOptions options = builder.build();

        AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt5(options);
        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

        assertTrue(feature.contains("A/B")); // Full
        assertTrue(feature.contains("B/A")); // CLEAN
        assertTrue(feature.contains("C/C")); // FAIL_ALL
        assertTrue(feature.contains("D/B")); // LRU
        assertTrue(feature.contains("E/A")); // Enabled
        assertTrue(feature.contains("F/5")); // MQTT5
    }

    // ======================== Feature Merging ========================

    @Test
    public void testUserOverridesCrt() {
        AWSIoTMetrics user = new AWSIoTMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "1"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "F/9"));
        user.setMetadataEntries(userEntries);

        try (MqttConnectionConfig config = createMqtt3Config(user)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);
            String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

            assertTrue(feature.contains("F/9"));
            assertFalse(feature.contains("F/3"));
        }
    }

    @Test
    public void testDisjointFeaturesAreMerged() {
        AWSIoTMetrics user = new AWSIoTMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "1"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A,K/D"));
        user.setMetadataEntries(userEntries);

        try (MqttConnectionConfig config = createMqtt3Config(user)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);
            String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

            assertTrue(feature.contains("F/3"));
            assertTrue(feature.contains("G/"));
            assertTrue(feature.contains("I/A"));
            assertTrue(feature.contains("K/D"));
        }
    }

    // ======================== Create Metrics - Default Options ========================

    @Test
    public void testCreateMetricsNullUserMetrics() {
        try (MqttConnectionConfig config = createMqtt3Config(null)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);

            assertEquals(getExpectedDefaultLibraryName(), result.getLibraryName());
            assertNotNull(result.getMetadataEntries());

            List<IoTMetricsMetadata> entries = result.getMetadataEntries();
            String crtVersion = findMetadataValue(entries, "CRTVersion");
            String feature = findMetadataValue(entries, "IoTSDKFeature");
            String metricsVersion = findMetadataValue(entries, "IoTSDKMetricsVersion");

            assertNotNull(crtVersion);
            assertNotNull(feature);
            assertEquals("1", metricsVersion);
        }
    }

    @Test
    public void testCreateMetricsEmptyUserMetrics() {
        AWSIoTMetrics user = new AWSIoTMetrics();
        try (MqttConnectionConfig config = createMqtt3Config(user)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);

            assertEquals(getExpectedDefaultLibraryName(), result.getLibraryName());
            String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
            assertTrue(feature.contains("F/3"));
        }
    }

    // ======================== Create Metrics - User Features Merged ========================

    @Test
    public void testUserFeatureAddedWhenVersionMatches() {
        AWSIoTMetrics user = new AWSIoTMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "1"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A"));
        user.setMetadataEntries(userEntries);

        try (MqttConnectionConfig config = createMqtt3Config(user)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);

            String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
            assertTrue(feature.contains("I/A"));
            assertTrue(feature.contains("F/3"));
            assertTrue(feature.contains("G/"));
        }
    }

    // ======================== Create Metrics - Version Mismatch ========================

    @Test
    public void testUserFeaturesIgnoredOnHigherVersion() {
        AWSIoTMetrics user = new AWSIoTMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "99"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A"));
        user.setMetadataEntries(userEntries);

        try (MqttConnectionConfig config = createMqtt3Config(user)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);

            String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
            assertFalse(feature.contains("I/A"));
            assertTrue(feature.contains("F/3"));
        }
    }

    @Test
    public void testUserFeaturesIgnoredOnNonNumericVersion() {
        AWSIoTMetrics user = new AWSIoTMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "abc"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A"));
        user.setMetadataEntries(userEntries);

        try (MqttConnectionConfig config = createMqtt3Config(user)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);

            String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
            assertFalse(feature.contains("I/A"));
        }
    }

    @Test
    public void testUserFeaturesIgnoredWhenNoVersion() {
        AWSIoTMetrics user = new AWSIoTMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A"));
        user.setMetadataEntries(userEntries);

        try (MqttConnectionConfig config = createMqtt3Config(user)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);

            String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
            assertFalse(feature.contains("I/A"));
        }
    }

    // ======================== CRTVersion Not Overridable ========================

    @Test
    public void testCrtVersionCannotBeOverridden() {
        AWSIoTMetrics user = new AWSIoTMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("CRTVersion", "fake_version"));
        user.setMetadataEntries(userEntries);

        try (MqttConnectionConfig config = createMqtt3Config(user)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);

            String crtVersion = findMetadataValue(result.getMetadataEntries(), "CRTVersion");
            assertNotEquals("fake_version", crtVersion);
        }
    }

    // ======================== Other User Metadata Preserved ========================

    @Test
    public void testSdkVersionPreserved() {
        AWSIoTMetrics user = new AWSIoTMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKVersion", "2.0.0"));
        user.setMetadataEntries(userEntries);

        try (MqttConnectionConfig config = createMqtt3Config(user)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);

            String sdkVersion = findMetadataValue(result.getMetadataEntries(), "IoTSDKVersion");
            assertEquals("2.0.0", sdkVersion);
        }
    }

    @Test
    public void testCustomLibraryName() {
        AWSIoTMetrics user = new AWSIoTMetrics("MyCustomSDK/1.0", null);
        try (MqttConnectionConfig config = createMqtt3Config(user)) {
            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);

            assertEquals("MyCustomSDK/1.0", result.getLibraryName());
        }
    }

    // ======================== Certificate Source ========================

    @Test
    public void testCertificateSourceInMqtt5Features() {
        try (TlsContextOptions tlsOptions =
                 TlsContextOptions.createWithMtls(TlsContextOptionsTest.TEST_CERT, TlsContextOptionsTest.TEST_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions)) {

            Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder("localhost", 8883L);
            builder.withTlsContext(tlsContext);
            builder.withDisableMetrics(true);
            Mqtt5ClientOptions options = builder.build();

            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt5(options);
            String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

            assertTrue(feature.contains("I/A"));
            assertTrue(feature.contains("F/5"));
        }
    }

    @Test
    public void testCertificateSourceInMqtt3Features() {
        try (TlsContextOptions tlsOptions =
                 TlsContextOptions.createWithMtls(TlsContextOptionsTest.TEST_CERT, TlsContextOptionsTest.TEST_KEY);
             TlsContext tlsContext = new TlsContext(tlsOptions);
             MqttClient client = new MqttClient(tlsContext);
             MqttConnectionConfig config = new MqttConnectionConfig()) {
            config.setMqttClient(client);

            AWSIoTMetrics result = AWSIoTMetrics.createMetricsMqtt3(config);
            String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

            assertTrue(feature.contains("I/A"));
            assertTrue(feature.contains("F/3"));
        }
    }

    // ======================== Helper ========================

    /**
     * Returns the library name that {@link AWSIoTMetrics} should produce
     * by default on the current platform.
     */
    private static String getExpectedDefaultLibraryName() {
        try {
            return "android".equals(CRT.getOSIdentifier())
                    ? "IoTDeviceSDK/Android"
                    : "IoTDeviceSDK/Java";
        } catch (Exception e) {
            return "IoTDeviceSDK/Java";
        }
    }

    private String findMetadataValue(List<IoTMetricsMetadata> entries, String key) {
        for (IoTMetricsMetadata entry : entries) {
            if (key.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
