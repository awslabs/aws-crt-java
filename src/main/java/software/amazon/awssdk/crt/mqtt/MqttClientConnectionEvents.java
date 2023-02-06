
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
     * Called when the connection was lost (or disconnected), reconnect will be attempted automatically until
     * disconnect() is called
     * @param errorCode AWS CRT error code, pass to {@link software.amazon.awssdk.crt.CRT#awsErrorString(int)} for a human readable error
     */
    void onConnectionInterrupted(int errorCode);

    /**
     * Called on first successful connect, and whenever a reconnect succeeds
     * @param sessionPresent true if the session has been resumed, false if the session is clean
     */
    void onConnectionResumed(boolean sessionPresent);

    /**
     * Called on every successful connect.
     * Optional and is not required to be defined.
     * @param data The data sent from the client alongside the successful connection callback.
     */
    default void onConnectionSuccess(OnConnectionSuccessReturn data) {};

    /**
     * Called on every unsuccessful connect. Does not get invoked in interrupts/disconnects from the server - use onConnectionInterrupted for that.
     * Optional and is not required to be defined.
     * @param data The data sent from the client alongside the failed connection callback.
     */
    default void onConnectionFailure(OnConnectionFailureReturn data) {};

    /**
     * Called when the connection was disconnected successfully.
     * Optional and is not required to be defined.
     * @param data The data sent from the client alongside the successful disconnect.
     */
    default void onConnectionClosed(OnConnectionClosedReturn data) {};
}
