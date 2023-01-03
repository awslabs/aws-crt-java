/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5.packets;

import software.amazon.awssdk.crt.mqtt5.QOS;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data model of an <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901100">MQTT5 PUBLISH</a> packet
 */
public class PublishPacket {

    private byte[] payload;
    private QOS packetQOS;
    private Boolean retain;
    private String topic;
    private PayloadFormatIndicator payloadFormat;
    private Long messageExpiryIntervalSeconds;
    private String responseTopic;
    private byte[] correlationData;
    private List<Long> subscriptionIdentifiers;
    private String contentType;
    private List<UserProperty> userProperties;

    /**
     * Returns the payload of the publish message.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901119">MQTT5 Publish Payload</a>
     *
     * @return The payload of the publish message.
     */
    public byte[] getPayload() {
        return this.payload;
    }

    /**
     * Sent publishes - Returns the MQTT quality of service level this message should be delivered with.
     *
     * Received publishes - Returns the MQTT quality of service level this message was delivered at.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901103">MQTT5 QoS</a>
     *
     * @return The MQTT quality of service associated with this PUBLISH packet.
     */
    public QOS getQOS() {
        return this.packetQOS;
    }

    /**
     * Returns true if this is a retained message, false otherwise.
     *
     * Always set on received publishes; on sent publishes, null implies false.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901104">MQTT5 Retain</a>
     *
     * @return True if this is a retained message, false otherwise.
     */
    public Boolean getRetain() {
        return this.retain;
    }

    /**
     * Sent publishes - Returns the topic this message should be published to.
     *
     * Received publishes - Returns the topic this message was published to.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901107">MQTT5 Topic Name</a>
     * @return The topic associated with this PUBLISH packet.
     */
    public String getTopic() {
        return this.topic;
    }

    /**
     * Returns the property specifying the format of the payload data. The Mqtt5Client does not enforce or use this
     * value in a meaningful way.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901111">MQTT5 Payload Format Indicator</a>
     *
     * @return Property specifying the format of the payload data.
     */
    public PayloadFormatIndicator getPayloadFormat() {
        return this.payloadFormat;
    }

    /**
     * Sent publishes - Returns the maximum amount of time allowed to elapse for message delivery before the server
     * should instead delete the message (relative to a recipient).
     *
     * Received publishes - Returns the remaining amount of time (from the server's perspective) before the message would
     * have been deleted relative to the subscribing client.
     *
     * If left null, indicates no expiration timeout.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901112">MQTT5 Message Expiry Interval</a>
     *
     * @return The message expiry interval associated with this PublishPacket.
     */
    public Long getMessageExpiryIntervalSeconds() {
        return this.messageExpiryIntervalSeconds;
    }

    /**
     * Returns a opaque topic string intended to assist with request/response implementations.  Not internally meaningful to
     * MQTT5 or this client.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901114">MQTT5 Response Topic</a>
     *
     * @return Opaque topic string intended to assist with request/response implementations.
     */
    public String getResponseTopic() {
        return this.responseTopic;
    }

    /**
     * Returns a opaque binary data used to correlate between publish messages, as a potential method for request-response
     * implementation.  Not internally meaningful to MQTT5.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901115">MQTT5 Correlation Data</a>
     *
     * @return Opaque binary data used to correlate between publish messages.
     */
    public byte[] getCorrelationData() {
        return this.correlationData;
    }

    /**
     * Returns a property specifying the content type of the payload.  Not internally meaningful to MQTT5.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901118">MQTT5 Content Type</a>
     *
     * @return Property specifying the content type of the payload.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Returns a list of MQTT5 user properties included with the packet.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901116">MQTT5 User Property</a>
     *
     * @return List of MQTT5 user properties included with the packet.
     */
    public List<UserProperty> getUserProperties() {
        return this.userProperties;
    }

