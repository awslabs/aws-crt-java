/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.CrtResource;;


public class MqttClientConnectionTest extends MqttClientConnectionFixture {
    public MqttClientConnectionTest() {
    }

    @Test
    public void testConnectDisconnect() {
        skipIfNetworkUnavailable();
        connect(AUTH_KEY_TYPE.RSA);
        disconnect();
        close();
    }

    @Test
    public void testECCKeyConnectDisconnect() {
        skipIfNetworkUnavailable();
        connect(AUTH_KEY_TYPE.ECC);
        disconnect();
        close();
    }
};
