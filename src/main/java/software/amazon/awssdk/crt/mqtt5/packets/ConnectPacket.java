/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5.packets;

import java.util.List;

/**
 * Data model of an <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901033">MQTT5 CONNECT</a> packet.
 */
public class ConnectPacket {

    private Long keepAliveIntervalSeconds;
    private String clientId;
    private String username;
    private byte[] password;
    private Long sessionExpiryIntervalSeconds;
    private Boolean requestResponseInformation;
    private Boolean requestProblemInformation;
    private Long receiveMaximum;
    private Long maximumPacketSizeBytes;
    private Long willDelayIntervalSeconds;
    private PublishPacket will;
    private List<UserProperty> userProperties;

    /**
     * Creates a ConnectPacket instance using the provided ConnectPacketBuilder
     */
    private ConnectPacket(ConnectPacketBuilder builder) {
        this.keepAliveIntervalSeconds = builder.keepAliveIntervalSeconds;
        this.clientId = builder.clientId;
        this.username = builder.username;
        this.password = builder.password;
        this.sessionExpiryIntervalSeconds = builder.sessionExpiryIntervalSeconds;
        this.requestResponseInformation = builder.requestResponseInformation;
        this.requestProblemInformation = builder.requestProblemInformation;
        this.receiveMaximum = builder.receiveMaximum;
        this.maximumPacketSizeBytes = builder.maximumPacketSizeBytes;
        this.willDelayIntervalSeconds = builder.willDelayIntervalSeconds;
        this.will = builder.will;
        this.userProperties = builder.userProperties;
    }

    /**
     * Returns the maximum time interval, in seconds, that is permitted to elapse between the point at which the client
     * finishes transmitting one MQTT packet and the point it starts sending the next.  The client will use
     * PINGREQ packets to maintain this property.
     *
     * If the responding ConnAckPacket contains a keep alive property value, then that is the negotiated keep alive value.
     * Otherwise, the keep alive sent by the client is the negotiated value.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901045">MQTT5 Keep Alive</a>
     *
     * @return The maximum time interval, in seconds, that is permitted to elapse between the point at which the client
     * finishes transmitting one MQTT packet and the point it starts sending the next.
     */
    public Long getKeepAliveIntervalSeconds()
    {
        return this.keepAliveIntervalSeconds;
    }

    /**
     * Returns a unique string identifying the client to the server.  Used to restore session state between connections.
     *
     * If left empty, the broker will auto-assign a unique client id.  When reconnecting, the Mqtt5Client will
     * always use the auto-assigned client id.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901059">MQTT5 Client Identifier</a>
     *
     * @return A unique string identifying the client to the server.
     */
    public String getClientId()
    {
        return this.clientId;
    }

    /**
     * Returns a string value that the server may use for client authentication and authorization.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901071">MQTT5 User Name</a>
     *
     * @return A string value that the server may use for client authentication and authorization.
     */
    public String getUsername()
    {
        return this.username;
    }

    /**
     * Returns opaque binary data that the server may use for client authentication and authorization.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901072">MQTT5 Password</a>
     *
     * @return Opaque binary data that the server may use for client authentication and authorization.
     */
    public byte[] getPassword()
    {
        return this.password;
    }

    /**
     * Returns a time interval, in seconds, that the client requests the server to persist this connection's MQTT session state
     * for.  Has no meaning if the client has not been configured to rejoin sessions.  Must be non-zero in order to
     * successfully rejoin a session.
     *
     * If the responding ConnAckPacket contains a session expiry property value, then that is the negotiated session expiry
     * value.  Otherwise, the session expiry sent by the client is the negotiated value.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901048">MQTT5 Session Expiry Interval</a>
     *
     * @return A time interval, in seconds, that the client requests the server to persist this connection's MQTT session
     * state for.
     */
    public Long getSessionExpiryIntervalSeconds()
    {
        return this.sessionExpiryIntervalSeconds;
    }

    /**
     * Returns a boolean that, if true, requests that the server send response information in the subsequent ConnAckPacket.  This response
     * information may be used to set up request-response implementations over MQTT, but doing so is outside
     * the scope of the MQTT5 spec and client.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901052">MQTT5 Request Response Information</a>
     *
     * @return If true, requests that the server send response information in the subsequent ConnAckPacket.
     */
    public Boolean getRequestResponseInformation()
    {
        return this.requestResponseInformation;
    }

    /**
     * Returns a boolean that, if true, requests that the server send additional diagnostic information (via response string or
     * user properties) in DisconnectPacket or ConnAckPacket from the server.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901053">MQTT5 Request Problem Information</a>
     *
     * @return If true, requests that the server send additional diagnostic information (via response string or
     * user properties) in DisconnectPacket or ConnAckPacket from the server.
     */
    public Boolean getRequestProblemInformation()
    {
        return this.requestProblemInformation;
    }

    /**
     * Returns the maximum number of in-flight QoS 1 and 2 messages the client is willing to handle.  If
     * omitted or null, then no limit is requested.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901049">MQTT5 Receive Maximum</a>
     *
     * @return The maximum number of in-flight QoS 1 and 2 messages the client is willing to handle.
     */
    public Long getReceiveMaximum()
    {
        return this.receiveMaximum;
    }

