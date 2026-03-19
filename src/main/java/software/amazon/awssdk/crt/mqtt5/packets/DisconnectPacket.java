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
 * Data model of an <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901205">MQTT5 DISCONNECT</a> packet.
 */
public class DisconnectPacket {

    private DisconnectReasonCode reasonCode = DisconnectReasonCode.NORMAL_DISCONNECTION;
    private Long sessionExpiryIntervalSeconds;
    private String reasonString;
    private List<UserProperty> userProperties;
    private String serverReference;

    /**
     * Returns a value indicating the reason that the sender is closing the connection
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901208">MQTT5 Disconnect Reason Code</a>
     *
     * @return Value indicating the reason that the sender is closing the connection
     */
    public DisconnectReasonCode getReasonCode() {
        return this.reasonCode;
    }

    /**
     * Returns a change to the session expiry interval negotiated at connection time as part of the disconnect.  Only
     * valid for DisconnectPackets sent from client to server.  It is not valid to attempt to change session expiry
     * from zero to a non-zero value.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901211">MQTT5 Session Expiry Interval</a>.
     *
     * @return A change to the session expiry interval negotiated at connection time as part of the disconnect.
     */
    public Long getSessionExpiryIntervalSeconds() {
        return this.sessionExpiryIntervalSeconds;
    }

    /**
     * Returns additional diagnostic information about the reason that the sender is closing the connection
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901212">MQTT5 Reason String</a>
     *
     * @return Additional diagnostic information about the reason that the sender is closing the connection
     */
    public String getReasonString() {
        return this.reasonString;
    }

    /**
     * Returns a list of MQTT5 user properties included with the packet.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901213">MQTT5 User Property</a>
     *
     * @return List of MQTT5 user properties included with the packet.
     */
    public List<UserProperty> getUserProperties() {
        return this.userProperties;
    }

    /**
     * Returns a property indicating an alternate server that the client may temporarily or permanently attempt
     * to connect to instead of the configured endpoint.  Will only be set if the reason code indicates another
     * server may be used (ServerMoved, UseAnotherServer).
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901214">MQTT5 Server Reference</a>
     *
     * @return Property indicating an alternate server that the client may temporarily or permanently attempt
     * to connect to instead of the configured endpoint.
     */
    public String getServerReference() {
        return this.serverReference;
    }

    /**
     * Creates a DisconnectPacket instance using the provided DisconnectPacketBuilder.
     */
    private DisconnectPacket(DisconnectPacketBuilder builder) {
        this.reasonCode = builder.reasonCode;
        this.sessionExpiryIntervalSeconds = builder.sessionExpiryIntervalSeconds;
        this.reasonString = builder.reasonString;
        this.userProperties = builder.userProperties;
        this.serverReference = builder.serverReference;
    }

    private DisconnectPacket() {}

    /**
     * A native, JNI-only helper function for more easily setting the reason code
     * @param reasonCode A int representing the reason code
     */
    private void nativeAddDisconnectReasonCode(int reasonCode) {
        this.reasonCode = DisconnectReasonCode.getEnumValueFromInteger(reasonCode);
    }

    /*******************************************************************************
     * builder
     ******************************************************************************/

    /**
     * Reason code inside DisconnectPackets.  Helps determine why a connection was terminated.
     *
     * Enum values match <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901208">MQTT5 spec</a> encoding values.
     */
    public enum DisconnectReasonCode {

        /**
         * Returned when the remote endpoint wishes to disconnect normally. Will not trigger the publish of a Will message if a
         * Will message was configured on the connection.
         *
         * May be sent by the client or server.
         */
        NORMAL_DISCONNECTION(0),

        /**
         * Returns when the client wants to disconnect but requires that the server publish the Will message configured
         * on the connection.
         *
         * May only be sent by the client.
         */
        DISCONNECT_WITH_WILL_MESSAGE(4),

        /**
         * Returned when the connection was closed but the sender does not want to specify a reason or none
         * of the other reason codes apply.
         *
         * May be sent by the client or the server.
         */
        UNSPECIFIED_ERROR(128),

        /**
         * Indicates the remote endpoint received a packet that does not conform to the MQTT specification.
         *
         * May be sent by the client or the server.
         */
        MALFORMED_PACKET(129),

        /**
         * Returned when an unexpected or out-of-order packet was received by the remote endpoint.
         *
         * May be sent by the client or the server.
         */
        PROTOCOL_ERROR(130),

