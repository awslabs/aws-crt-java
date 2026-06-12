/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.iot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.ExponentialBackoffRetryOptions.JitterMode;
import software.amazon.awssdk.crt.io.TlsCipherPreference;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions;
import software.amazon.awssdk.crt.mqtt5.TopicAliasingOptions;
import software.amazon.awssdk.crt.mqtt.MqttConnectionConfig;
import software.amazon.awssdk.crt.utils.PackageInfo;

/**
 * IoT Device SDK Metrics configuration.
 * Holds library identification and metadata entries that are appended
 * to the MQTT CONNECT packet username field.
 */
public class IoTDeviceSDKMetrics {
    private String libraryName;
    private List<IoTMetricsMetadata> metadataEntries;

    // Feature ID constants
    private static final String RETRY_JITTER_MODE = "A";
    private static final String SESSION_BEHAVIOR = "B";
    private static final String OFFLINE_QUEUE_BEHAVIOR = "C";
    private static final String OUTBOUND_TOPIC_ALIAS_BEHAVIOR = "D";
    private static final String INBOUND_TOPIC_ALIAS_BEHAVIOR = "E";
    private static final String PROTOCOL_VERSION = "F";
    private static final String SOCKET_IMPLEMENTATION = "G";
    private static final String HTTP_PROXY_TYPE = "H";
    private static final String CERTIFICATE_SOURCE = "I";
    private static final String TLS_CIPHER_PREFERENCE = "J";
    private static final String MINIMUM_TLS_VERSION = "K";

    public static final int IOT_SDK_METRICS_FEATURE_VERSION = 1;

    public IoTDeviceSDKMetrics() {
        this.libraryName = "IoTDeviceSDK/Java";
        this.metadataEntries = new ArrayList<>();
    }

    public IoTDeviceSDKMetrics(String libraryName, List<IoTMetricsMetadata> metadataEntries) {
        this.libraryName = libraryName;
        this.metadataEntries = metadataEntries;
    }

    public String getLibraryName() { return libraryName; }

    public void setLibraryName(String libraryName) { this.libraryName = libraryName; }

    public List<IoTMetricsMetadata> getMetadataEntries() { return metadataEntries; }

    public void setMetadataEntries(List<IoTMetricsMetadata> metadataEntries) { this.metadataEntries = metadataEntries; }

    /**
     * Builds the final metrics object for an MQTT5 client by encoding the CRT
     * feature list from {@code clientOptions} and merging it with any
     * user-supplied metadata.
     *
     * @param clientOptions MQTT5 client options containing connection configuration and user metrics
     * @return the merged metrics object ready to be passed to JNI
     */
    public static IoTDeviceSDKMetrics createMetricsMqtt5(Mqtt5ClientOptions clientOptions) {
        String crtFeatureList = getEncodedFeatureListMqtt5(clientOptions);
        return createMetrics(clientOptions.getUserMetrics(), crtFeatureList);
    }

    /**
     * Builds the final metrics object for an MQTT3 connection by encoding the CRT
     * feature list from the connection configuration and merging it
     * with any user-supplied metadata.
     *
     * @param config the MQTT3 connection configuration containing proxy, TLS, and user metrics
     * @return the merged metrics object ready to be passed to JNI
     */
    public static IoTDeviceSDKMetrics createMetricsMqtt3(MqttConnectionConfig config) {
        String crtFeatureList = getEncodedFeatureListMqtt3(config.getHttpProxyOptions(), config.getTlsContext());
        return createMetrics(config.getMetrics(), crtFeatureList);
    }

