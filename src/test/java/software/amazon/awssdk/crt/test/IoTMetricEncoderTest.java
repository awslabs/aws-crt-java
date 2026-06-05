/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import org.junit.Test;
import static org.junit.Assert.*;

import software.amazon.awssdk.crt.internal.IoTMetricEncoder;
import software.amazon.awssdk.crt.internal.IoTDeviceSDKMetrics;
import software.amazon.awssdk.crt.internal.IoTMetricsMetadata;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.Mqtt5ClientOptionsBuilder;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.ClientSessionBehavior;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.ClientOfflineQueueBehavior;
import software.amazon.awssdk.crt.mqtt5.TopicAliasingOptions;
import software.amazon.awssdk.crt.mqtt5.TopicAliasingOptions.OutboundTopicAliasBehaviorType;
import software.amazon.awssdk.crt.mqtt5.TopicAliasingOptions.InboundTopicAliasBehaviorType;
import software.amazon.awssdk.crt.io.ExponentialBackoffRetryOptions.JitterMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IoTMetricEncoderTest extends CrtTestFixture {

    public IoTMetricEncoderTest() {}

    // ======================== Minimal Options Encoding ========================

    @Test
    public void testMqtt5Minimal() {
        Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder("localhost", 8883L);
        builder.withDisableMetrics(true); // disable so constructor doesn't call encoder
        Mqtt5ClientOptions options = builder.build();

        String result = IoTMetricEncoder.getEncodedFeatureListMqtt5(options);

        // Should only have F (protocol=5) and G (socket)
        assertTrue(result.contains("F/5"));
        assertTrue(result.contains("G/"));
        // Should NOT have A, B, C, D, E since all are DEFAULT
        assertFalse(result.contains("A/"));
        assertFalse(result.contains("B/"));
        assertFalse(result.contains("C/"));
        assertFalse(result.contains("D/"));
        assertFalse(result.contains("E/"));
    }

    @Test
    public void testMqtt3Minimal() {
        String result = IoTMetricEncoder.getEncodedFeatureListMqtt3(null, null);

        assertTrue(result.contains("F/3"));
        assertTrue(result.contains("G/"));
        // Only F and G
        assertEquals(2, result.split(",").length);
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

        String result = IoTMetricEncoder.getEncodedFeatureListMqtt5(options);

        assertTrue(result.contains("A/B")); // Full
        assertTrue(result.contains("B/A")); // CLEAN
        assertTrue(result.contains("C/C")); // FAIL_ALL
        assertTrue(result.contains("D/B")); // LRU
        assertTrue(result.contains("E/A")); // Enabled
        assertTrue(result.contains("F/5")); // MQTT5
    }

    @Test
    public void testDefaultEnumValuesOmitted() {
        Mqtt5ClientOptionsBuilder builder = new Mqtt5ClientOptionsBuilder("localhost", 8883L);
        builder.withSessionBehavior(ClientSessionBehavior.DEFAULT);
        builder.withOfflineQueueBehavior(ClientOfflineQueueBehavior.DEFAULT);
        builder.withRetryJitterMode(JitterMode.Default);
        builder.withDisableMetrics(true);
        Mqtt5ClientOptions options = builder.build();

        String result = IoTMetricEncoder.getEncodedFeatureListMqtt5(options);

        assertFalse(result.contains("A/"));
        assertFalse(result.contains("B/"));
        assertFalse(result.contains("C/"));
    }

    // ======================== Feature List Merging ========================

    @Test
    public void testUserOverridesCrt() {
        String result = IoTMetricEncoder.mergeFeatureLists("A/B,F/5", "A/C");
        assertEquals("A/C,F/5", result);
    }

    @Test
    public void testUserOverridesMultipleCrtFeatures() {
        String result = IoTMetricEncoder.mergeFeatureLists("A/B,F/5,G/A,K/D", "A/C,F/3,K/E");
        assertEquals("A/C,F/3,G/A,K/E", result);
    }

    @Test
    public void testEmptyUserFeatures() {
        String result = IoTMetricEncoder.mergeFeatureLists("F/5,G/A", "");
        assertEquals("F/5,G/A", result);
    }

    @Test
    public void testEmptyCrtFeatures() {
        String result = IoTMetricEncoder.mergeFeatureLists("", "A/B");
        assertEquals("A/B", result);
    }

    @Test
    public void testBothEmpty() {
        String result = IoTMetricEncoder.mergeFeatureLists("", "");
        assertEquals("", result);
    }

    @Test
    public void testDisjointFeatures() {
        String result = IoTMetricEncoder.mergeFeatureLists("F/5,G/A", "I/A,K/D");
        assertEquals("F/5,G/A,I/A,K/D", result);
    }

    @Test
    public void testInsertionOrderPreserved() {
        String result = IoTMetricEncoder.mergeFeatureLists("G/A,F/5", "A/B");
        assertEquals("G/A,F/5,A/B", result);
    }

    // ======================== Create Metrics - Default Options ========================

    @Test
    public void testCreateMetricsNullUserMetrics() {
        IoTDeviceSDKMetrics result = IoTMetricEncoder.createMetricsMqtt3(null, null, null);

        assertEquals("IoTDeviceSDK/Java", result.getLibraryName());
        assertNotNull(result.getMetadataEntries());

        // Should have CRTVersion, IoTSDKFeature, IoTSDKMetricsVersion
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
        IoTDeviceSDKMetrics result = IoTMetricEncoder.createMetricsMqtt3(user, null, null);

        assertEquals("IoTDeviceSDK/Java", result.getLibraryName());
        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
        assertTrue(feature.contains("F/3")); // MQTT3 protocol
    }

    // ======================== Create Metrics - User Features Merged ========================

    @Test
    public void testUserFeatureAddedWhenVersionMatches() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKMetricsVersion", "1"));
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A"));
        user.setMetadataEntries(userEntries);

        IoTDeviceSDKMetrics result = IoTMetricEncoder.createMetricsMqtt3(user, null, null);

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

        IoTDeviceSDKMetrics result = IoTMetricEncoder.createMetricsMqtt3(user, null, null);

        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
        assertTrue(feature.contains("F/3"));
        assertFalse(feature.contains("F/5"));
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

        IoTDeviceSDKMetrics result = IoTMetricEncoder.createMetricsMqtt3(user, null, null);

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

        IoTDeviceSDKMetrics result = IoTMetricEncoder.createMetricsMqtt3(user, null, null);

        String feature = findMetadataValue(result.getMetadataEntries(), "IoTSDKFeature");
        assertFalse(feature.contains("I/A"));
    }

    @Test
    public void testUserFeaturesIgnoredWhenNoVersion() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics();
        List<IoTMetricsMetadata> userEntries = new ArrayList<>();
        userEntries.add(new IoTMetricsMetadata("IoTSDKFeature", "I/A"));
        user.setMetadataEntries(userEntries);

        IoTDeviceSDKMetrics result = IoTMetricEncoder.createMetricsMqtt3(user, null, null);

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

        IoTDeviceSDKMetrics result = IoTMetricEncoder.createMetricsMqtt3(user, null, null);

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

        IoTDeviceSDKMetrics result = IoTMetricEncoder.createMetricsMqtt3(user, null, null);

        String sdkVersion = findMetadataValue(result.getMetadataEntries(), "IoTSDKVersion");
        assertEquals("2.0.0", sdkVersion);
    }

    @Test
    public void testCustomLibraryName() {
        IoTDeviceSDKMetrics user = new IoTDeviceSDKMetrics("MyCustomSDK/1.0", null);
        IoTDeviceSDKMetrics result = IoTMetricEncoder.createMetricsMqtt3(user, null, null);

        assertEquals("MyCustomSDK/1.0", result.getLibraryName());
    }

    @Test
    public void testMetricsVersionAlwaysSet() {
        IoTDeviceSDKMetrics result = IoTMetricEncoder.createMetricsMqtt3(null, null, null);

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
