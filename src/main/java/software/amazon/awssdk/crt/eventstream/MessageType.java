/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.eventstream;

/**
 * Java mirror of the native aws_event_stream_rpc_message_type enum, specifying the type of rpc message
 */
public enum MessageType {
    ApplicationMessage((byte)0),
    ApplicationError((byte)1),
    Ping((byte)2),
    PingResponse((byte)3),
    Connect((byte)4),
    ConnectAck((byte)5),
    ProtocolError((byte)6),
    ServerError((byte)7);

    private byte enumValue;

    MessageType(byte enumValue) {
        this.enumValue = enumValue;
    }

    /**
     * @return the native enum value associated with this Java enum value
     */
    public byte getEnumValue() {
        return this.enumValue;
    }

    /**
     * Create a MessageType enum value from a native enum value
     * @param enumValue native enum value
     * @return a new MessageType enum value
     */
    public static MessageType fromEnumValue(int enumValue) {
        for (MessageType type : MessageType.values()) {
            if (type.enumValue == enumValue) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown MessageType enum value: " + enumValue);
    }
}