        /**
         * Returned when a valid packet was received by the remote endpoint, but could not be processed by the current implementation.
         *
         * May be sent by the client or the server.
         */
        IMPLEMENTATION_SPECIFIC_ERROR(131),

        /**
         * Returned when the remote endpoint received a packet that represented an operation that was not authorized within
         * the current connection.
         *
         * May only be sent by the server.
         */
        NOT_AUTHORIZED(135),

        /**
         * Returned when the server is busy and cannot continue processing packets from the client.
         *
         * May only be sent by the server.
         */
        SERVER_BUSY(137),

        /**
         * Returned when the server is shutting down.
         *
         * May only be sent by the server.
         */
        SERVER_SHUTTING_DOWN(139),

        /**
         * Returned when the server closes the connection because no packet from the client has been received in
         * 1.5 times the KeepAlive time set when the connection was established.
         *
         * May only be sent by the server.
         */
        KEEP_ALIVE_TIMEOUT(141),

        /**
         * Returned when the server has established another connection with the same client ID as a client's current
         * connection, causing the current client to become disconnected.
         *
         * May only be sent by the server.
         */
        SESSION_TAKEN_OVER(142),

        /**
         * Returned when the topic filter name is correctly formed but not accepted by the server.
         *
         * May only be sent by the server.
         */
        TOPIC_FILTER_INVALID(143),

        /**
         * Returned when topic name is correctly formed, but is not accepted.
         *
         * May be sent by the client or the server.
         */
        TOPIC_NAME_INVALID(144),

        /**
         * Returned when the remote endpoint reached a state where there were more in-progress QoS1+ publishes then the
         * limit it established for itself when the connection was opened.
         *
         * May be sent by the client or the server.
         */
        RECEIVE_MAXIMUM_EXCEEDED(147),

        /**
         * Returned when the remote endpoint receives a PublishPacket that contained a topic alias greater than the
         * maximum topic alias limit that it established for itself when the connection was opened.
         *
         * May be sent by the client or the server.
         */
        TOPIC_ALIAS_INVALID(148),

        /**
         * Returned when the remote endpoint received a packet whose size was greater than the maximum packet size limit
         * it established for itself when the connection was opened.
         *
         * May be sent by the client or the server.
         */
        PACKET_TOO_LARGE(149),

        /**
         * Returned when the remote endpoint's incoming data rate was too high.
         *
         * May be sent by the client or the server.
         */
        MESSAGE_RATE_TOO_HIGH(150),

        /**
         * Returned when an internal quota of the remote endpoint was exceeded.
         *
         * May be sent by the client or the server.
         */
        QUOTA_EXCEEDED(151),

        /**
         * Returned when the connection was closed due to an administrative action.
         *
         * May be sent by the client or the server.
         */
        ADMINISTRATIVE_ACTION(152),

        /**
         * Returned when the remote endpoint received a packet where payload format did not match the format specified
         * by the payload format indicator.
         *
         * May be sent by the client or the server.
         */
        PAYLOAD_FORMAT_INVALID(153),

        /**
         * Returned when the server does not support retained messages.
         *
         * May only be sent by the server.
         */
        RETAIN_NOT_SUPPORTED(154),

        /**
         * Returned when the client sends a QoS that is greater than the maximum QOS established when the connection was
         * opened.
         *
         * May only be sent by the server.
         */
        QOS_NOT_SUPPORTED(155),

        /**
         * Returned by the server to tell the client to temporarily use a different server.
         *
         * May only be sent by the server.
         */
        USE_ANOTHER_SERVER(156),

        /**
         * Returned by the server to tell the client to permanently use a different server.
         *
         * May only be sent by the server.
         */
        SERVER_MOVED(157),

        /**
         * Returned by the server to tell the client that shared subscriptions are not supported on the server.
         *
         * May only be sent by the server.
         */
        SHARED_SUBSCRIPTIONS_NOT_SUPPORTED(158),

        /**
         * Returned when the server disconnects the client due to the connection rate being too high.
         *
         * May only be sent by the server.
         */
        CONNECTION_RATE_EXCEEDED(159),

        /**
         * Returned by the server when the maximum connection time authorized for the connection was exceeded.
         *
         * May only be sent by the server.
         */
        MAXIMUM_CONNECT_TIME(160),

        /**
         * Returned by the server when it received a SubscribePacket with a subscription identifier, but the server does
         * not support subscription identifiers.
         *
         * May only be sent by the server.
         */
        SUBSCRIPTION_IDENTIFIERS_NOT_SUPPORTED (161),

