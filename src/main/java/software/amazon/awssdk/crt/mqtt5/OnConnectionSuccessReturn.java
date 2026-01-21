/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

import software.amazon.awssdk.crt.mqtt5.packets.ConnAckPacket;

/**
 * The data returned when OnConnectionSuccess is invoked in the LifecycleEvents callback.
 * The data contained within can be gotten using the <code>get</code> functions.
 * For example, <code>getResultPublishPacket</code> will return the PublishPacket from the server.
 */
public class OnConnectionSuccessReturn {
    private ConnAckPacket connAckPacket;
    private NegotiatedSettings negotiatedSettings;

    /**
     * Returns the ConnAckPacket returned from the server on the connection success or Null if none was returned.
     * @return The ConnAckPacket returned from the server.
     */
    public ConnAckPacket getConnAckPacket() {
        return connAckPacket;
    }

    /**
     * Returns the NegotiatedSettings returned from the server on the connection success or Null if none was returned.
     * @return The NegotiatedSettings returned from the server.
     */
    public NegotiatedSettings getNegotiatedSettings() {
        return negotiatedSettings;
    }

    /**
     * This is only called in JNI to make a new OnConnectionSuccessReturn.
     */
    private OnConnectionSuccessReturn(ConnAckPacket newConnAckPacket, NegotiatedSettings newNegotiatedSettings)
    {
        this.connAckPacket = newConnAckPacket;
        this.negotiatedSettings = newNegotiatedSettings;
    }
}
