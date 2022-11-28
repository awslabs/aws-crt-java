/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket;

/**
 * The type of data returned when a publish is made to a topic the MQTT5 client is subscribed to.
 * The data contained within can be gotten using the <code>getResult</code> functions.
 * For example, <code>getResultPublishPacket</code> will return the PublishPacket from the server.
 */
public class PublishReturn {
    // The publishPacket from the server
    private PublishPacket publishPacket;

    /**
     * Returns the PublishPacket returned from the server or Null if none was returned.
     * @return The PublishPacket returned from the server.
     */
    public PublishPacket getPublishPacket() {
        return publishPacket;
    }

    /**
     * This is only called in JNI to make a new PublishReturn with a PUBLISH packet.
     * @param newPublishPacket The PubAckPacket data for QoS 1 packets. Can be null if result is non QoS 1.
     * @return A newly created PublishResult
     */
    private PublishReturn(PublishPacket newPublishPacket) {
        this.publishPacket = newPublishPacket;
    }
}
