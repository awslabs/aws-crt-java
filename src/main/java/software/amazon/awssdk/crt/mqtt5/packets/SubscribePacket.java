/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5.packets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import software.amazon.awssdk.crt.mqtt5.QOS;

/**
 * Data model of an <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901161">MQTT5 SUBSCRIBE</a> packet.
 */
public class SubscribePacket {

    private List<Subscription> subscriptions;
    private Long subscriptionIdentifier;
    private List<UserProperty> userProperties;

    private SubscribePacket(SubscribePacketBuilder builder) {
        this.subscriptions = builder.subscriptions;
        this.subscriptionIdentifier = builder.subscriptionIdentifier;
        this.userProperties = builder.userProperties;
    }

    /**
     * Returns the list of subscriptions that the client wishes to listen to
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901168">MQTT5 Subscribe Payload</a>
     *
     * @return List of subscriptions that the client wishes to listen to
     */
    public List<Subscription> getSubscriptions() {
        return this.subscriptions;
    }

    /**
     * Returns the positive long to associate with all subscriptions in this request.  Publish packets that match
     * a subscription in this request should include this identifier in the resulting message.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901166">MQTT5 Subscription Identifier</a>
     *
     * @return A positive long to associate with all subscriptions in this request.
     */
    public Long getSubscriptionIdentifier() {
        return this.subscriptionIdentifier;
    }

    /**
     * Returns the list of MQTT5 user properties included with the packet.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901167">MQTT5 User Property</a>
     *
     * @return List of MQTT5 user properties included with the packet.
     */
    public List<UserProperty> getUserProperties() {
        return this.userProperties;
    }

    /**
     * Configures how retained messages should be handled when subscribing with a subscription that matches topics with
     * associated retained messages.
     *
     * Enum values match <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901169">MQTT5 spec</a> encoding values.
     */
    public enum RetainHandlingType {

        /**
         * The server should always send all retained messages on topics that match a subscription's filter.
         */
        SEND_ON_SUBSCRIBE(0),

        /**
         * The server should send retained messages on topics that match the subscription's filter, but only for the
         * first matching subscription, per session.
         */
        SEND_ON_SUBSCRIBE_IF_NEW(1),

        /**
         * Subscriptions must not trigger any retained message publishes from the server.
         */
        DONT_SEND(2);

        private int type;

        private RetainHandlingType(int code) {
            type = code;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return type;
        }

        /**
         * Creates a Java RetainHandlingType enum value from a native integer value.
         *
         * @param value native integer value for RetainHandlingType
         * @return a new RetainHandlingType value
         */
        public static RetainHandlingType getEnumValueFromInteger(int value) {
            RetainHandlingType enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal RetainHandlingType");
        }

        private static Map<Integer, RetainHandlingType> buildEnumMapping() {
            return Stream.of(RetainHandlingType.values())
                .collect(Collectors.toMap(RetainHandlingType::getValue, Function.identity()));
        }

        private static Map<Integer, RetainHandlingType> enumMapping = buildEnumMapping();
    }

    /**
     * Configures a single subscription within a Subscribe operation
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901169">MQTT5 Subscription Options</a>
     */
    static final public class Subscription {

        private String topicFilter;
        private QOS qos;
        private Boolean noLocal;
        private Boolean retainAsPublished;
        private RetainHandlingType retainHandlingType;

        /**
         * Creates a new subscription within a subscribe operation
         *
         * @param topicFilter The topic filter to subscribe to
         * @param qos The maximum QoS on which the subscriber will accept publish messages
         * @param noLocal Whether the server will not send publishes to a client when that client was the one who
         * sent the publish
         * @param retainAsPublished Whether messages sent due to this subscription keep the retain flag preserved
         * on the message
         * @param retainHandlingType Whether retained messages on matching topics be sent in reaction to this subscription
         */
        Subscription(String topicFilter, QOS qos, Boolean noLocal, Boolean retainAsPublished, RetainHandlingType retainHandlingType) {
            this.topicFilter = topicFilter;
            this.qos = qos;
            this.noLocal = noLocal;
            this.retainAsPublished = retainAsPublished;
            this.retainHandlingType = retainHandlingType;
        }

        /**
         * Creates a new subscription within a subscribe operation
         *
         * @param topicFilter The topic filter to subscribe to
         * @param qos The maximum QoS on which the subscriber will accept publish messages
         */
        Subscription(String topicFilter, QOS qos) {
            this.topicFilter = topicFilter;
            this.qos = qos;
            // Defaults:
            this.noLocal = false;
            this.retainAsPublished = false;
            this.retainHandlingType = RetainHandlingType.SEND_ON_SUBSCRIBE;
        }

        /**
         * Returns the topic filter to subscribe to
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901169">MQTT5 Subscription Options</a>
         *
         * @return The topic filter to subscribe to
         */
        public String getTopicFilter() {
            return this.topicFilter;
        }

