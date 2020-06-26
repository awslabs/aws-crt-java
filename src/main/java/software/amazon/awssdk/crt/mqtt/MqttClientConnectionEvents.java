
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt;

/** 
 * Interface used to receive connection events from the CRT 
 */
public interface MqttClientConnectionEvents {
    /**
     * connection was lost (or disconnected), reconnect will be attempted automatically until
     * disconnect() is called
     * @param errorCode AWS CRT error code, pass to {@link software.amazon.awssdk.crt.CRT#awsErrorString(int)} for a human readable error
     */
    void onConnectionInterrupted(int errorCode);

    /**
     *  called on first successful connect, and whenever a reconnect succeeds
     * @param sessionPresent true if the session has been resumed, false if the session is clean
     */
    void onConnectionResumed(boolean sessionPresent);
}
