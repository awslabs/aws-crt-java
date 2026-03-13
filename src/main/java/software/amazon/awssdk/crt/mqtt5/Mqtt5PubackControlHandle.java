/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

/**
 * An opaque handle representing manual control over a QoS 1 PUBACK for a received PUBLISH packet.
 *
 * <p>This class cannot be instantiated directly. Instances are only created by the CRT library.</p>
 */
public class Mqtt5PubackControlHandle {

    private final long controlId;

    /**
     * Creates a new Mqtt5PubackControlHandle. Only called from native/JNI code.
     *
     * @param controlId The native puback control ID returned by aws_mqtt5_client_acquire_puback.
     */
    Mqtt5PubackControlHandle(long controlId) {
        this.controlId = controlId;
    }

    /**
     * Returns the native puback control ID. Used internally by JNI.
     *
     * @return The native puback control ID.
     */
    long getControlId() {
        return controlId;
    }
}