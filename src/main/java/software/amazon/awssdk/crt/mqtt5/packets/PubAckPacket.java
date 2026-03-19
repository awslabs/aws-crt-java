/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5.packets;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data model of an <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901121">MQTT5 PUBACK</a> packet
 */
public class PubAckPacket {

    private PubAckReasonCode reasonCode;
    private String reasonString;
    private List<UserProperty> userProperties;

    /**
     * Returns success indicator or failure reason for the associated PublishPacket.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901124">MQTT5 PUBACK Reason Code</a>
     *
     * @return Success indicator or failure reason for the associated PublishPacket.
     */
    public PubAckReasonCode getReasonCode() {
        return this.reasonCode;
    }

    /**
     * Returns additional diagnostic information about the result of the PUBLISH attempt.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901127">MQTT5 Reason String</a>
     *
     * @return Additional diagnostic information about the result of the PUBLISH attempt.
     */
    public String getReasonString() {
        return this.reasonString;
    }

    /**
     * Returns a list of MQTT5 user properties included with the packet.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901128">MQTT5 User Property</a>
     *
     * @return List of MQTT5 user properties included with the packet.
     */
    public List<UserProperty> getUserProperties() {
        return this.userProperties;
    }

    private PubAckPacket() {}

    /**
     * A native, JNI-only helper function for more easily setting the reason code
     * @param reasonCode A int representing the reason code
     */
    private void nativeAddReasonCode(int reasonCode) {
        this.reasonCode = PubAckReasonCode.getEnumValueFromInteger(reasonCode);
    }

    /**
     * Reason code inside PubAckPackets that indicates the result of the associated PUBLISH request.
     *
     * Enum values match <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901124">MQTT5 spec</a> encoding values.
     */
    public enum PubAckReasonCode {

        /**
         * Returned when the (QoS 1) publish was accepted by the recipient.
         *
         * May be sent by the client or the server.
         */
        SUCCESS(0),

        /**
         * Returned when the (QoS 1) publish was accepted but there were no matching subscribers.
         *
         * May only be sent by the server.
         */
        NO_MATCHING_SUBSCRIBERS(16),

        /**
         * Returned when the (QoS 1) publish was not accepted and the receiver does not want to specify a reason or none
         * of the other reason codes apply.
         *
         * May be sent by the client or the server.
         */
        UNSPECIFIED_ERROR(128),

        /**
         * Returned when the (QoS 1) publish was valid but the receiver was not willing to accept it.
         *
         * May be sent by the client or the server.
         */
        IMPLEMENTATION_SPECIFIC_ERROR(131),

        /**
         * Returned when the (QoS 1) publish was not authorized by the receiver.
         *
         * May be sent by the client or the server.
         */
        NOT_AUTHORIZED(135),

        /**
         * Returned when the topic name was valid but the receiver was not willing to accept it.
         *
         * May be sent by the client or the server.
         */
        TOPIC_NAME_INVALID(144),

        /**
         * Returned when the packet identifier used in the associated PUBLISH was already in use.
         * This can indicate a mismatch in the session state between client and server.
         *
         * May be sent by the client or the server.
         */
        PACKET_IDENTIFIER_IN_USE(145),

        /**
         * Returned when the associated PUBLISH failed because an internal quota on the recipient was exceeded.
         *
         * May be sent by the client or the server.
         */
        QUOTA_EXCEEDED(151),

        /**
         * Returned when the PUBLISH packet's payload format did not match its payload format indicator property.
         *
         * May be sent by the client or the server.
         */
        PAYLOAD_FORMAT_INVALID(153);

        private int reasonCode;

        private PubAckReasonCode(int code) {
            reasonCode = code;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return reasonCode;
        }

        /**
         * Creates a Java PubAckReasonCode enum value from a native integer value.
         *
         * @param value native integer value for PubAckReasonCode
         * @return a new PubAckReasonCode value
         */
        public static PubAckReasonCode getEnumValueFromInteger(int value) {
            PubAckReasonCode enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal PubAckReasonCode");
        }

        private static Map<Integer, PubAckReasonCode> buildEnumMapping() {
            return Stream.of(PubAckReasonCode.values())
                .collect(Collectors.toMap(PubAckReasonCode::getValue, Function.identity()));
        }

        private static Map<Integer, PubAckReasonCode> enumMapping = buildEnumMapping();
    }
}
