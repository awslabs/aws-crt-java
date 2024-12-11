/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

public class IncomingPublishEvent {

    private final byte[] payload;

    private IncomingPublishEvent(byte[] payload) {
        this.payload = payload;
    }

    /**
     * Gets the payload of the IncomingPublishEvent.
     *
     * @return Payload of the IncomingPublishEvent.
     */
    public byte[] getPayload() {
        return payload;
    }
}