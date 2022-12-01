/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5.packets;

import software.amazon.awssdk.crt.mqtt5.QOS;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Data model of an <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901074">MQTT5 CONNACK</a> packet.
 */
public class ConnAckPacket {

    private boolean sessionPresent;
    private ConnectReasonCode reasonCode;

    private Long sessionExpiryIntervalSeconds;
    private Integer receiveMaximum;
    private QOS maximumQOS;
    private Boolean retainAvailable;
    private Long maximumPacketSize;
    private String assignedClientIdentifier;
    private String reasonString;

    private List<UserProperty> userProperties;

    private Boolean wildcardSubscriptionsAvailable;
    private Boolean subscriptionIdentifiersAvailable;
    private Boolean sharedSubscriptionsAvailable;

    private Integer serverKeepAlive;
    private String responseInformation;
    private String serverReference;

    /**
     * Returns true if the client rejoined an existing session on the server, false otherwise.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901078">MQTT5 Session Present</a>
     *
     * @return True if the client rejoined an existing session on the server, false otherwise.
     */
    public boolean getSessionPresent() {
        return this.sessionPresent;
    }

    /**
     * Returns an indicator that is either success or the reason for failure for the connection attempt.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901079">MQTT5 Connect Reason Code</a>
     *
     * @return Code indicating either success or the reason for failure for the connection attempt.
     */
    public ConnectReasonCode getReasonCode() {
        return this.reasonCode;
    }

    /**
     * Returns a time interval, in seconds, that the server will persist this connection's MQTT session state
     * for.  If present, this value overrides any session expiry specified in the preceding ConnectPacket.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901082">MQTT5 Session Expiry Interval</a>
     *
     * @return A time interval, in seconds, that the server will persist this connection's MQTT session state for.
     */
    public Long getSessionExpiryInterval() {
        return this.sessionExpiryIntervalSeconds;
    }

    /**
     * Returns the maximum amount of in-flight QoS 1 or 2 messages that the server is willing to handle at once. If omitted or null,
     * the limit is based on the valid MQTT packet id space (65535).
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901083">MQTT5 Receive Maximum</a>
     *
     * @return The maximum amount of in-flight QoS 1 or 2 messages that the server is willing to handle at once.
     */
    public Integer getReceiveMaximum() {
        return this.receiveMaximum;
    }

    /**
     * Returns the maximum message delivery quality of service that the server will allow on this connection.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901084">MQTT5 Maximum QoS</a>
     *
     * @return The maximum message delivery quality of service that the server will allow on this connection.
     */
    public QOS getMaximumQOS() {
        return this.maximumQOS;
    }

    /**
     * Returns an indicator whether the server supports retained messages.  If null, retained messages are
     * supported.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901085">MQTT5 Retain Available</a>
     *
     * @return Whether the server supports retained messages
     */
    public Boolean getRetainAvailable() {
        return this.retainAvailable;
    }

    /**
     * Returns the maximum packet size, in bytes, that the server is willing to accept.  If null, there
     * is no limit beyond what is imposed by the MQTT spec itself.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901086">MQTT5 Maximum Packet Size</a>
     *
     * @return The maximum packet size, in bytes, that the server is willing to accept.
     */
    public Long getMaximumPacketSize() {
        return this.maximumPacketSize;
    }

    /**
     * Returns a client identifier assigned to this connection by the server.  Only valid when the client id of
     * the preceding ConnectPacket was left empty.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901087">MQTT5 Assigned Client Identifier</a>
     *
     * @return Client identifier assigned to this connection by the server
     */
    public String getAssignedClientIdentifier() {
        return this.assignedClientIdentifier;
    }

    /**
     * Returns additional diagnostic information about the result of the connection attempt.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901089">MQTT5 Reason String</a>
     *
     * @return Additional diagnostic information about the result of the connection attempt.
     */
    public String getReasonString() {
        return this.reasonString;
    }

    /**
     * Returns a list of MQTT5 user properties included with the packet.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901090">MQTT5 User Property</a>
     *
     * @return List of MQTT5 user properties included with the packet.
     */
    public List<UserProperty> getUserProperties() {
        return this.userProperties;
    }

    /**
     * Returns whether the server supports wildcard subscriptions.  If null, wildcard subscriptions
     * are supported.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901091">MQTT5 Wildcard Subscriptions Available</a>
     *
     * @return Whether the server supports wildcard subscriptions.
     */
    public Boolean getWildcardSubscriptionsAvailable() {
        return this.wildcardSubscriptionsAvailable;
    }

    /**
     * Returns whether the server supports subscription identifiers.  If null, subscription identifiers
     * are supported.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901092">MQTT5 Subscription Identifiers Available</a>
     *
     * @return whether the server supports subscription identifiers.
     */
    public Boolean getSubscriptionIdentifiersAvailable() {
        return this.subscriptionIdentifiersAvailable;
    }

    /**
     * Returns whether the server supports shared subscription topic filters.  If null, shared subscriptions
     * are supported.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901093">MQTT5 Shared Subscriptions Available</a>
     *
     * @return whether the server supports shared subscription topic filters.
     */
    public Boolean getSharedSubscriptionsAvailable() {
        return this.sharedSubscriptionsAvailable;
    }

    /**
     * Returns server-requested override of the keep alive interval, in seconds.  If null, the keep alive value sent
     * by the client should be used.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901094">MQTT5 Server Keep Alive</a>
     *
     * @return Server-requested override of the keep alive interval, in seconds
     */
    public Integer getServerKeepAlive() {
        return this.serverKeepAlive;
    }

