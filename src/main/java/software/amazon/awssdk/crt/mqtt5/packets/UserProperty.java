/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5.packets;

/**
 * A simple key-value pair struct to define a user property.
 * A user property is a name-value pair of utf-8 strings that can be added to MQTT5 packets.
 */
public class UserProperty {

    public final String key;
    public final String value;

    public UserProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
