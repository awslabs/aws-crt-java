/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Data model of an <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc384800445">MQTT5 UNSUBSCRIBE</a> packet.
 */
public class UnsubscribePacket {

    private List<String> subscriptions;
    private List<UserProperty> userProperties;

    private UnsubscribePacket(UnsubscribePacketBuilder builder) {
        this.userProperties = builder.userProperties;
        this.subscriptions = builder.subscriptions;
    }

    /**
     * Creates an {@link UnsubscribePacket} containing only a single subscription topic to unsubscribe from:
     * <em>topicFilter</em>.
     * <p>
     * Internally this is just syntactic sugar around
     * {@link UnsubscribePacketBuilder#UnsubscribePacketBuilder(String)}
     * followed by {@link UnsubscribePacketBuilder#build()}.
     * 
     * @param topicFilter The topic filter to unsubscribe from.
     * @return an immutable {@code UnsubscribePacket} ready for use
     * 
     * @throws NullPointerException if {@code topicFilter} is {@code null}
     */
    public static UnsubscribePacket of(String topicFilter) {
        Objects.requireNonNull(topicFilter, "topicFilter");

        return new UnsubscribePacketBuilder(topicFilter).build();
    }

    /**
     * Returns a list of subscriptions that the client wishes to unsubscribe from.
     *
     * @return List of subscriptions that the client wishes to unsubscribe from.
     */
    public List<String> getSubscriptions() {
        return this.subscriptions;
    }

    /**
     * Returns a list of MQTT5 user properties included with the packet.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901184">MQTT5 User Property</a>
     *
     * @return List of MQTT5 user properties included with the packet.
     */
    public List<UserProperty> getUserProperties() {
        return this.userProperties;
    }

    /**
     * A class to that allows for the creation of a UnsubscribePacket. Set all of the settings you want in the
     * packet and then use the build() function to get a UnsubscribePacket populated with the settings
     * defined in the builder.
     */
    static final public class UnsubscribePacketBuilder {
        private List<String> subscriptions;
        private List<UserProperty> userProperties;

        /**
         * Sets a single topic filter that the client wishes to unsubscribe from.
         *
         * @param topicFilter A single topic filter that the client wishes to unsubscribe from.
         * @return The UnsubscribePacketBuilder after setting the subscription.
         */
        public UnsubscribePacketBuilder withSubscription(String topicFilter) {
            if (this.subscriptions == null) {
                this.subscriptions = new ArrayList<String>();
            }
            this.subscriptions.add(topicFilter);
            return this;
        }

        /**
         * Sets the list of MQTT5 user properties included with the packet.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901184">MQTT5 User Property</a>
         *
         * @param userProperties List of MQTT5 user properties included with the packet.
         * @return The UnsubscribePacketBuilder after setting the user properties.
         */
        public UnsubscribePacketBuilder withUserProperties(List<UserProperty> userProperties) {
            this.userProperties = userProperties;
            return this;
        }

        /**
         * Creates a new UnsubscribePacketBuilder so a UnsubscribePacket can be created.
         */
        public UnsubscribePacketBuilder() {}

        /**
         * Creates a new UnsubscribePacketBuilder with one subscription defined.
         *
         * @param topicFilter A single topic filter that the client wishes to unsubscribe from.
         */
        public UnsubscribePacketBuilder(String topicFilter) {
            withSubscription(topicFilter);
        }

        /**
         * Creates a new UnsubscribePacket using the settings set in the builder.
         * @return The UnsubscribePacket created from the builder
         */
        public UnsubscribePacket build() {
            return new UnsubscribePacket(this);
        }
    }
}
