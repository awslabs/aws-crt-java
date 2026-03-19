/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

import software.amazon.awssdk.crt.mqtt5.packets.ConnAckPacket;

/**
 * The data returned when OnConnectionFailure is invoked in the LifecycleEvents callback.
 * The data contained within can be gotten using the <code>get</code> functions.
 * For example, <code>getConnAckPacket</code> will return the ConnAckPacket from the server.
 */
public class OnConnectionFailureReturn {
    private int errorCode;
    private ConnAckPacket connAckPacket;

    /**
     * Returns the error code returned from the server on the connection failure.
     * Pass to {@link software.amazon.awssdk.crt.CRT#awsErrorString(int)} for a human readable error.
     * @return The error code returned from the server.
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the ConnAckPacket returned from the server on the connection failure, or Null if none was returned.
     * @return The ConnAckPacket returned from the server.
     */
    public ConnAckPacket getConnAckPacket() {
        return connAckPacket;
    }

    /**
     * This is only called in JNI to make a new OnConnectionFailureReturn.
     */
    private OnConnectionFailureReturn(int newErrorCode, ConnAckPacket newConnAckPacket)
    {
        this.errorCode = newErrorCode;
        this.connAckPacket = newConnAckPacket;
    }
}