        /**
         * Returns the maximum QoS on which the subscriber will accept publish messages.  Negotiated QoS may be different.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901169">MQTT5 Subscription Options</a>
         *
         * @return The maximum QoS on which the subscriber will accept publish messages
         */
        public QOS getQOS() {
            return this.qos;
        }

        /**
         * Returns whether the server should not send publishes to a client when that client was the one who sent the publish.  If
         * null, this is assumed to be false.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901169">MQTT5 Subscription Options</a>
         *
         * @return Whether the server will not send publishes to a client when that client was the one who sent the publish
         */
        public Boolean getNoLocal() {
            return this.noLocal;
        }

        /**
         * Returns whether messages sent due to this subscription keep the retain flag preserved on the message.  If null,
         * this is assumed to be false.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901169">MQTT5 Subscription Options</a>
         *
         * @return Whether messages sent due to this subscription keep the retain flag preserved on the message
         */
        public Boolean getRetainAsPublished() {
            return this.retainAsPublished;
        }

        /**
         * Returns whether retained messages on matching topics be sent in reaction to this subscription.  If null,
         * this is assumed to be RetainHandlingType.SendOnSubscribe.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901169">MQTT5 Subscription Options</a>
         *
         * @return Whether retained messages on matching topics be sent in reaction to this subscription
         */
        public RetainHandlingType getRetainHandlingType() {
            return this.retainHandlingType;
        }
    }

    /**
     * A class to that allows for the creation of a SubscribePacket. Set all of the settings you want in the
     * packet and then use the build() function to get a SubscribePacket populated with the settings
     * defined in the builder.
     */
    static final public class SubscribePacketBuilder {
        private List<Subscription> subscriptions = new ArrayList<Subscription>();
        Long subscriptionIdentifier;
        private List<UserProperty> userProperties;

        /**
         * Sets a single subscription within the SubscribePacket.
         *
         * @param subscription The subscription to add within the SubscribePacket.
         * @return The SubscribePacketBuilder after setting the subscription.
         */
        public SubscribePacketBuilder withSubscription(Subscription subscription) {
            this.subscriptions.add(subscription);
            return this;
        }

        /**
         * Sets a single subscription within the SubscribePacket.
         *
         * @param topicFilter The topic filter to subscribe to
         * @param qos The maximum QoS on which the subscriber will accept publish messages
         * @param noLocal Whether the server will not send publishes to a client when that client was the one who
         * sent the publish
         * @param retainAsPublished Whether messages sent due to this subscription keep the retain flag preserved
         * on the message
         * @param retainHandlingType Whether retained messages on matching topics be sent in reaction to this subscription
         * @return The SubscribePacketBuilder after setting the subscription.
         */
        public SubscribePacketBuilder withSubscription(String topicFilter, QOS qos, Boolean noLocal, Boolean retainAsPublished, RetainHandlingType retainHandlingType) {
            return this.withSubscription(new Subscription(topicFilter, qos, noLocal, retainAsPublished, retainHandlingType));
        }

        /**
         * Sets a single subscription within the SubscribePacket.
         *
         * @param topicFilter The topic filter to subscribe to
         * @param qos The maximum QoS on which the subscriber will accept publish messages
         * @return The SubscribePacketBuilder after setting the subscription.
         */
        public SubscribePacketBuilder withSubscription(String topicFilter, QOS qos) {
            return this.withSubscription(new Subscription(topicFilter, qos));
        }

        /**
         * Sets the positive long to associate with all topic filters in this request.  Publish packets that match
         * a subscription in this request should include this identifier in the resulting message.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901166">MQTT5 Subscription Identifier</a>
         *
         * @param subscriptionIdentifier A positive long to associate with all topic filters in this request.
         * @return The SubscribePacketBuilder after setting the subscription identifier.
         */
        public SubscribePacketBuilder withSubscriptionIdentifier(long subscriptionIdentifier) {
            this.subscriptionIdentifier = subscriptionIdentifier;
            return this;
        }

        /**
         * Sets the list of MQTT5 user properties included with the packet.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901167">MQTT5 User Property</a>
         *
         * @param userProperties List of MQTT5 user properties to be included with the packet.
         * @return The SubscribePacketBuilder after setting the user properties.
         */
        public SubscribePacketBuilder withUserProperties(List<UserProperty> userProperties) {
            this.userProperties = userProperties;
            return this;
        }

        /**
         * Creates a new SubscribePacketBuilder so a SubscribePacket can be created.
         */
        public SubscribePacketBuilder() {}

        /**
         * Creates a new SUBSCRIBE packet using the settings set in the builder.
         * @return The SubscribePacket created from the builder
         */
        public SubscribePacket build() {
            return new SubscribePacket(this);
        }
    }

}