    /**
     * Generates the encoded feature list string for metrics from MQTT5 client options.
     *
     * <p>
     * Format: "ID/Value,ID/Value,..."
     * Example: "A/B,C/A,F/5,G/A" means retry_jitter_mode=FULL,
     * offline_queue_behavior=FAIL_NON_QOS1, protocol=MQTT5, socket=POSIX.
     *
     * MQTT5 connections always include:
     * - F (protocol_version): set to MQTT5
     * - G (socket_implementation): detected from platform
     *
     * Conditionally includes (only when not DEFAULT):
     * - A (retry_jitter_mode),
     * - B (session_behavior),
     * - C (offline_queue_behavior),
     * - D (outbound_topic_alias),
     * - E (inbound_topic_alias),
     * - H (http_proxy_type),
     * - J (tls_cipher_preference),
     * - K (minimum_tls_version)
     *
     * Feature I (certificate_source) is set at the IoT SDK level, not here.
     *
     * @param clientOptions MQTT5 client options containing connection configuration
     * @return the encoded feature list string
     */
    private static String getEncodedFeatureListMqtt5(Mqtt5ClientOptions clientOptions) {
        List<String> features = new ArrayList<>();

        if (clientOptions.getRetryJitterMode() != null) {
            String val = retryJitterModeValue(clientOptions.getRetryJitterMode());
            if (val != null) features.add(RETRY_JITTER_MODE + "/" + val);
        }

        if (clientOptions.getSessionBehavior() != null) {
            String val = sessionBehaviorValue(clientOptions.getSessionBehavior());
            if (val != null) features.add(SESSION_BEHAVIOR + "/" + val);
        }

        if (clientOptions.getOfflineQueueBehavior() != null) {
            String val = offlineQueueBehaviorValue(clientOptions.getOfflineQueueBehavior());
            if (val != null) features.add(OFFLINE_QUEUE_BEHAVIOR + "/" + val);
        }

        TopicAliasingOptions topicAliasing = clientOptions.getTopicAliasingOptions();
        if (topicAliasing != null) {
            if (topicAliasing.getOutboundBehavior() != null) {
                String val = outboundTopicAliasBehaviorValue(topicAliasing.getOutboundBehavior());
                if (val != null) features.add(OUTBOUND_TOPIC_ALIAS_BEHAVIOR + "/" + val);
            }
            if (topicAliasing.getInboundBehavior() != null) {
                String val = inboundTopicAliasBehaviorValue(topicAliasing.getInboundBehavior());
                if (val != null) features.add(INBOUND_TOPIC_ALIAS_BEHAVIOR + "/" + val);
            }
        }

        features.add(PROTOCOL_VERSION + "/" + protocolVersionValue(true));
        features.add(SOCKET_IMPLEMENTATION + "/" + socketImplementationValue());

        HttpProxyOptions proxyOptions = clientOptions.getHttpProxyOptions();
        if (proxyOptions != null) {
            boolean proxyUsesTls = proxyOptions.getTlsContext() != null;
            features.add(HTTP_PROXY_TYPE + "/" + httpProxyTypeValue(proxyUsesTls));
        }

        TlsContext tlsCtx = clientOptions.getTlsContext();
        if (tlsCtx != null) {
            String val = tlsCipherPreferenceValue(tlsCtx.getCipherPreference());
            if (val != null) features.add(TLS_CIPHER_PREFERENCE + "/" + val);

            String tlsVer = minimumTlsVersionValue(tlsCtx.getMinimumTlsVersion());
            if (tlsVer != null) features.add(MINIMUM_TLS_VERSION + "/" + tlsVer);
        }

        return String.join(",", features);
    }

    /**
     * Generates the encoded feature list string for metrics from MQTT3 connection parameters.
     * <p>
     *
     * Format: "ID/Value,ID/Value,..."
     *
     * MQTT3 connections always include:
     * - F (protocol_version): set to MQTT311
     * - G (socket_implementation): detected from platform
     *
     * Conditionally includes:
     * - H (http_proxy_type)
     * - J (tls_cipher_preference)
     * - K (minimum_tls_version)
     *
     * @param proxyOptions optional HTTP proxy options from the connection
     * @param tlsCtx optional TLS context used by the connection
     * @return the encoded feature list string
     */
    private static String getEncodedFeatureListMqtt3(HttpProxyOptions proxyOptions, TlsContext tlsCtx) {
        List<String> features = new ArrayList<>();

        features.add(PROTOCOL_VERSION + "/" + protocolVersionValue(false));
        features.add(SOCKET_IMPLEMENTATION + "/" + socketImplementationValue());

        if (proxyOptions != null) {
            boolean proxyUsesTls = proxyOptions.getTlsContext() != null;
            features.add(HTTP_PROXY_TYPE + "/" + httpProxyTypeValue(proxyUsesTls));
        }

        if (tlsCtx != null) {
            String val = tlsCipherPreferenceValue(tlsCtx.getCipherPreference());
            if (val != null) features.add(TLS_CIPHER_PREFERENCE + "/" + val);

            String tlsVer = minimumTlsVersionValue(tlsCtx.getMinimumTlsVersion());
            if (tlsVer != null) features.add(MINIMUM_TLS_VERSION + "/" + tlsVer);
        }

        return String.join(",", features);
    }

