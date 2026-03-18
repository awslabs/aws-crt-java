/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

/**
 * An opaque handle representing manual control over a publish acknowledgement for a received
 * PUBLISH packet.
 *
 * <p>This class cannot be instantiated directly. Instances are only created by the CRT library.</p>
 */
public class Mqtt5PublishAcknowledgementControlHandle {

    private final long controlId;

    /**
     * Creates a new Mqtt5PublishAcknowledgementControlHandle. Only called from native/JNI code.
     *
     * @param controlId The native publish acknowledgement control ID returned by
     *                  aws_mqtt5_client_acquire_publish_acknowledgement.
     */
    Mqtt5PublishAcknowledgementControlHandle(long controlId) {
        this.controlId = controlId;
    }

    /**
     * Returns the native publish acknowledgement control ID. Used internally by JNI.
     *
     * @return The native publish acknowledgement control ID.
     */
    long getControlId() {
        return controlId;
    }
}