    /**
     * Returns the maximum packet size the client is willing to handle.  If
     * omitted or null, then no limit beyond the natural limits of MQTT packet size is requested.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901050">MQTT5 Maximum Packet Size</a>
     *
     * @return The maximum packet size the client is willing to handle
     */
    public Long getMaximumPacketSizeBytes()
    {
        return this.maximumPacketSizeBytes;
    }

    /**
     * Returns a time interval, in seconds, that the server should wait (for a session reconnection) before sending the
     * will message associated with the connection's session.  If omitted or null, the server will send the will when the
     * associated session is destroyed.  If the session is destroyed before a will delay interval has elapsed, then
     * the will must be sent at the time of session destruction.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901062">MQTT5 Will Delay Interval</a>
     *
     * @return A time interval, in seconds, that the server should wait (for a session reconnection) before sending the
     * will message associated with the connection's session.
     */
    public Long getWillDelayIntervalSeconds()
    {
        return this.willDelayIntervalSeconds;
    }

    /**
     * Returns the definition of a message to be published when the connection's session is destroyed by the server or when
     * the will delay interval has elapsed, whichever comes first.  If null, then nothing will be sent.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901040">MQTT5 Will</a>
     *
     * @return The message to be published when the connection's session is destroyed by the server or when
     * the will delay interval has elapsed, whichever comes first.
     */
    public PublishPacket getWill()
    {
        return this.will;
    }

    /**
     * Returns a list of MQTT5 user properties included with the packet.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901054">MQTT5 User Property</a>
     *
     * @return List of MQTT5 user properties included with the packet.
     */
    public List<UserProperty> getUserProperties()
    {
        return this.userProperties;
    }

    /*******************************************************************************
     * builder
     ******************************************************************************/

    /**
     * A class to that allows for the creation of a ConnectPacket. Set all of the settings you want in the
     * packet and then use the build() function to get a ConnectPacket populated with the settings
     * defined in the builder.
     */
    static final public class ConnectPacketBuilder {
        private Long keepAliveIntervalSeconds = 1200L;
        private String clientId;
        private String username;
        private byte[] password;
        private Long sessionExpiryIntervalSeconds;
        private Boolean requestResponseInformation;
        private Boolean requestProblemInformation;
        private Long receiveMaximum;
        private Long maximumPacketSizeBytes;
        private Long willDelayIntervalSeconds;
        private PublishPacket will = null;
        private List<UserProperty> userProperties;

        /**
         * Sets the maximum time interval, in seconds, that is permitted to elapse between the point at which the client
         * finishes transmitting one MQTT packet and the point it starts sending the next.  The client will use
         * PINGREQ packets to maintain this property.
         *
         * If the responding ConnAckPacket contains a keep alive property value, then that is the negotiated keep alive value.
         * Otherwise, the keep alive sent by the client is the negotiated value.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901045">MQTT5 Keep Alive</a>
         *
         * NOTE: The keepAliveIntervalSeconds HAS to be larger than the pingTimeoutMs time set in the Mqtt5ClientOptions.
         *
         * @param keepAliveInteralSeconds the maximum time interval, in seconds, that is permitted to elapse between the point
         * at which the client finishes transmitting one MQTT packet and the point it starts sending the next.
         * @return The ConnectPacketBuilder after setting the keep alive interval.
         */
        public ConnectPacketBuilder withKeepAliveIntervalSeconds(Long keepAliveInteralSeconds)
        {
            this.keepAliveIntervalSeconds = keepAliveInteralSeconds;
            return this;
        }

        /**
         * Sets the unique string identifying the client to the server.  Used to restore session state between connections.
         *
         * If left empty, the broker will auto-assign a unique client id.  When reconnecting, the Mqtt5Client will
         * always use the auto-assigned client id.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901059">MQTT5 Client Identifier</a>
         *
         * @param clientId A unique string identifying the client to the server.
         * @return The ConnectPacketBuilder after setting the client ID.
         */
        public ConnectPacketBuilder withClientId(String clientId)
        {
            this.clientId = clientId;
            return this;
        }

        /**
         * Sets the string value that the server may use for client authentication and authorization.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901071">MQTT5 User Name</a>
         *
         * @param username The string value that the server may use for client authentication and authorization.
         * @return The ConnectPacketBuilder after setting the username.
         */
        public ConnectPacketBuilder withUsername(String username)
        {
            this.username = username;
            return this;
        }

        /**
         * Sets the opaque binary data that the server may use for client authentication and authorization.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901072">MQTT5 Password</a>
         *
         * @param password Opaque binary data that the server may use for client authentication and authorization.
         * @return The ConnectPacketBuilder after setting the password.
         */
        public ConnectPacketBuilder withPassword(byte[] password)
        {
            this.password = password;
            return this;
        }

