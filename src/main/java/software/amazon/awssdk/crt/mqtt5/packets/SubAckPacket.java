/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data model of an <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901171">MQTT5 SUBACK</a> packet.
 */
public class SubAckPacket {

    private String reasonString;
    private List<UserProperty> userProperties;
    private List<SubAckReasonCode> reasonCodes;

    /**
     * Returns additional diagnostic information about the result of the SUBSCRIBE attempt.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901176">MQTT5 Reason String</a>
     *
     * @return Additional diagnostic information about the result of the SUBSCRIBE attempt.
     */
    public String getReasonString() {
        return this.reasonString;
    }

    /**
     * Returns a list of MQTT5 user properties included with the packet.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901177">MQTT5 User Property</a>
     *
     * @return List of MQTT5 user properties included with the packet.
     */
    public List<UserProperty> getUserProperties() {
        return this.userProperties;
    }

    /**
     * Returns a list of reason codes indicating the result of each individual subscription entry in the
     * associated SubscribePacket.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901178">MQTT5 Suback Payload</a>
     *
     * @return list of reason codes indicating the result of each individual subscription entry in the
     * associated SubscribePacket.
     */
    public List<SubAckReasonCode> getReasonCodes() {
        return this.reasonCodes;
    }

    private SubAckPacket() {}

    /**
     * A native, JNI-only helper function for more easily adding a SubAckReasonCode
     * @param reasonCode A int representing the SubAckReasonCode
     */
    private void nativeAddSubackCode(int reasonCode) {
        if (this.reasonCodes == null) {
            this.reasonCodes = new ArrayList<SubAckReasonCode>();
        }
        this.reasonCodes.add(SubAckReasonCode.getEnumValueFromInteger(reasonCode));
    }

    /**
     * Reason code inside SubAckPacket payloads.
     * Enum values match MQTT spec encoding values.
     *
     * This will only be sent by the server and not the client.
     *
     * Enum values match <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901178">MQTT5 spec</a> encoding values.
     */
    public enum SubAckReasonCode {

        /**
         * Returned when the subscription was accepted and the maximum QoS sent will be QoS 0.
         */
        GRANTED_QOS_0(0),

        /**
         * Returned when the subscription was accepted and the maximum QoS sent will be QoS 1.
         */
        GRANTED_QOS_1(1),

        /**
         * Returned when the subscription was accepted and the maximum QoS sent will be QoS 2.
         */
        GRANTED_QOS_2(2),

        /**
         * Returned when the connection was closed but the sender does not want to specify a reason or none
         * of the other reason codes apply.
         */
        UNSPECIFIED_ERROR(128),

        /**
         * Returned when the subscription was valid but the server did not accept it.
         */
        IMPLEMENTATION_SPECIFIC_ERROR(131),

        /**
         * Returned when the client was not authorized to make the subscription on the server.
         */
        NOT_AUTHORIZED(135),

        /**
         * Returned when the subscription topic filter was correctly formed but not allowed for the client.
         */
        TOPIC_FILTER_INVALID(143),

        /**
         * Returned when the packet identifier was already in use on the server.
         */
        PACKET_IDENTIFIER_IN_USE(145),

        /**
         * Returned when a subscribe-related quota set on the server was exceeded.
         */
        QUOTA_EXCEEDED(151),

        /**
         * Returned when the subscription's topic filter was a shared subscription and the server does not support
         * shared subscriptions.
         */
        SHARED_SUBSCRIPTIONS_NOT_SUPPORTED(158),

        /**
         * Returned when the SubscribePacket contained a subscription identifier and the server does not support
         * subscription identifiers.
         */
        SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED(161),

        /**
         * Returned when the subscription's topic filter contains a wildcard but the server does not support
         * wildcard subscriptions.
         */
        WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED(162);

        private int reasonCode;

        private SubAckReasonCode(int code) {
            reasonCode = code;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return reasonCode;
        }

        /**
         * Creates a Java SubAckReasonCode enum value from a native integer value.
         *
         * @param value native integer value for SubAckReasonCode
         * @return a new SubAckReasonCode value
         */
        public static SubAckReasonCode getEnumValueFromInteger(int value) {
            SubAckReasonCode enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal SubAckReasonCode");
        }

        private static Map<Integer, SubAckReasonCode> buildEnumMapping() {
            return Stream.of(SubAckReasonCode.values())
                .collect(Collectors.toMap(SubAckReasonCode::getValue, Function.identity()));
        }

        private static Map<Integer, SubAckReasonCode> enumMapping = buildEnumMapping();
    }
}
