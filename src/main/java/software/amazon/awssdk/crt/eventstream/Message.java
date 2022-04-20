/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.CleanableCrtResource;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Wrapper around an instance of aws-event-stream-message. It's auto closable, so be sure
 * to call close when finished with the object.
 */
public class Message extends CleanableCrtResource {
    /**
     * Creates a message using headers and payload.
     * @param headers list of headers to include in the message's header block. Can be null.
     * @param payload payload body to include in the message's payload block. Can be null.
     */
    public Message(List<Header> headers, byte[] payload) {
        acquireNativeHandle(messageNew(Header.marshallHeadersForJNI(headers), payload), Message::messageDelete);
    }

    /**
     * Get the binary format of this message (i.e. for sending across the wire manually)
     * @return ByteBuffer wrapping the underlying message data. This buffer is only valid
     * as long as the message itself is valid.
     */
    public ByteBuffer getMessageBuffer() {
        return messageBuffer(getNativeHandle());
    }

    private static native long messageNew(byte[] serializedHeaders, byte[] payload);
    private static native void messageDelete(long messageHandle);
    private static native ByteBuffer messageBuffer(long messageHandle);
}