    /**
     * Merges CRT-generated features with user-provided (IoT SDK) features.
     * When both lists contain the same feature ID, the user-provided value takes precedence.
     *
     * @param crtFeatures CRT-generated feature list (e.g. {@code "F/5,G/A"}); may be {@code null} or empty
     * @param userFeatures user-provided feature list from the IoT SDK; may be {@code null} or empty
     * @return the merged feature list string (never {@code null}; empty if both inputs are empty)
     */
    private static String mergeFeatureLists(String crtFeatures, String userFeatures) {
        LinkedHashMap<String, String> merged = new LinkedHashMap<>();
        parseFeatures(crtFeatures, merged);
        parseFeatures(userFeatures, merged);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : merged.entrySet()) {
            if (sb.length() > 0) sb.append(",");
            sb.append(entry.getKey()).append("/").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * <p>
     * Metrics creation logic:
     * - CRTVersion: auto-set from package version, not overridable by user
     * - IoTSDKMetricsVersion: always set to current IOT_SDK_METRICS_FEATURE_VERSION
     * - IoTSDKFeature: merged if user version matches, else CRT only
     * - Other user metadata: passed through unchanged
     *
     * @param userMetrics optional metrics from a higher-level IoT SDK (may be {@code null})
     * @param crtFeatureList encoded CRT feature list string
     * @return the final metrics object
     */
    private static IoTDeviceSDKMetrics createMetrics(IoTDeviceSDKMetrics userMetrics, String crtFeatureList) {
        String libraryName = (userMetrics != null) ? userMetrics.getLibraryName() : "IoTDeviceSDK/Java";

        String crtVersion = new PackageInfo().version.toString();
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("CRTVersion", crtVersion);

        String userMetricsVersion = null;
        String userFeature = "";

        if (userMetrics != null && userMetrics.getMetadataEntries() != null) {
            for (IoTMetricsMetadata entry : userMetrics.getMetadataEntries()) {
                if ("IoTSDKMetricsVersion".equals(entry.getKey())) {
                    userMetricsVersion = entry.getValue();
                } else if ("IoTSDKFeature".equals(entry.getKey())) {
                    userFeature = entry.getValue();
                } else if (!"CRTVersion".equals(entry.getKey())) {
                    metadata.put(entry.getKey(), entry.getValue());
                }
            }
        }

        if (parsedVersionMatches(userMetricsVersion)) {
            metadata.put("IoTSDKFeature", mergeFeatureLists(crtFeatureList, userFeature));
        } else {
            metadata.put("IoTSDKFeature", mergeFeatureLists(crtFeatureList, ""));
        }

        metadata.put("IoTSDKMetricsVersion", String.valueOf(IOT_SDK_METRICS_FEATURE_VERSION));

        List<IoTMetricsMetadata> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            entries.add(new IoTMetricsMetadata(entry.getKey(), entry.getValue()));
        }

        return new IoTDeviceSDKMetrics(libraryName, entries);
    }

    private static void parseFeatures(String featureStr, Map<String, String> map) {
        if (featureStr == null || featureStr.isEmpty()) return;
        for (String pair : featureStr.split(",")) {
            int slash = pair.indexOf('/');
            if (slash > 0) {
                map.put(pair.substring(0, slash), pair.substring(slash + 1));
            }
        }
    }

    /**
     * @param isMqtt5 {@code true} for MQTT5, {@code false} for MQTT3
     * @return the encoded protocol version value ({@code "5"} or {@code "3"})
     */
    private static String protocolVersionValue(boolean isMqtt5) {
        return isMqtt5 ? "5" : "3";
    }

    /**
     * @return {@code "B"} on Windows (IOCP), {@code "A"} otherwise (POSIX). Defaults to {@code "A"} if OS detection fails.
     */
    private static String socketImplementationValue() {
        try {
            return "windows".equals(CRT.getOSIdentifier()) ? "B" : "A";
        } catch (Exception e) {
            return "A";
        }
    }

    /**
     * @param proxyUsesTls whether the proxy connection is TLS-tunneled
     * @return {@code "B"} for TLS proxy, {@code "A"} for plaintext proxy
     */
    private static String httpProxyTypeValue(boolean proxyUsesTls) {
        return proxyUsesTls ? "B" : "A";
    }

    /**
     * Encodes a {@link JitterMode} into its single-character metrics value.
     *
     * @param mode the configured jitter mode
     * @return {@code "A"} for None, {@code "B"} for Full, {@code "C"} for Decorrelated, or {@code null} for Default/unknown
     */
    private static String retryJitterModeValue(JitterMode mode) {
        switch (mode) {
            case None: return "A";
            case Full: return "B";
            case Decorrelated: return "C";
            default: return null;
        }
    }

    /**
     * Encodes an MQTT5 session behavior enum value.
     *
     * @param behavior the {@code ClientSessionBehavior} ordinal
     * @return {@code "A"} for CLEAN, {@code "B"} for REJOIN_POST_SUCCESS, {@code "C"} for REJOIN_ALWAYS,
     *         or {@code null} for DEFAULT/unknown
     */
    private static String sessionBehaviorValue(Mqtt5ClientOptions.ClientSessionBehavior behavior) {
        switch (behavior) {
            case CLEAN: return "A";
            case REJOIN_POST_SUCCESS: return "B";
            case REJOIN_ALWAYS: return "C";
            default: return null;
        }
    }

