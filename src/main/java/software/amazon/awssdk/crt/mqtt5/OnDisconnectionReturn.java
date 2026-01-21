/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

import software.amazon.awssdk.crt.mqtt5.packets.DisconnectPacket;

/**
 * The data returned when OnDisconnect is invoked in the LifecycleEvents callback.
 * The data contained within can be gotten using the <code>get</code> functions.
 * For example, <code>getDisconnectPacket</code> will return the DisconnectPacket from the server.
 */
public class OnDisconnectionReturn {
    private int errorCode;
    private DisconnectPacket disconnectPacket;

    /**
     * Returns the error code returned from the server on the disconnection.
     * Pass to {@link software.amazon.awssdk.crt.CRT#awsErrorString(int)} for a human readable error.
     * @return The error code returned from the server.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the ConnAckPacket returned from the server on the disconnection, or Null if none was returned.
     * @return The ConnAckPacket returned from the server.
     */
    public DisconnectPacket getDisconnectPacket() {
        return disconnectPacket;
    }

    /**
     * This is only called in JNI to make a new OnDisconnectionReturn.
     */
    private OnDisconnectionReturn(int newErrorCode, DisconnectPacket newDisconnectPacket)
    {
        this.errorCode = newErrorCode;
        this.disconnectPacket = newDisconnectPacket;
    }
}
