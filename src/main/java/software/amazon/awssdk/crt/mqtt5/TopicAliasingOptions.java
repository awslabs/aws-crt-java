package software.amazon.awssdk.crt.mqtt5;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Configuration for all client topic aliasing behavior.
 */
public class TopicAliasingOptions {

    private OutboundTopicAliasBehaviorType outboundBehavior;
    private Integer outboundCacheMaxSize;
    private InboundTopicAliasBehaviorType inboundBehavior;
    private Integer inboundCacheMaxSize;

    /**
     * Default constructor
     */
    public TopicAliasingOptions() {
        this.outboundBehavior = OutboundTopicAliasBehaviorType.Default;
        this.outboundCacheMaxSize = 0;
        this.inboundBehavior = InboundTopicAliasBehaviorType.Default;
        this.inboundCacheMaxSize = 0;
    }

    /**
     * Controls what kind of outbound topic aliasing behavior the client should attempt to use.
     *
     * If topic aliasing is not supported by the server, this setting has no effect and any attempts to directly
     * manipulate the topic alias id in outbound publishes will be ignored.
     *
     * By default, outbound topic aliasing is disabled.
     *
     * @param behavior outbound topic alias behavior to use
     *
     * @return the topic aliasing options object
     */
    public TopicAliasingOptions withOutboundBehavior(OutboundTopicAliasBehaviorType behavior) {
        this.outboundBehavior = behavior;
        return this;
    }

    /**
     * If outbound topic aliasing is set to LRU, this controls the maximum size of the cache.  If outbound topic
     * aliasing is set to LRU and this is zero or undefined, a sensible default is used (25).  If outbound topic
     * aliasing is not set to LRU, then this setting has no effect.
     *
     * The final size of the cache is determined by the minimum of this setting and the value of the
     * topic_alias_maximum property of the received CONNACK.  If the received CONNACK does not have an explicit
     * positive value for that field, outbound topic aliasing is disabled for the duration of that connection.
     *
     * @param size maximum size to use for the outbound alias cache
     *
     * @return the topic aliasing options object
     */
    public TopicAliasingOptions withOutboundCacheMaxSize(int size) {
        this.outboundCacheMaxSize = size;
        return this;
    }

    /**
     * Controls whether or not the client allows the broker to use topic aliasing when sending publishes.  Even if
     * inbound topic aliasing is enabled, it is up to the server to choose whether or not to use it.
     *
     * If left undefined, then inbound topic aliasing is disabled.
     *
     * @param behavior inbound topic alias behavior to use
     *
     * @return the topic aliasing options object
     */
    public TopicAliasingOptions withInboundBehavior(InboundTopicAliasBehaviorType behavior) {
        this.inboundBehavior = behavior;
        return this;
    }

    /**
     * If inbound topic aliasing is enabled, this will control the size of the inbound alias cache.  If inbound
     * aliases are enabled and this is zero or undefined, then a sensible default will be used (25).  If inbound
     * aliases are disabled, this setting has no effect.
     *
     * Behaviorally, this value overrides anything present in the topic_alias_maximum field of
     * the CONNECT packet options.
     *
     * @param size maximum size to use for the inbound alias cache
     *
     * @return the topic aliasing options object
     */
    public TopicAliasingOptions withInboundCacheMaxSize(int size) {
        this.inboundCacheMaxSize = size;
        return this;
    }

    /**
     * An enumeration that controls how the client applies topic aliasing to outbound publish packets.
     *
     * Topic alias behavior is described in <a href="https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901113">MQTT5 Topic Aliasing</a>
     */
    public enum OutboundTopicAliasBehaviorType {

        /**
         * Maps to Disabled.  This keeps the client from being broken (by default) if the broker
         * topic aliasing implementation has a problem.
         */
        Default(0),

        /**
         * Outbound aliasing is the user's responsibility.  Client will cache and use
         * previously-established aliases if they fall within the negotiated limits of the connection.
         *
         * The user must still always submit a full topic in their publishes because disconnections disrupt
         * topic alias mappings unpredictably.  The client will properly use a requested alias when the most-recently-seen
         * binding for a topic alias value matches the alias and topic in the publish packet.
         */
        Manual(1),

        /**
         * (Recommended) The client will ignore any user-specified topic aliasing and instead use an LRU cache to drive
         * alias usage.
         */
        LRU(2),

        /**
         * Completely disable outbound topic aliasing.
         */
        Disabled(3);

        private int value;

        private OutboundTopicAliasBehaviorType(int value) {
            this.value = value;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Creates a Java OutboundTopicAliasBehaviorType enum value from a native integer value.
         *
         * @param value native integer value for the OutboundTopicAliasBehaviorType value
         * @return a new OutboundTopicAliasBehaviorType value
         */
        public static OutboundTopicAliasBehaviorType getEnumValueFromInteger(int value) {
            OutboundTopicAliasBehaviorType enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal OutboundTopicAliasBehaviorType");
        }

        private static Map<Integer, OutboundTopicAliasBehaviorType> buildEnumMapping() {
            return Stream.of(OutboundTopicAliasBehaviorType.values())
                    .collect(Collectors.toMap(OutboundTopicAliasBehaviorType::getValue, Function.identity()));
        }

        private static Map<Integer, OutboundTopicAliasBehaviorType> enumMapping = buildEnumMapping();
    }

    /**
     * An enumeration that controls whether or not the client allows the broker to send publishes that use topic
     * aliasing.
     *
     * Topic alias behavior is described in https://docs.oasis-open.org/mqtt/mqtt/v5.0/os/mqtt-v5.0-os.html#_Toc3901113
     */
    public enum InboundTopicAliasBehaviorType {

        /**
         * Maps to Disabled.  This keeps the client from being broken (by default) if the broker
         * topic aliasing implementation has a problem.
         */
        Default(0),

        /**
         * Allow the server to send PUBLISH packets to the client that use topic aliasing
         */
        Enabled(1),

        /**
         * Forbid the server from sending PUBLISH packets to the client that use topic aliasing
         */
        Disabled(2);

        private int value;

        private InboundTopicAliasBehaviorType(int value) {
            this.value = value;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return this.value;
        }

        /**
         * Creates a Java InboundTopicAliasBehaviorType enum value from a native integer value.
         *
         * @param value native integer value for the InboundTopicAliasBehaviorType value
         * @return a new InboundTopicAliasBehaviorType value
         */
        public static InboundTopicAliasBehaviorType getEnumValueFromInteger(int value) {
            InboundTopicAliasBehaviorType enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal InboundTopicAliasBehaviorType");
        }

        private static Map<Integer, InboundTopicAliasBehaviorType> buildEnumMapping() {
            return Stream.of(InboundTopicAliasBehaviorType.values())
                    .collect(Collectors.toMap(InboundTopicAliasBehaviorType::getValue, Function.identity()));
        }

        private static Map<Integer, InboundTopicAliasBehaviorType> enumMapping = buildEnumMapping();
    }
}