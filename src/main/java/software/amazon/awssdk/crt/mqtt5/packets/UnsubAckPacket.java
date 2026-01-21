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
 * Data model of an <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc471483687">MQTT5 UNSUBACK</a> packet.
 */
public class UnsubAckPacket {

    private String reasonString;
    private List<UserProperty> userProperties;
    private List<UnsubAckReasonCode> reasonCodes;

    /**
     * Returns additional diagnostic information about the result of the UNSUBSCRIBE attempt.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901192">MQTT5 Reason String</a>
     *
     * @return Additional diagnostic information about the result of the UNSUBSCRIBE attempt.
     */
    public String getReasonString() {
        return this.reasonString;
    }

    /**
     * Returns a list of MQTT5 user properties included with the packet.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901193">MQTT5 User Property</a>
     *
     * @return List of MQTT5 user properties included with the packet.
     */
    public List<UserProperty> getUserProperties() {
        return this.userProperties;
    }

    /**
     * Returns a list of reason codes indicating the result of unsubscribing from each individual topic filter entry in the
     * associated UNSUBSCRIBE packet.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901194">MQTT5 Unsuback Payload</a>
     *
     * @return A list of reason codes indicating the result of unsubscribing from each individual topic filter entry in the
     * associated UNSUBSCRIBE packet.
     */
    public List<UnsubAckReasonCode> getReasonCodes() {
        return this.reasonCodes;
    }

    private UnsubAckPacket() {}

    /**
     * A native, JNI-only helper function for more easily adding a UnsubAckReasonCode
     * @param reasonCode A int representing the UnsubAckReasonCode
     */
    private void nativeAddUnsubackCode(int reasonCode) {
        if (this.reasonCodes == null) {
            this.reasonCodes = new ArrayList<UnsubAckReasonCode>();
        }
        this.reasonCodes.add(UnsubAckReasonCode.getEnumValueFromInteger(reasonCode));
    }

    /**
     * Reason codes inside UnsubAckPacket payloads that specify the results for each topic filter in the associated
     * UnsubscribePacket.
     *
     * Enum values match <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901194">MQTT5 spec</a> encoding values.
     */
    public enum UnsubAckReasonCode {

        /**
         * Returned when the unsubscribe was successful and the client is no longer subscribed to the topic filter on the server.
         */
        SUCCESS(0),

        /**
         * Returned when the topic filter did not match one of the client's existing topic filters on the server.
         */
        NO_SUBSCRIPTION_EXISTED(17),

        /**
         * Returned when the unsubscribe of the topic filter was not accepted and the server does not want to specify a
         * reason or none of the other reason codes apply.
         */
        UNSPECIFIED_ERROR(128),

        /**
         * Returned when the topic filter was valid but the server does not accept an unsubscribe for it.
         */
        IMPLEMENTATION_SPECIFIC_ERROR(131),

        /**
         * Returned when the client was not authorized to unsubscribe from that topic filter on the server.
         */
        NOT_AUTHORIZED(135),

        /**
         * Returned when the topic filter was correctly formed but is not allowed for the client on the server.
         */
        TOPIC_FILTER_INVALID(143),

        /**
         * Returned when the packet identifier was already in use on the server.
         */
        PACKET_IDENTIFIER_IN_USE(145);

        private int reasonCode;

        private UnsubAckReasonCode(int code) {
            reasonCode = code;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return reasonCode;
        }

        /**
         * Creates a Java UnsubAckReasonCode enum value from a native integer value.
         *
         * @param value native integer value for UnsubAckReasonCode
         * @return a new UnsubAckReasonCode value
         */
        public static UnsubAckReasonCode getEnumValueFromInteger(int value) {
            UnsubAckReasonCode enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal UnsubAckReasonCode");
        }

        private static Map<Integer, UnsubAckReasonCode> buildEnumMapping() {
            return Stream.of(UnsubAckReasonCode.values())
                .collect(Collectors.toMap(UnsubAckReasonCode::getValue, Function.identity()));
        }

        private static Map<Integer, UnsubAckReasonCode> enumMapping = buildEnumMapping();
    }

}
