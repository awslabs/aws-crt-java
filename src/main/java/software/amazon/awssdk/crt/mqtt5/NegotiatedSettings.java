/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

/**
 * MQTT behavior settings that are dynamically negotiated as part of the CONNECT/CONNACK exchange.
 *
 * While you can infer all of these values from a combination of
 *   (1) defaults as specified in the MQTT5 spec
 *   (2) your CONNECT settings
 *   (3) the CONNACK from the broker
 *
 * the client instead does the combining for you and emits a NegotiatedSettings object with final, authoritative values.
 *
 * Negotiated settings are communicated with every successful connection establishment.
 */
public class NegotiatedSettings {

    private QOS maximumQOS;
    private long sessionExpiryInterval;
    private int receiveMaximumFromServer;
    private long maximumPacketSizeToServer;
    private int serverKeepAlive;
    private boolean retainAvailable;
    private boolean wildcardSubscriptionsAvailable;
    private boolean subscriptionIdentifiersAvailable;
    private boolean sharedSubscriptionsAvailable;
    private String assignedClientID;
    private boolean rejoinedSession;

    /**
     * @return Returns the maximum QoS allowed for publishes on this connection instance
     */
    public QOS getMaximumQOS() {
        return this.maximumQOS;
    }

    /**
     * @return Returns the amount of time in seconds the server will retain the MQTT session after a disconnect.
     */
    public long getSessionExpiryInterval() {
        return this.sessionExpiryInterval;
    }

    /**
     * @return Returns the number of in-flight QoS 1 and QoS 2 publications the server is willing to process concurrently.
     */
    public int getReceiveMaximumFromServer() {
        return this.receiveMaximumFromServer;
    }

    /**
     * @return Returns the maximum packet size the server is willing to accept.
     */
    public long getMaximumPacketSizeToServer() {
        return this.maximumPacketSizeToServer;
    }

    /**
     * Returns the maximum amount of time in seconds between client packets. The client should use PINGREQs to ensure this
     * limit is not breached.  The server will disconnect the client for inactivity if no MQTT packet is received
     * in a time interval equal to 1.5 x this value.
     *
     * @return The maximum amount of time in seconds between client packets.
     */
    public int getServerKeepAlive() {
        return this.serverKeepAlive;
    }

    /**
     * @return Returns whether the server supports retained messages.
     */
    public boolean getRetainAvailable() {
        return this.retainAvailable;
    }

    /**
     * @return Returns whether the server supports wildcard subscriptions.
     */
    public boolean getWildcardSubscriptionsAvailable() {
        return this.wildcardSubscriptionsAvailable;
    }

    /**
     * @return Returns whether the server supports subscription identifiers
     */
    public boolean getSubscriptionIdentifiersAvailable() {
        return this.subscriptionIdentifiersAvailable;
    }

    /**
     * @return Returns whether the server supports shared subscriptions
     */
    public boolean getSharedSubscriptionsAvailable() {
        return this.sharedSubscriptionsAvailable;
    }

    /**
     * @return Returns whether the client has rejoined an existing session.
     */
    public boolean getRejoinedSession() {
        return this.rejoinedSession;
    }

    /**
     * Returns the final client id in use by the newly-established connection.  This will be the configured client id if one
     * was given in the configuration, otherwise, if no client id was specified, this will be the client id assigned
     * by the server.  Reconnection attempts will always use the auto-assigned client id, allowing for auto-assigned
     * session resumption.
     *
     * @return The final client id in use by the newly-established connection
     */
    public String getAssignedClientID() {
        return this.assignedClientID;
    }

    public NegotiatedSettings() {}

    /**
     * A native, JNI-only helper function for more easily setting the QOS
     * @param QOSValue A int representing the QoS
     */
    private void nativeSetQOS(int QOSValue) {
        this.maximumQOS = QOS.getEnumValueFromInteger(QOSValue);
    }
}