    /**
     * Sent publishes - Ignored
     *
     * Received publishes - Returns the subscription identifiers of all the subscriptions this message matched.
     *
     * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901117">MQTT5 Subscription Identifier</a>
     *
     * @return the subscription identifiers of all the subscriptions this message matched.
     */
    public List<Long> getSubscriptionIdentifiers() {
        return this.subscriptionIdentifiers;
    }

    /**
     * Creates a Mqtt5Client options instance
     * @throws CrtRuntimeException If the system is unable to allocate space for a native MQTT client structure
     */
    private PublishPacket(PublishPacketBuilder builder) {
        this.payload = builder.payload;
        this.packetQOS = builder.packetQOS;
        this.retain = builder.retain;
        this.topic = builder.topic;
        this.payloadFormat = builder.payloadFormat;
        this.messageExpiryIntervalSeconds = builder.messageExpiryIntervalSeconds;
        this.responseTopic = builder.responseTopic;
        this.correlationData = builder.correlationData;
        this.contentType = builder.contentType;
        this.userProperties = builder.userProperties;
    }

    private PublishPacket() {}

    /**
     * A native, JNI-only helper function for more easily setting the QOS
     * @param QOSValue A int representing the QoS
     */
    private void nativeSetQOS(int QOSValue) {
        this.packetQOS = QOS.getEnumValueFromInteger(QOSValue);
    }

    /**
     * A native, JNI-only helper function for more easily setting the payload format indicator
     * @param payloadFormatIndicator A int representing the payload format
     */
    private void nativeSetPayloadFormatIndicator(int payloadFormatIndicator) {
        this.payloadFormat = PayloadFormatIndicator.getEnumValueFromInteger(payloadFormatIndicator);
    }

    /*******************************************************************************
     * builder
     ******************************************************************************/

    /**
     * Optional property describing a PublishPacket payload's format.
     *
     * Enum values match <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901111">MQTT5 spec</a> encoding values.
     */
    public enum PayloadFormatIndicator {

        /**
         * The payload is arbitrary binary data
         */
        BYTES(0),

        /**
         * The payload is a well-formed utf-8 string value.
         */
        UTF8(1);

        private int indicator;

        private PayloadFormatIndicator(int value) {
            indicator = value;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return indicator;
        }

        /**
         * Creates a Java PayloadFormatIndicator enum value from a native integer value.
         *
         * @param value native integer value for PayloadFormatIndicator
         * @return a new PayloadFormatIndicator value
         */
        public static PayloadFormatIndicator getEnumValueFromInteger(int value) {
            PayloadFormatIndicator enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal PayloadFormatIndicator");
        }

        private static Map<Integer, PayloadFormatIndicator> buildEnumMapping() {
            return Stream.of(PayloadFormatIndicator.values())
                .collect(Collectors.toMap(PayloadFormatIndicator::getValue, Function.identity()));
        }

        private static Map<Integer, PayloadFormatIndicator> enumMapping = buildEnumMapping();
    }

    /**
     * A class to that allows for the creation of a PublishPacket. Set all of the settings you want in the
     * packet and then use the build() function to get a PublishPacket populated with the settings
     * defined in the builder.
     */
    static final public class PublishPacketBuilder {

        private byte[] payload;
        private QOS packetQOS;
        private Boolean retain;
        private String topic;
        private PayloadFormatIndicator payloadFormat;
        private Long messageExpiryIntervalSeconds;
        private String responseTopic;
        private byte[] correlationData;
        private String contentType;
        private List<UserProperty> userProperties;

        /**
         * Sets the payload for the publish message.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901119">MQTT5 Publish Payload</a>
         *
         * @param payload The payload for the publish message.
         * @return The PublishPacketBuilder after setting the payload.
         */
        public PublishPacketBuilder withPayload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        /**
         * Sets the MQTT quality of service level the message should be delivered with.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901103">MQTT5 QoS</a>
         *
         * @param packetQOS The MQTT quality of service level the message should be delivered with.
         * @return The PublishPacketBuilder after setting the QOS.
         */
        public PublishPacketBuilder withQOS(QOS packetQOS) {
            this.packetQOS = packetQOS;
            return this;
        }