    /**
     * Returns a value that can be used in the creation of a response topic associated with this connection. MQTT5-based
     * request/response is outside the purview of the MQTT5 spec and this client.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901095">MQTT5 Response Information</a>
     *
     * @return A value that can be used in the creation of a response topic associated with this connection.
     */
    public String getResponseInformation() {
        return this.responseInformation;
    }

    /**
     * Returns property indicating an alternate server that the client may temporarily or permanently attempt
     * to connect to instead of the configured endpoint.  Will only be set if the reason code indicates another
     * server may be used (ServerMoved, UseAnotherServer).
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901096">MQTT5 Server Reference</a>
     *
     * @return Property indicating an alternate server that the client may temporarily or permanently attempt
     * to connect to instead of the configured endpoint.
     */
    public String getServerReference() {
        return this.serverReference;
    }

    public ConnAckPacket() {}

    /**
     * A native, JNI-only helper function for more easily setting the QOS
     * @param QOSValue A int representing the QoS
     */
    private void nativeAddMaximumQOS(int QOSValue) {
        this.maximumQOS = QOS.getEnumValueFromInteger(QOSValue);
    }

    /**
     * A native, JNI-only helper function for more easily setting the reason code
     * @param reasonCode A int representing the reason code
     */
    private void nativeAddReasonCode(int reasonCode) {
        this.reasonCode = ConnectReasonCode.getEnumValueFromInteger(reasonCode);
    }

    /**
     * Server return code for connect attempts.
     *
     * Enum values match <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901079">MQTT5 spec</a> encoding values.
     */
    public enum ConnectReasonCode {

        /**
         * Returned when the connection is accepted.
         */
        SUCCESS(0),

        /**
         * Returned when the server has a failure but does not want to specify a reason or none
         * of the other reason codes apply.
         */
        UNSPECIFIED_ERROR(128),

        /**
         * Returned when data in the ConnectPacket could not be correctly parsed by the server.
         */
        MALFORMED_PACKET(129),

        /**
         * Returned when data in the ConnectPacket does not conform to the MQTT5 specification requirements.
         */
        PROTOCOL_ERROR(130),

        /**
         * Returned when the ConnectPacket is valid but was not accepted by the server.
         */
        IMPLEMENTATION_SPECIFIC_ERROR(131),

        /**
         * Returned when the server does not support MQTT5 protocol version specified in the connection.
         */
        UNSUPPORTED_PROTOCOL_VERSION(132),

        /**
         * Returned when the client identifier in the ConnectPacket is a valid string but not one that
         * is allowed on the server.
         */
        CLIENT_IDENTIFIER_NOT_VALID(133),

        /**
         * Returned when the server does not accept the username and/or password specified by the client
         * in the connection packet.
         */
        BAD_USERNAME_OR_PASSWORD(134),

        /**
         * Returned when the client is not authorized to connect to the server.
         */
        NOT_AUTHORIZED(135),

        /**
         * Returned when the MQTT5 server is not available.
         */
        SERVER_UNAVAILABLE(136),

        /**
         * Returned when the server is too busy to make a connection. It is recommended that the client try again later.
         */
        SERVER_BUSY(137),

        /**
         * Returned when the client has been banned by the server.
         */
        BANNED(138),

        /**
         * Returned when the authentication method used in the connection is either nor supported on the server or it does
         * not match the authentication method currently in use in the ConnectPacket.
         */
        BAD_AUTHENTICATION_METHOD(140),

        /**
         * Returned when the Will topic name sent in the ConnectPacket is correctly formed, but is not accepted by
         * the server.
         */
        TOPIC_NAME_INVALID(144),

        /**
         * Returned when the ConnectPacket exceeded the maximum permissible size on the server.
         */
        PACKET_TOO_LARGE(149),

        /**
         * Returned when the quota limits set on the server have been met and/or exceeded.
         */
        QUOTA_EXCEEDED(151),

        /**
         * Returned when the Will payload in the ConnectPacket does not match the specified payload format indicator.
         */
        PAYLOAD_FORMAT_INVALID(153),

        /**
         * Returned when the server does not retain messages but the ConnectPacket on the client had Will retain enabled.
         */
        RETAIN_NOT_SUPPORTED(154),

        /**
         * Returned when the server does not support the QOS setting set in the Will QOS in the ConnectPacket.
         */
        QOS_NOT_SUPPORTED(155),

        /**
         * Returned when the server is telling the client to temporarily use another server instead of the one they
         * are trying to connect to.
         */
        USE_ANOTHER_SERVER(156),

        /**
         * Returned when the server is telling the client to permanently use another server instead of the one they
         * are trying to connect to.
         */
        SERVER_MOVED(157),

        /**
         * Returned when the server connection rate limit has been exceeded.
         */
        CONNECTION_RATE_EXCEEDED(159);

        private int reasonCode;

        private ConnectReasonCode(int code) {
            reasonCode = code;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return reasonCode;
        }

        /**
         * Creates a Java ConnectReasonCode enum value from a native integer value.
         *
         * @param value native integer value for ConnectReasonCode
         * @return a new ConnectReasonCode value
         */
        public static ConnectReasonCode getEnumValueFromInteger(int value) {
            ConnectReasonCode enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal ConnectReasonCode");
        }

        private static Map<Integer, ConnectReasonCode> buildEnumMapping() {
            return Stream.of(ConnectReasonCode.values())
                .collect(Collectors.toMap(ConnectReasonCode::getValue, Function.identity()));
        }

        private static Map<Integer, ConnectReasonCode> enumMapping = buildEnumMapping();
    }
}