    /**
     * Encodes an MQTT5 offline queue behavior enum value.
     *
     * @param behavior the {@code ClientOfflineQueueBehavior} ordinal
     * @return {@code "A"} for FAIL_NON_QOS1_PUBLISH_ON_DISCONNECT, {@code "B"} for FAIL_QOS0_PUBLISH_ON_DISCONNECT,
     *         {@code "C"} for FAIL_ALL_ON_DISCONNECT, or {@code null} for DEFAULT/unknown
     */
    private static String offlineQueueBehaviorValue(Mqtt5ClientOptions.ClientOfflineQueueBehavior behavior) {
        switch (behavior) {
            case FAIL_NON_QOS1_PUBLISH_ON_DISCONNECT: return "A";
            case FAIL_QOS0_PUBLISH_ON_DISCONNECT: return "B";
            case FAIL_ALL_ON_DISCONNECT: return "C";
            default: return null;
        }
    }

    /**
     * Encodes an outbound topic alias behavior enum value.
     *
     * @param behavior the {@code OutboundTopicAliasBehaviorType} ordinal
     * @return {@code "A"} for Manual, {@code "B"} for LRU, {@code "C"} for Disabled,
     *         or {@code null} for Default/unknown
     */
    private static String outboundTopicAliasBehaviorValue(TopicAliasingOptions.OutboundTopicAliasBehaviorType behavior) {
        switch (behavior) {
            case Manual: return "A";
            case LRU: return "B";
            case Disabled: return "C";
            default: return null;
        }
    }

    /**
     * Encodes an inbound topic alias behavior enum value.
     *
     * @param behavior the {@code InboundTopicAliasBehaviorType} ordinal
     * @return {@code "A"} for Enabled, {@code "B"} for Disabled, or {@code null} for Default/unknown
     */
    private static String inboundTopicAliasBehaviorValue(TopicAliasingOptions.InboundTopicAliasBehaviorType behavior) {
        switch (behavior) {
            case Enabled: return "A";
            case Disabled: return "B";
            default: return null;
        }
    }

    /**
     * Encodes a {@link TlsCipherPreference} into its single-character metrics value.
     *
     * @param pref the configured TLS cipher preference
     * @return {@code "A"} for {@code TLS_CIPHER_PREF_PQ_TLSv1_0_2021_05},
     *         {@code "B"} for {@code TLS_CIPHER_PQ_DEFAULT},
     *         {@code "C"} for {@code TLS_CIPHER_PREF_TLSv1_2_2025},
     *         or {@code null} for system default/unknown
     */
    private static String tlsCipherPreferenceValue(TlsCipherPreference pref) {
        switch (pref) {
            case TLS_CIPHER_PREF_PQ_TLSv1_0_2021_05: return "A"; // TLS_CIPHER_PREF_PQ_TLSv1_0_2021_05
            case TLS_CIPHER_PQ_DEFAULT: return "B"; // TLS_CIPHER_PQ_DEFAULT
            case TLS_CIPHER_PREF_TLSv1_2_2025: return "C"; // TLS_CIPHER_PREF_TLSv1_2_2025
            default: return null;
        }
    }

    /**
     * Encodes a minimum {@link TlsContextOptions.TlsVersions TLS version} into its single-character metrics value.
     *
     * @param version the configured minimum TLS version
     * @return {@code "A"} for SSLv3, {@code "B"} for TLSv1, {@code "C"} for TLSv1.1,
     *         {@code "D"} for TLSv1.2, {@code "E"} for TLSv1.3, or {@code null} for system default/unknown
     */
    private static String minimumTlsVersionValue(TlsContextOptions.TlsVersions version) {
        switch (version) {
            case SSLv3: return "A"; // SSLv3
            case TLSv1: return "B"; // TLSv1
            case TLSv1_1: return "C"; // TLSv1_1
            case TLSv1_2: return "D"; // TLSv1_2
            case TLSv1_3: return "E"; // TLSv1_3
            default: return null;
        }
    }

    /**
     * Returns {@code true} when {@code userMetricsVersion} parses as an integer equal to
     * {@link #IOT_SDK_METRICS_FEATURE_VERSION}. Null and non-numeric inputs return {@code false}.
     *
     * @param userMetricsVersion the user-supplied {@code IoTSDKMetricsVersion} string (may be {@code null})
     * @return {@code true} if the userMetricsVersion matches the current schema, {@code false} otherwise
     */
    private static boolean parsedVersionMatches(String userMetricsVersion) {
        if (userMetricsVersion == null) return false;
        try {
            return Integer.parseInt(userMetricsVersion) == IOT_SDK_METRICS_FEATURE_VERSION;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