        /**
         * Returned by the server when it received a SubscribePacket with a wildcard topic filter, but the server does
         * not support wildcard topic filters.
         *
         * May only be sent by the server.
         */
        WILDCARD_SUBSCRIPTIONS_NOT_SUPPORTED(162);

        private int reasonCode;

        private DisconnectReasonCode(int code) {
            reasonCode = code;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return reasonCode;
        }

        /**
         * Creates a Java DisconnectReasonCode enum value from a native integer value.
         *
         * @param value native integer value for DisconnectReasonCode
         * @return a new DisconnectReasonCode value
         */
        public static DisconnectReasonCode getEnumValueFromInteger(int value) {
            DisconnectReasonCode enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal DisconnectReasonCode");
        }

        private static Map<Integer, DisconnectReasonCode> buildEnumMapping() {
            return Stream.of(DisconnectReasonCode.values())
                .collect(Collectors.toMap(DisconnectReasonCode::getValue, Function.identity()));
        }

        private static Map<Integer, DisconnectReasonCode> enumMapping = buildEnumMapping();
    }

    /**
     * A class to that allows for the creation of a DisconnectPacket. Set all of the settings you want in the
     * packet and then use the build() function to get a DisconnectPacket populated with the settings
     * defined in the builder.
     */
    static final public class DisconnectPacketBuilder {

        private DisconnectReasonCode reasonCode = DisconnectReasonCode.NORMAL_DISCONNECTION;
        private Long sessionExpiryIntervalSeconds;
        private String reasonString;
        private List<UserProperty> userProperties;
        private String serverReference;

        /**
         * Sets the value indicating the reason that the sender is closing the connection
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901208">MQTT5 Disconnect Reason Code</a>
         *
         * @param reasonCode Value indicating the reason that the sender is closing the connection
         * @return The DisconnectPacketBuilder after setting the reason code.
         */
        public DisconnectPacketBuilder withReasonCode(DisconnectReasonCode reasonCode) {
            this.reasonCode = reasonCode;
            return this;
        }

        /**
         * Sets the change to the session expiry interval negotiated at connection time as part of the disconnect.  Only
         * valid for DisconnectPackets sent from client to server.  It is not valid to attempt to change session expiry
         * from zero to a non-zero value.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901211">MQTT5 Session Expiry Interval</a>
         *
         * @param sessionExpiryIntervalSeconds the session expiry interval negotiated at connection time as part of the disconnect
         * @return The DisconnectPacketBuilder after setting the session expiry interval.
         */
        public DisconnectPacketBuilder withSessionExpiryIntervalSeconds(long sessionExpiryIntervalSeconds) {
            this.sessionExpiryIntervalSeconds = sessionExpiryIntervalSeconds;
            return this;
        }

        /**
         * Sets the additional diagnostic information about the reason that the sender is closing the connection
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901212">MQTT5 Reason String</a>
         *
         * @param reasonString Additional diagnostic information about the reason that the sender is closing the connection
         * @return The DisconnectPacketBuilder after setting the reason string.
         */
        public DisconnectPacketBuilder withReasonString(String reasonString) {
            this.reasonString = reasonString;
            return this;
        }

        /**
         * Sets the list of MQTT5 user properties included with the packet.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901213">MQTT5 User Property</a>
         *
         * @param userProperties List of MQTT5 user properties included with the packet.
         * @return The DisconnectPacketBuilder after setting the user properties.
         */
        public DisconnectPacketBuilder withUserProperties(List<UserProperty> userProperties) {
            this.userProperties = userProperties;
            return this;
        }

        /**
         * Sets the property indicating an alternate server that the client may temporarily or permanently attempt
         * to connect to instead of the configured endpoint.  Will only be set if the reason code indicates another
         * server may be used (ServerMoved, UseAnotherServer).
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901214">MQTT5 Server Reference</a>
         *
         * @param serverReference Property indicating an alternate server that the client may temporarily or permanently
         * attempt to connect to instead of the configured endpoint.
         * @return The DisconnectPacketBuilder after setting the server reference.
         */
        public DisconnectPacketBuilder withServerReference(String serverReference) {
            this.serverReference = serverReference;
            return this;
        }

        /**
         * Creates a new DisconnectPacketBuilder so a DisconnectPacket can be created.
         */
        public DisconnectPacketBuilder() {}

        /**
         * Creates a new DisconnectPacket using the settings set in the builder.
         * @return The DisconnectPacket created from the builder
         */
        public DisconnectPacket build()
        {
            return new DisconnectPacket(this);
        }
    }

}