        /**
         * Sets the time interval, in seconds, that the client requests the server to persist this connection's MQTT session state
         * for.  Has no meaning if the client has not been configured to rejoin sessions.  Must be non-zero in order to
         * successfully rejoin a session.
         *
         * If the responding ConnAckPacket contains a session expiry property value, then that is the negotiated session expiry
         * value.  Otherwise, the session expiry sent by the client is the negotiated value.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901048">MQTT5 Session Expiry Interval</a>
         *
         * @param sessionExpiryIntervalSeconds A time interval, in seconds, that the client requests the server to persist this
         * connection's MQTT session state for.
         * @return The ConnectPacketBuilder after setting the session expiry interval.
         */
        public ConnectPacketBuilder withSessionExpiryIntervalSeconds(Long sessionExpiryIntervalSeconds)
        {
            this.sessionExpiryIntervalSeconds = sessionExpiryIntervalSeconds;
            return this;
        }

        /**
         * Sets whether requests that the server send response information in the subsequent ConnAckPacket.  This response
         * information may be used to set up request-response implementations over MQTT, but doing so is outside
         * the scope of the MQTT5 spec and client.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901052">MQTT5 Request Response Information</a>
         *
         * @param requestResponseInformation If true, requests that the server send response information in the subsequent ConnAckPacket.
         * @return The ConnectPacketBuilder after setting the request response information.
         */
        public ConnectPacketBuilder withRequestResponseInformation(Boolean requestResponseInformation)
        {
            this.requestResponseInformation = requestResponseInformation;
            return this;
        }

        /**
         * Sets whether requests that the server send additional diagnostic information (via response string or
         * user properties) in DisconnectPacket or ConnAckPacket from the server.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901053">MQTT5 Request Problem Information</a>
         *
         * @param requestProblemInformation If true, requests that the server send additional diagnostic information
         * (via response string or user properties) in DisconnectPacket or ConnAckPacket from the server.
         * @return The ConnectPacketBuilder after setting the request problem information.
         */
        public ConnectPacketBuilder withRequestProblemInformation(Boolean requestProblemInformation)
        {
            this.requestProblemInformation = requestProblemInformation;
            return this;
        }

        /**
         * Sets the maximum number of in-flight QoS 1 and 2 messages the client is willing to handle.  If
         * omitted or null, then no limit is requested.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901049">MQTT5 Receive Maximum</a>
         *
         * @param receiveMaximum The maximum number of in-flight QoS 1 and 2 messages the client is willing to handle.
         * @return The ConnectPacketBuilder after setting the receive maximum.
         */
        public ConnectPacketBuilder withReceiveMaximum(Long receiveMaximum)
        {
            this.receiveMaximum = receiveMaximum;
            return this;
        }

        /**
         * Sets the maximum packet size the client is willing to handle.  If
         * omitted or null, then no limit beyond the natural limits of MQTT packet size is requested.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901050">MQTT5 Maximum Packet Size</a>
         *
         * @param maximumPacketSizeBytes The maximum packet size the client is willing to handle
         * @return The ConnectPacketBuilder after setting the maximum packet size.
         */
        public ConnectPacketBuilder withMaximumPacketSizeBytes(Long maximumPacketSizeBytes)
        {
            this.maximumPacketSizeBytes = maximumPacketSizeBytes;
            return this;
        }

        /**
         * Sets the time interval, in seconds, that the server should wait (for a session reconnection) before sending the
         * will message associated with the connection's session.  If omitted or null, the server will send the will when the
         * associated session is destroyed.  If the session is destroyed before a will delay interval has elapsed, then
         * the will must be sent at the time of session destruction.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901062">MQTT5 Will Delay Interval</a>
         *
         * @param willDelayIntervalSeconds A time interval, in seconds, that the server should wait (for a session reconnection)
         * before sending the will message associated with the connection's session.
         * @return The ConnectPacketBuilder after setting the will message delay interval.
         */
        public ConnectPacketBuilder withWillDelayIntervalSeconds(Long willDelayIntervalSeconds)
        {
            this.willDelayIntervalSeconds = willDelayIntervalSeconds;
            return this;
        }

        /**
         * Sets the definition of a message to be published when the connection's session is destroyed by the server or when
         * the will delay interval has elapsed, whichever comes first.  If null, then nothing will be sent.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901040">MQTT5 Will</a>
         *
         * @param will The message to be published when the connection's session is destroyed by the server or when
         * the will delay interval has elapsed, whichever comes first.
         * @return The ConnectPacketBuilder after setting the will message.
         */
        public ConnectPacketBuilder withWill(PublishPacket will)
        {
            this.will = will;
            return this;
        }

        /**
         * Sets the list of MQTT5 user properties included with the packet.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901054">MQTT5 User Property</a>
         *
         * @param userProperties List of MQTT5 user properties included with the packet.
         * @return The ConnectPacketBuilder after setting the user properties.
         */
        public ConnectPacketBuilder withUserProperties(List<UserProperty> userProperties)
        {
            this.userProperties = userProperties;
            return this;
        }

        /**
         * Creates a new ConnectPacketBuilder so a ConnectPacket can be created.
         */
        public ConnectPacketBuilder() {}

        /**
         * Creates a new ConnectPacket using the settings set in the builder.
         * @return The ConnectPacket created from the builder
         */
        public ConnectPacket build()
        {
            return new ConnectPacket(this);
        }
    }
}
