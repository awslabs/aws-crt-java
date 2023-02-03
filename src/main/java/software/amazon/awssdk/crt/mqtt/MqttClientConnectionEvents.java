
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

    /**
     * Called when a connection was successful.
     * @param data The data sent from the client alongside the successful connection callback.
     */
    default void onConnectionSuccess(OnConnectionSuccessReturn data) {};

    /**
     * Called when a connection was unsuccessful.
     * @param data The data sent from the client alongside the failed connection callback.
     */
    default void onConnectionFailure(OnConnectionFailureReturn data) {};

    /**
     * called when the connection was disconnected with user-initiated disconnect successfully.
     * @param data The data sent from the client alongside the successful disconnect.
     */
    default void onConnectionClosed(OnConnectionClosedReturn data) {};
}
