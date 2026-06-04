/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.internal;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions;
import software.amazon.awssdk.crt.mqtt5.TopicAliasingOptions;
import software.amazon.awssdk.crt.utils.PackageInfo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @internal
 * Encodes IoT SDK metrics features and creates final metrics object
 *
 */

public class IoTMetricEncoder{

    /*
    Feature IDs for IoT SDK metrics tracking.

    Each ID is a single character used to encode feature usage in the metrics
    string with the format "ID/Value". IDs are assigned sequentially and never
    reused to ensure historical data consistency across SDK versions.
    */

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

    // Metrics Version Constant
    public static final int IOT_SDK_METRICS_FEATURE_VERSION = 1;

    // value mapping methods

    /**
     * Map protocol version to its single-character metrics value.
     * Mapping: MQTT311->3, MQTT5->5.
     *
     * @param isMqtt5 true if the client is using MQTT5, false for MQTT311
     * @return the single-character metrics value for the protocol version
     */
    private static String protocolVersionValue(boolean isMqtt5) {
        return isMqtt5 ? "5" : "3";
    }

    /**
     * Detect the socket implementation and return its single-character metrics value.
     * Mapping: Windows (WINSOCK)->B, all other platforms (POSIX)->A.
     *
     * @return the single-character metrics value for the socket implementation
     * @throws CRT.UnknownPlatformException if the running platform cannot be identified
     */
    private static String socketImplementationValue() throws CRT.UnknownPlatformException {
        return "windows".equals(CRT.getOSIdentifier()) ? "B" : "A";
    }

    /**
     * Map proxy options to its single-character metrics value for proxy type.
     * Mapping: HTTPS (has tls connection option)->B, HTTP->A.
     *
     * @param proxyUsesTls
     * @return the single-character metrics value for the proxy options
     */
    private static String httpProxyTypeValue(boolean proxyUsesTls){
        return proxyUsesTls? "B" : "A";
    }

    /**
    * Maps ExponentialBackoffJitterMode value to its metrics character.
    *
    * @param value the int value from JitterMode.getValue()
    * @return single-character metrics value, or null if DEFAULT (should be omitted)
    */
    private static String retryJitterModeValue(int value) {
        switch (value) {
            case 1: return "A"; // None
            case 2: return "B"; // Full
            case 3: return "C"; // Decorrelated
            default: return null;
        }
    }

    /**
    * Maps ClientSessionBehavior value to its metrics character.
    *
    * @param value the int value from ClientSessionBehavior.getValue()
    * @return single-character metrics value, or null if DEFAULT (should be omitted)
    */
    private static String sessionBehaviorValue(int value) {
        switch (value) {
            case 1: return "A"; // CLEAN
            case 2: return "B"; // REJOIN_POST_SUCCESS
            case 3: return "C"; // REJOIN_ALWAYS
            default: return null;
        }
    }

    /**
    * Maps ClientOfflineQueueBehavior value to its metrics character.
    *
    * @param value the int value from ClientOfflineQueueBehavior.getValue()
    * @return single-character metrics value, or null if DEFAULT (should be omitted)
    */
    private static String offlineQueueBehaviorValue(int value) {
        switch (value) {
            case 1: return "A"; // FAIL_NON_QOS1_PUBLISH_ON_DISCONNECT
            case 2: return "B"; // FAIL_QOS0_PUBLISH_ON_DISCONNECT
            case 3: return "C"; // FAIL_ALL_ON_DISCONNECT
            default: return null;
        }
    }

    /**
    * Maps OutboundTopicAliasBehaviorType value to its metrics character.
    *
    * @param value the int value from OutboundTopicAliasBehaviorType.getValue()
    * @return single-character metrics value, or null if Default (should be omitted)
    */
    private static String outboundTopicAliasBehaviorValue(int value) {
        switch (value) {
            case 1: return "A"; // Manual
            case 2: return "B"; // LRU
            case 3: return "C"; // Disabled
            default: return null;
        }
    }

    /**
    * Maps InboundTopicAliasBehaviorType value to its metrics character.
    *
    * @param value the int value from InboundTopicAliasBehaviorType.getValue()
    * @return single-character metrics value, or null if Default (should be omitted)
    */
    private static String inboundTopicAliasBehaviorValue(int value) {
        switch (value) {
            case 1: return "A"; // Enabled
            case 2: return "B"; // Disabled
            default: return null;
        }
    }

    /**
    * Maps TlsCipherPreference value to its metrics character.
    *
    * @param value the int value from TlsCipherPreference.getValue()
    * @return single-character metrics value, or null if SYSTEM_DEFAULT (should be omitted)
    */
    private static String tlsCipherPreferenceValue(int value) {
        switch (value) {
            case 6: return "A"; // TLS_CIPHER_PREF_PQ_TLSv1_0_2021_05
            case 8: return "B"; // TLS_CIPHER_PQ_DEFAULT
            case 9: return "C"; // TLS_CIPHER_PREF_TLSv1_2_2025
            default: return null;
        }
    }

