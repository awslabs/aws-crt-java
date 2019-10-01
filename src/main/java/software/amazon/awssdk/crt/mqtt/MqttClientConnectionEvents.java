
/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
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
