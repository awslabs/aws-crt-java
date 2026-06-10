/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import org.junit.Test;
import static org.junit.Assert.*;

import software.amazon.awssdk.crt.iot.IoTDeviceSDKMetrics;
import software.amazon.awssdk.crt.iot.IoTMetricsMetadata;
import software.amazon.awssdk.crt.mqtt.MqttConnectionConfig;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.Mqtt5ClientOptionsBuilder;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.ClientSessionBehavior;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.ClientOfflineQueueBehavior;
import software.amazon.awssdk.crt.mqtt5.TopicAliasingOptions;
import software.amazon.awssdk.crt.mqtt5.TopicAliasingOptions.OutboundTopicAliasBehaviorType;
import software.amazon.awssdk.crt.mqtt5.TopicAliasingOptions.InboundTopicAliasBehaviorType;
import software.amazon.awssdk.crt.io.ExponentialBackoffRetryOptions.JitterMode;

import java.util.ArrayList;
import java.util.List;

public class IoTMetricEncoderTest extends CrtTestFixture {

    public IoTMetricEncoderTest() {}

    private MqttConnectionConfig createMqtt3Config(IoTDeviceSDKMetrics userMetrics) {
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

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt5(options);
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
        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(null));
        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

        assertTrue(feature.contains("F/3"));
        assertTrue(feature.contains("G/"));
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

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt5(options);
        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

        assertTrue(feature.contains("A/B")); // Full
        assertTrue(feature.contains("B/A")); // CLEAN
        assertTrue(feature.contains("C/C")); // FAIL_ALL
        assertTrue(feature.contains("D/B")); // LRU
        assertTrue(feature.contains("E/A")); // Enabled
        assertTrue(feature.contains("F/5")); // MQTT5
    }

    @Test
    public void testDefaultEnumValuesOmitted() {
        Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder("localhost", 8883L);
        builder.withSessionBehavior(ClientSessionBehavior.DEFAULT);
        builder.withOfflineQueueBehavior(ClientOfflineQueueBehavior.DEFAULT);
        builder.withRetryJitterMode(JitterMode.Default);
        builder.withDisableMetrics(true);
        Mqtt5ClientOptions options = builder.build();

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt5(options);
        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

        assertFalse(feature.contains("A/"));
        assertFalse(feature.contains("B/"));
        assertFalse(feature.contains("C/"));
    }

    // ======================== Feature Merging ========================

    @Test
    public void testUserOverridesCrt() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "1"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "F/9"));
        user.setMetadataEntries(userEntries);

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(user));
        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

        assertTrue(feature.contains("F/9"));
        assertFalse(feature.contains("F/3"));
    }

    @Test
    public void testDisjointFeaturesAreMerged() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "1"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A,K/D"));
        user.setMetadataEntries(userEntries);

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(user));
        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");

        assertTrue(feature.contains("F/3"));
        assertTrue(feature.contains("G/"));
        assertTrue(feature.contains("I/A"));
        assertTrue(feature.contains("K/D"));
    }

    // ======================== Create Metrics - Default Options ========================

    @Test
    public void testCreateMetricsNullUserMetrics() {
        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(null));

        assertEquals("IoTDeviceSDK/Java", result.getLibraryName());
        assertNotNull(result.getMetadataEntries());

        List<IoTMetricsMetadata> entries = result.getMetadataEntries();
        String crtVersion = findMetadataValue(entries, "CRTVersion");
        String feature = findMetadataValue(entries, "IoTSDKFeature");
        String metricsVersion = findMetadataValue(entries, "IoTSDKMetricsVersion");

        assertNotNull(crtVersion);
        assertNotNull(feature);
        assertEquals("1", metricsVersion);
    }

    @Test
    public void testCreateMetricsEmptyUserMetrics() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(user));

        assertEquals("IoTDeviceSDK/Java", result.getLibraryName());
        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
        assertTrue(feature.contains("F/3"));
    }

    // ======================== Create Metrics - User Features Merged ========================

    @Test
    public void testUserFeatureAddedWhenVersionMatches() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "1"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A"));
        user.setMetadataEntries(userEntries);

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(user));

        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
        assertTrue(feature.contains("I/A"));
        assertTrue(feature.contains("F/3"));
        assertTrue(feature.contains("G/"));
    }

    @Test
    public void testUserFeatureOverridesCrt() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "1"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "F/3,I/B"));
        user.setMetadataEntries(userEntries);

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(user));

        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
        assertTrue(feature.contains("F/3"));
        assertTrue(feature.contains("I/B"));
    }

    // ======================== Create Metrics - Version Mismatch ========================

    @Test
    public void testUserFeaturesIgnoredOnHigherVersion() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "99"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A"));
        user.setMetadataEntries(userEntries);

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(user));

        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
        assertFalse(feature.contains("I/A"));
        assertTrue(feature.contains("F/3"));
    }

    @Test
    public void testUserFeaturesIgnoredOnNonNumericVersion() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "abc"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A"));
        user.setMetadataEntries(userEntries);

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(user));

        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
        assertFalse(feature.contains("I/A"));
    }

    @Test
    public void testUserFeaturesIgnoredWhenNoVersion() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A"));
        user.setMetadataEntries(userEntries);

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(user));

        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
        assertFalse(feature.contains("I/A"));
    }

    // ======================== CRTVersion Not Overridable ========================

    @Test
    public void testCrtVersionCannotBeOverridden() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("CRTVersion", "fake_version"));
        user.setMetadataEntries(userEntries);

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(user));

        String crtVersion = findMetadataValue(result.getMetadataEntries(), "CRTVersion");
        assertNotEquals("fake_version", crtVersion);
    }

    // ======================== Other User Metadata Preserved ========================

    @Test
    public void testSdkVersionPreserved() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKVersion", "2.0.0"));
        user.setMetadataEntries(userEntries);

        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(user));

        String sdkVersion = findMetadataValue(result.getMetadataEntries(), "IoTSDKVersion");
        assertEquals("2.0.0", sdkVersion);
    }

    @Test
    public void testCustomLibraryName() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics("MyCustomSDK/1.0", null);
        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(user));

        assertEquals("MyCustomSDK/1.0", result.getLibraryName());
    }

    @Test
    public void testMetricsVersionAlwaysSet() {
        IoTDeviceSDKMetrics result = IoTDeviceSDKMetrics.createMetricsMqtt3(createMqtt3Config(null));

        String metricsVersion = findMetadataValue(result.getMetadataEntries(), "IoTSDKMetricsVersion");
        assertEquals("1", metricsVersion);
    }

    // ======================== Helper ========================

    private String findMetadataValue(List<IoTMetricsMetadata> entries, String key) {
        for (IoTMetricsMetadata entry : entries) {
            if (key.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