    /**
    * Maps TlsVersions value to its metrics character.
    *
    * @param value the int value from TlsVersions.getValue()
    * @return single-character metrics value, or null if SYS_DEFAULTS (should be omitted)
    */
    private static String minimumTlsVersionValue(int value) {
        switch (value) {
            case 0: return "A"; // SSLv3
            case 1: return "B"; // TLSv1
            case 2: return "C"; // TLSv1_1
            case 3: return "D"; // TLSv1_2
            case 4: return "E"; // TLSv1_3
            default: return null;
        }
    }

    // Feature List Encoding

    /**
     * Generates the encoded feature list string for metrics from MQTT5 client options.
     *
     * Format: "ID/Value,ID/Value,..."
     * Example: "A/B,C/A,F/5,G/A" means retry_jitter_mode=FULL,
     * offline_queue_behavior=FAIL_NON_QOS1, protocol=MQTT5, socket=POSIX.
     *
     * MQTT5 connections always include:
     * - F (protocol_version): set to MQTT5
     * - G (socket_implementation): detected from platform
     *
     * Conditionally includes (only when not DEFAULT):
     * A (retry_jitter_mode), B (session_behavior), C (offline_queue_behavior),
     * D (outbound_topic_alias), E (inbound_topic_alias), H (http_proxy_type),
     * J (tls_cipher_preference), K (minimum_tls_version)
     *
     * Feature I (certificate_source) is set at the IoT SDK level, not here.
     *
     * @param clientOptions MQTT5 client options containing connection configuration
     * @return the encoded feature list string
     */
    public static String getEncodedFeatureListMqtt5(Mqtt5ClientOptions clientOptions) {
        List<String> features = new ArrayList<>();

        // A: retry_jitter_mode
        if (clientOptions.getRetryJitterMode() != null) {
            String val = retryJitterModeValue(clientOptions.getRetryJitterMode().getValue());
            if (val != null) features.add(RETRY_JITTER_MODE + "/" + val);
        }

        // B: session_behavior
        if (clientOptions.getSessionBehavior() != null) {
            String val = sessionBehaviorValue(clientOptions.getSessionBehavior().getValue());
            if (val != null) features.add(SESSION_BEHAVIOR + "/" + val);
        }

        // C: offline_queue_behavior
        if (clientOptions.getOfflineQueueBehavior() != null) {
            String val = offlineQueueBehaviorValue(clientOptions.getOfflineQueueBehavior().getValue());
            if (val != null) features.add(OFFLINE_QUEUE_BEHAVIOR + "/" + val);
        }

        // D & E: topic aliasing
        TopicAliasingOptions topicAliasing = clientOptions.getTopicAliasingOptions();
        if (topicAliasing != null) {
            if (topicAliasing.getOutboundBehavior() != null) {
                String val = outboundTopicAliasBehaviorValue(topicAliasing.getOutboundBehavior().getValue());
                if (val != null) features.add(OUTBOUND_TOPIC_ALIAS_BEHAVIOR + "/" + val);
            }
            if (topicAliasing.getInboundBehavior() != null) {
                String val = inboundTopicAliasBehaviorValue(topicAliasing.getInboundBehavior().getValue());
                if (val != null) features.add(INBOUND_TOPIC_ALIAS_BEHAVIOR + "/" + val);
            }
        }

        // F: protocol_version - always MQTT5
        features.add(PROTOCOL_VERSION + "/" + protocolVersionValue(true));

        // G: socket_implementation - always present
        features.add(SOCKET_IMPLEMENTATION + "/" + socketImplementationValue());

        // H: http_proxy_type
        HttpProxyOptions proxyOptions = clientOptions.getHttpProxyOptions();
        if (proxyOptions != null) {
            boolean proxyUsesTls = proxyOptions.getTlsContext() != null;
            features.add(HTTP_PROXY_TYPE + "/" + httpProxyTypeValue(proxyUsesTls));
        }

        // I: certificate_source - set at IoT SDK level, not here

        // J & K: TLS properties
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
     *
     * MQTT3 connections always include:
     * - F (protocol_version): set to MQTT311
     * - G (socket_implementation): detected from platform
     *
     * Conditionally includes:
     * H (http_proxy_type), J (tls_cipher_preference), K (minimum_tls_version)
     *
     * @param proxyOptions optional HTTP proxy options from the connection
     * @param tlsCtx optional TLS context used by the connection
     * @return the encoded feature list string
     */
    public static String getEncodedFeatureListMqtt3(HttpProxyOptions proxyOptions, TlsContext tlsCtx) {
        List<String> features = new ArrayList<>();

        // F: protocol_version - always MQTT311
        features.add(PROTOCOL_VERSION + "/" + protocolVersionValue(false));

        // G: socket_implementation
        features.add(SOCKET_IMPLEMENTATION + "/" + socketImplementationValue());

        // H: http_proxy_type
        if (proxyOptions != null) {
            boolean proxyUsesTls = proxyOptions.getTlsContext() != null;
            features.add(HTTP_PROXY_TYPE + "/" + httpProxyTypeValue(proxyUsesTls));
        }

        // J & K: TLS properties
        if (tlsCtx != null) {
            String val = tlsCipherPreferenceValue(tlsCtx.getCipherPreference());
            if (val != null) features.add(TLS_CIPHER_PREFERENCE + "/" + val);

            String tlsVer = minimumTlsVersionValue(tlsCtx.getMinimumTlsVersion());
            if (tlsVer != null) features.add(MINIMUM_TLS_VERSION + "/" + tlsVer);
        }

        return String.join(",", features);
    }

    // ======================== Feature List Merging ========================

    /**
     * Merges CRT-generated features with user-provided (IoT SDK) features.
     * When both lists contain the same feature ID, the user-provided value
     * takes precedence. Insertion order is preserved.
     *
     * @param crtFeatures CRT-generated feature list string (e.g., "F/5,G/A")
     * @param userFeatures user-provided feature list from the IoT SDK (may be null or empty)
     * @return the merged feature list string
     */
    public static String mergeFeatureLists(String crtFeatures, String userFeatures) {
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
     * Parses a feature string into key-value pairs in the given map.
     * Later entries overwrite earlier ones with the same key.
     *
     * @param featureStr comma-separated feature string (e.g., "A/B,F/5")
     * @param map target map to populate
     */
    private static void parseFeatures(String featureStr, Map<String, String> map) {
        if (featureStr == null || featureStr.isEmpty()) return;
        for (String pair : featureStr.split(",")) {
            int slash = pair.indexOf('/');
            if (slash > 0) {
                map.put(pair.substring(0, slash), pair.substring(slash + 1));
            }
        }
    }

    // ======================== Metrics Creation ========================

    /**
     * Creates the final IoTDeviceSDKMetrics object for an MQTT5 client.
     *
     * @param clientOptions MQTT5 client options containing connection configuration
     * @param userMetrics optional metrics configuration from the IoT SDK (may be null)
     * @return the final metrics object ready for JNI passing
     */
    public static IoTDeviceSDKMetrics createMetricsMqtt5(Mqtt5ClientOptions clientOptions, IoTDeviceSDKMetrics userMetrics) {
        String crtFeatureList = getEncodedFeatureListMqtt5(clientOptions);
        return createMetrics(userMetrics, crtFeatureList);
    }

    /**
     * Creates the final IoTDeviceSDKMetrics object for an MQTT3 connection.
     *
     * @param userMetrics optional metrics configuration from the IoT SDK (may be null)
     * @param proxyOptions optional HTTP proxy options from the connection
     * @param tlsCtx optional TLS context used by the connection
     * @return the final metrics object ready for JNI passing
     */
    public static IoTDeviceSDKMetrics createMetricsMqtt3(IoTDeviceSDKMetrics userMetrics, HttpProxyOptions proxyOptions, TlsContext tlsCtx) {
        String crtFeatureList = getEncodedFeatureListMqtt3(proxyOptions, tlsCtx);
        return createMetrics(userMetrics, crtFeatureList);
    }

    /**
     * Metrics creation logic:
     * 1. CRTVersion: auto-set from package version, not overridable by user
     * 2. IoTSDKMetricsVersion: always set to current IOT_SDK_METRICS_FEATURE_VERSION
     * 3. IoTSDKFeature: merged if user version matches, else CRT only
     * 4. Other user metadata: passed through unchanged
     *
     * @param userMetrics optional metrics from IoT SDK (may be null)
     * @param crtFeatureList encoded CRT feature list string
     * @return the final metrics object
     */
    private static IoTDeviceSDKMetrics createMetrics(IoTDeviceSDKMetrics userMetrics, String crtFeatureList) {
        String libraryName = (userMetrics != null) ? userMetrics.getLibraryName() : "IoTDeviceSDK/Java";

        // CRTVersion: not modifiable by user
        String crtVersion = new PackageInfo().version.toString();
        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("CRTVersion", crtVersion);

        // Extract user metadata
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

        // Merge features: if version matches, merge CRT + SDK; otherwise CRT only
        if (userMetricsVersion != null && isNumeric(userMetricsVersion)
                && Integer.parseInt(userMetricsVersion) == IOT_SDK_METRICS_FEATURE_VERSION) {
            metadata.put("IoTSDKFeature", mergeFeatureLists(crtFeatureList, userFeature));
        } else {
            metadata.put("IoTSDKFeature", mergeFeatureLists(crtFeatureList, ""));
        }

        // Always set current metrics version
        metadata.put("IoTSDKMetricsVersion", String.valueOf(IOT_SDK_METRICS_FEATURE_VERSION));

        // Build final metrics object
        List<IoTMetricsMetadata> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            entries.add(new IoTMetricsMetadata(entry.getKey(), entry.getValue()));
        }

        return new IoTDeviceSDKMetrics(libraryName, entries);
    }

    /**
     * Checks if a string can be parsed as an integer.
     *
     * @param str the string to check
     * @return true if the string is a valid integer, false otherwise
     */
    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}