        /**
         * Sets if this should be a retained message.
         * Null implies false.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901104">MQTT5 Retain</a>
         *
         * @param retain if this is a retained message.
         * @return The PublishPacketBuilder after setting the retain setting.
         */
        public PublishPacketBuilder withRetain(Boolean retain) {
            this.retain = retain;
            return this;
        }

        /**
         * Sets the topic this message should be published to.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901107">MQTT5 Topic Name</a>
         *
         * @param topic The topic this message should be published to.
         * @return The PublishPacketBuilder after setting the topic.
         */
        public PublishPacketBuilder withTopic(String topic) {
            this.topic = topic;
            return this;
        }

        /**
         * Sets the property specifying the format of the payload data. The Mqtt5Client does not enforce or use this
         * value in a meaningful way.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901111">MQTT5 Payload Format Indicator</a>
         *
         * @param payloadFormat Property specifying the format of the payload data
         * @return The PublishPacketBuilder after setting the payload format.
         */
        public PublishPacketBuilder withPayloadFormat(PayloadFormatIndicator payloadFormat) {
            this.payloadFormat = payloadFormat;
            return this;
        }

        /**
         * Sets the maximum amount of time allowed to elapse for message delivery before the server
         * should instead delete the message (relative to a recipient).
         *
         * If left null, indicates no expiration timeout.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901112">MQTT5 Message Expiry Interval</a>
         *
         * @param messageExpiryIntervalSeconds The maximum amount of time allowed to elapse for message delivery before the server
         * should instead delete the message (relative to a recipient).
         * @return The PublishPacketBuilder after setting the message expiry interval.
         */
        public PublishPacketBuilder withMessageExpiryIntervalSeconds(Long messageExpiryIntervalSeconds) {
            this.messageExpiryIntervalSeconds = messageExpiryIntervalSeconds;
            return this;
        }

        /**
         * Sets the opaque topic string intended to assist with request/response implementations.  Not internally meaningful to
         * MQTT5 or this client.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901114">MQTT5 Response Topic</a>
         * @param responseTopic Topic string intended to assist with request/response implementations
         * @return The PublishPacketBuilder after setting the response topic.
         */
        public PublishPacketBuilder withResponseTopic(String responseTopic) {
            this.responseTopic = responseTopic;
            return this;
        }

        /**
         * Sets the opaque binary data used to correlate between publish messages, as a potential method for request-response
         * implementation.  Not internally meaningful to MQTT5.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901115">MQTT5 Correlation Data</a>
         *
         * @param correlationData Opaque binary data used to correlate between publish messages
         * @return The PublishPacketBuilder after setting the correlation data.
         */
        public PublishPacketBuilder withCorrelationData(byte[] correlationData) {
            this.correlationData = correlationData;
            return this;
        }

        /**
         * Sets the property specifying the content type of the payload.  Not internally meaningful to MQTT5.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901118">MQTT5 Content Type</a>
         *
         * @param contentType Property specifying the content type of the payload
         * @return The PublishPacketBuilder after setting the content type.
         */
        public PublishPacketBuilder withContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        /**
         * Sets the list of MQTT5 user properties included with the packet.
         *
         * See <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901116">MQTT5 User Property</a>
         *
         * @param userProperties List of MQTT5 user properties included with the packet.
         * @return The PublishPacketBuilder after setting the user properties.
         */
        public PublishPacketBuilder withUserProperties(List<UserProperty> userProperties) {
            this.userProperties = userProperties;
            return this;
        }

        /**
         * Creates a new PublishPacketBuilder so a PublishPacket can be created.
         */
        public PublishPacketBuilder() {}

        /**
         * Creates a new PublishPacket using the settings set in the builder.
         *
         * @return The PublishPacket created from the builder
         */
        public PublishPacket build() {
            return new PublishPacket(this);
        }
    }
}
