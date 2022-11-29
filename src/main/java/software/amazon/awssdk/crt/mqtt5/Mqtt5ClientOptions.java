/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.ExponentialBackoffRetryOptions.JitterMode;

import software.amazon.awssdk.crt.mqtt5.packets.ConnAckPacket;
import software.amazon.awssdk.crt.mqtt5.packets.ConnectPacket;
import software.amazon.awssdk.crt.mqtt5.packets.DisconnectPacket;
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Consumer;

/**
 * Configuration for the creation of Mqtt5Clients
 *
 * MQTT5 support is currently in <b>developer preview<b>.  We encourage feedback at all times, but feedback during the
 * preview window is especially valuable in shaping the final product.  During the preview period we may make
 * backwards-incompatible changes to the public API, but in general, this is something we will try our best to avoid.
 */
public class Mqtt5ClientOptions {

    private String hostName;
    private Long port;
    private ClientBootstrap bootstrap;
    private SocketOptions socketOptions;
    private TlsContext tlsContext;
    private HttpProxyOptions httpProxyOptions;
    private ConnectPacket connectOptions;
    private ClientSessionBehavior sessionBehavior = ClientSessionBehavior.DEFAULT;
    private ExtendedValidationAndFlowControlOptions extendedValidationAndFlowControlOptions = ExtendedValidationAndFlowControlOptions.NONE;
    private ClientOfflineQueueBehavior offlineQueueBehavior = ClientOfflineQueueBehavior.DEFAULT;
    private JitterMode retryJitterMode = JitterMode.Default;
    private Long minReconnectDelayMs;
    private Long maxReconnectDelayMs;
    private Long minConnectedTimeToResetReconnectDelayMs;
    private Long pingTimeoutMs;
    private Long connackTimeoutMs;
    private Long ackTimeoutSeconds;
    private LifecycleEvents lifecycleEvents;
    private Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketHandshakeTransform;
    private PublishEvents publishEvents;

    /**
     * Returns the host name of the MQTT server to connect to.
     *
     * @return Host name of the MQTT server to connect to.
     */
    public String getHostName()
    {
        return this.hostName;
    }

    /**
     * Returns the network port of the MQTT server to connect to.
     *
     * @return Network port of the MQTT server to connect to.
     */
    public Long getPort()
    {
        return this.port;
    }

    /**
     * Returns the Client bootstrap used.
     *
     * @return The Client bootstrap used
     */
    public ClientBootstrap getBootstrap()
    {
        return this.bootstrap;
    }

    /**
     * Returns the socket properties of the underlying MQTT connections made by the client.
     *
     * @return the socket properties of the underlying MQTT connections made by the client or null if defaults are used.
     */
    public SocketOptions getSocketOptions()
    {
        return this.socketOptions;
    }

    /**
     * Returns the TLS context for secure socket connections.
     * If null, then a plaintext connection will be used.
     *
     * @return TLS context for secure socket connections.
     */
    public TlsContext getTlsContext()
    {
        return this.tlsContext;
    }

    /**
     * Returns the (tunneling) HTTP proxy usage when establishing MQTT connections
     *
     * @return (tunneling) HTTP proxy usage when establishing MQTT connections
     */
    public HttpProxyOptions getHttpProxyOptions()
    {
        return this.httpProxyOptions;
    }

    /**
     * Returns all configurable options with respect to the CONNECT packet sent by the client, including the will.  These
     * connect properties will be used for every connection attempt made by the client.
     *
     * @return all configurable options with respect to the CONNECT packet sent by the client, including the will
     */
    public ConnectPacket getConnectOptions()
    {
        return this.connectOptions;
    }

    /**
     * Returns how the Mqtt5Client should behave with respect to MQTT sessions.
     *
     * @return How the Mqtt5Client should behave with respect to MQTT sessions.
     */
    public ClientSessionBehavior getSessionBehavior()
    {
        return this.sessionBehavior;
    }

    /**
     * Returns the additional controls for client behavior with respect to operation validation and flow control;
     * these checks go beyond the base MQTT5 spec to respect limits of specific MQTT brokers.
     *
     * @return The additional controls for client behavior with respect to operation validation and flow control
     */
    public ExtendedValidationAndFlowControlOptions getExtendedValidationAndFlowControlOptions()
    {
        return this.extendedValidationAndFlowControlOptions;
    }

    /**
     * Returns how disconnects affect the queued and in-progress operations tracked by the client.  Also controls
     * how new operations are handled while the client is not connected.  In particular, if the client is not connected,
     * then any operation that would be failed on disconnect (according to these rules) will also be rejected.
     *
     * @return How disconnects affect the queued and in-progress operations tracked by the client.
     */
    public ClientOfflineQueueBehavior getOfflineQueueBehavior()
    {
        return this.offlineQueueBehavior;
    }

    /**
     * Returns how the reconnect delay is modified in order to smooth out the distribution of reconnection attempt
     * time points for a large set of reconnecting clients.
     *
     * @return how the reconnect delay is modified in order to smooth out the distribution of reconnection attempt
     * time points for a large set of reconnecting clients.
     */
    public JitterMode getRetryJitterMode()
    {
        return this.retryJitterMode;
    }

    /**
     * Returns the minimum amount of time to wait to reconnect after a disconnect.
     * Exponential back-off is performed with jitter after each connection failure.
     *
     * @return The minimum amount of time to wait to reconnect after a disconnect.
     */
    public Long getMinReconnectDelayMs() {
        return this.minReconnectDelayMs;
    }

    /**
     * Returns the maximum amount of time to wait to reconnect after a disconnect.  Exponential back-off is performed with jitter
     * after each connection failure.
     *
     * @return The maximum amount of time to wait to reconnect after a disconnect
     */
    public Long getMaxReconnectDelayMs() {
        return this.maxReconnectDelayMs;
    }

    /**
     * Returns the amount of time that must elapse with an established connection before the reconnect delay is reset to the minimum.
     * This helps alleviate bandwidth-waste in fast reconnect cycles due to permission failures on operations.
     *
     * @return The amount of time that must elapse with an established connection before the reconnect delay is reset to the minimum
     */
    public Long getMinConnectedTimeToResetReconnectDelayMs() {
        return this.minConnectedTimeToResetReconnectDelayMs;
    }

    /**
     * Returns the time interval to wait after sending a PINGREQ for a PINGRESP to arrive. If one does not arrive, the client will
     * close the current connection.
     *
     * @return time interval to wait after sending a PINGREQ for a PINGRESP to arrive.
     */
    public Long getPingTimeoutMs() {
        return this.pingTimeoutMs;
    }

    /**
     * Returns the time interval to wait after sending a CONNECT request for a CONNACK to arrive.  If one does not arrive, the
     * connection will be shut down.
     *
     * @return Time interval to wait after sending a CONNECT request for a CONNACK to arrive
     */
    public Long getConnackTimeoutMs() {
        return this.connackTimeoutMs;
    }

    /**
     * Returns the time interval to wait for an ack after sending a QoS 1+ PUBLISH, SUBSCRIBE, or UNSUBSCRIBE before
     * failing the operation.
     *
     * @return the time interval to wait for an ack after sending a QoS 1+ PUBLISH, SUBSCRIBE, or UNSUBSCRIBE before
     * failing the operation.
     */
    public Long getAckTimeoutSeconds() {
        return this.ackTimeoutSeconds;
    }

    /**
     * Returns the LifecycleEvents interface that will be called when the client gets a LifecycleEvent.
     *
     * @return The LifecycleEvents interface that will be called when the client gets a LifecycleEvent
     */
    public LifecycleEvents getLifecycleEvents() {
        return this.lifecycleEvents;
    }

    /**
     * Returns the callback that allows a custom transformation of the HTTP request which acts as the websocket handshake.
     * Websockets will be used if this is set to a valid transformation callback.  To use websockets but not perform
     * a transformation, just set this as a trivial completion callback.  If null, the connection will be made
     * with direct MQTT.
     *
     * @return The custom transformation of the HTTP request that acts as the websocket handshake or null.
     */
    public Consumer<Mqtt5WebsocketHandshakeTransformArgs> getWebsocketHandshakeTransform() {
        return this.websocketHandshakeTransform;
    }

    /**
     * Returns the PublishEvents interface that will be called when the client gets a message.
     *
     * @return PublishEvents interface that will be called when the client gets a message.
     */
    public PublishEvents getPublishEvents() {
        return this.publishEvents;
    }

    /**
     * Creates a Mqtt5ClientOptionsBuilder instance
     * @param builder The builder to get the Mqtt5ClientOptions values from
     */
    public Mqtt5ClientOptions(Mqtt5ClientOptionsBuilder builder) {
        this.hostName = builder.hostName;
        this.port = builder.port;
        this.bootstrap = builder.bootstrap;
        this.socketOptions = builder.socketOptions;
        this.tlsContext = builder.tlsContext;
        this.httpProxyOptions = builder.httpProxyOptions;
        this.connectOptions = builder.connectOptions;
        this.sessionBehavior = builder.sessionBehavior;
        this.extendedValidationAndFlowControlOptions = builder.extendedValidationAndFlowControlOptions;
        this.offlineQueueBehavior = builder.offlineQueueBehavior;
        this.retryJitterMode = builder.retryJitterMode;
        this.minReconnectDelayMs = builder.minReconnectDelayMs;
        this.maxReconnectDelayMs = builder.maxReconnectDelayMs;
        this.minConnectedTimeToResetReconnectDelayMs = builder.minConnectedTimeToResetReconnectDelayMs;
        this.pingTimeoutMs = builder.pingTimeoutMs;
        this.connackTimeoutMs = builder.connackTimeoutMs;
        this.ackTimeoutSeconds = builder.ackTimeoutSeconds;
        this.lifecycleEvents = builder.lifecycleEvents;
        this.websocketHandshakeTransform = builder.websocketHandshakeTransform;
        this.publishEvents = builder.publishEvents;
    }

    /*******************************************************************************
     * lifecycle methods
     ******************************************************************************/

    /**
     * An interface that defines all of the functions the Mqtt5Client will call when it receives a lifecycle event.
     */
    public interface LifecycleEvents {
        /**
         * Called when the client begins a connection attempt
         *
         * @param client The client associated with the event
         * @param onAttemptingConnectReturn The data associated with the onAttemptingConnect event.
         */
        public void onAttemptingConnect(Mqtt5Client client, OnAttemptingConnectReturn onAttemptingConnectReturn);

        /**
         * Called when the client successfully establishes an MQTT connection
         *
         * @param client The client associated with the event
         * @param onConnectionSuccessReturn The data associated with the onConnectionSuccess event.
         */
        public void onConnectionSuccess(Mqtt5Client client, OnConnectionSuccessReturn onConnectionSuccessReturn);

        /**
         * Called when the client fails to establish an MQTT connection
         *
         * @param client The client associated with the event
         * @param onConnectionFailureReturn The data associated with the onConnectionFailure event.
         */
        public void onConnectionFailure(Mqtt5Client client, OnConnectionFailureReturn onConnectionFailureReturn);

        /**
         * Called when the client's current MQTT connection is closed
         *
         * @param client The client associated with the event
         * @param onDisconnectionReturn The data associated with the onDisconnection event.
         */
        public void onDisconnection(Mqtt5Client client, OnDisconnectionReturn onDisconnectionReturn);

        /**
         * Called when the client reaches the 'Stopped' state as a result of the user invoking .stop()
         *
         * @param client The client associated with the event
         * @param onStoppedReturn The data associated with the onStopped event.
         */
        public void onStopped(Mqtt5Client client, OnStoppedReturn onStoppedReturn);
    }

    /**
     * An interface that defines all of the publish functions the Mqtt5Client will call when it receives a publish packet.
     */
    public interface PublishEvents {
        /**
         * Called when an MQTT PUBLISH packet is received by the client
         *
         * @param client The client that has received the message
         * @param publishReturn All of the data that was received from the server
         */
        public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn);
    }

    /*******************************************************************************
     * builder
     ******************************************************************************/

    /**
     * Controls how the Mqtt5Client should behave with respect to MQTT sessions.
     */
    public enum ClientSessionBehavior {

        /**
         * Default client session behavior. Maps to CLEAN.
         */
        DEFAULT(0),

        /**
        * Always ask for a clean session when connecting
        */
        CLEAN(1),

        /**
         * Always attempt to rejoin an existing session after an initial connection success.
         *
         * Session rejoin requires an appropriate non-zero session expiry interval in the client's CONNECT options.
         */
        REJOIN_POST_SUCCESS(2);

        private int type;

        private ClientSessionBehavior(int code) {
            type = code;
        }

        /**
         * @return The native enum integer value associated with this enum value
         */
        public int getValue() {
            return type;
        }

        /**
         * Creates a ClientSessionBehavior enum value from a native integer value.
         *
         * @param value native integer value for the Client Session Behavior Type
         * @return a new ClientSessionBehavior value
         */
        public static ClientSessionBehavior getEnumValueFromInteger(int value) {
            ClientSessionBehavior enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal ClientSessionBehavior");
        }

        private static Map<Integer, ClientSessionBehavior> buildEnumMapping() {
            return Stream.of(ClientSessionBehavior.values())
                .collect(Collectors.toMap(ClientSessionBehavior::getValue, Function.identity()));
        }

        private static Map<Integer, ClientSessionBehavior> enumMapping = buildEnumMapping();
    }

    /**
     * Additional controls for client behavior with respect to operation validation and flow control; these checks
     * go beyond the MQTT5 spec to respect limits of specific MQTT brokers.
     */
    public enum ExtendedValidationAndFlowControlOptions {

        /**
         * Do not do any additional validation or flow control
         */
        NONE(0),

        /**
         * Apply additional client-side validation and operational flow control that respects the
         * default AWS IoT Core limits.
         *
         * Currently applies the following additional validation:
         *
         * <ol>
         * <li> No more than 8 subscriptions per SUBSCRIBE packet </li>
         * <li> Topics and topic filters have a maximum of 7 slashes (8 segments), not counting any AWS rules prefix </li>
         * <li> Topics must be 256 bytes or less in length </li>
         * <li> Client id must be 128 or less bytes in length </li>
         * </ol>
         *
         * Also applies the following flow control:
         *
         * <ol>
         * <li> Outbound throughput throttled to 512KB/s </li>
         * <li> Outbound publish TPS throttled to 100 </li>
         * </ol>
         */
        AWS_IOT_CORE_DEFAULTS(1);

        private int type;

        private ExtendedValidationAndFlowControlOptions(int code) {
            type = code;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return type;
        }

        /**
         * Creates a Java ExtendedValidationAndFlowControlOptions enum value from a native integer value.
         *
         * @param value native integer value for the extended validation and flow control options
         * @return a new ExtendedValidationAndFlowControlOptions value
         */
        public static ExtendedValidationAndFlowControlOptions getEnumValueFromInteger(int value) {
            ExtendedValidationAndFlowControlOptions enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal ExtendedValidationAndFlowControlOptions");
        }

        private static Map<Integer, ExtendedValidationAndFlowControlOptions> buildEnumMapping() {
            return Stream.of(ExtendedValidationAndFlowControlOptions.values())
                .collect(Collectors.toMap(ExtendedValidationAndFlowControlOptions::getValue, Function.identity()));
        }

        private static Map<Integer, ExtendedValidationAndFlowControlOptions> enumMapping = buildEnumMapping();
    }

    /**
     * Controls how disconnects affect the queued and in-progress operations tracked by the client.  Also controls
     * how operations are handled while the client is not connected.  In particular, if the client is not connected,
     * then any operation that would be failed on disconnect (according to these rules) will be rejected.
     */
    public enum ClientOfflineQueueBehavior {

        /**
         * Default client operation queue behavior. Maps to FAIL_QOS0_PUBLISH_ON_DISCONNECT.
         */
        DEFAULT(0),

        /*
         * Re-queues QoS 1+ publishes on disconnect; un-acked publishes go to the front while unprocessed publishes stay
         * in place.  All other operations (QoS 0 publishes, subscribe, unsubscribe) are failed.
         */
        FAIL_NON_QOS1_PUBLISH_ON_DISCONNECT(1),

        /*
         * QoS 0 publishes that are not complete at the time of disconnection are failed.  Un-acked QoS 1+ publishes are
         * re-queued at the head of the line for immediate retransmission on a session resumption.  All other operations
         * are requeued in original order behind any retransmissions.
         */
        FAIL_QOS0_PUBLISH_ON_DISCONNECT(2),

        /*
         * All operations that are not complete at the time of disconnection are failed, except operations that
         * the MQTT5 spec requires to be retransmitted (un-acked QoS1+ publishes).
         */
        FAIL_ALL_ON_DISCONNECT(3);

        private int type;

        private ClientOfflineQueueBehavior(int code) {
            type = code;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return type;
        }

        /**
         * Creates a Java ClientOfflineQueueBehavior enum value from a native integer value.
         *
         * @param value native integer value for the client operation queue behavior type
         * @return a new ClientOfflineQueueBehavior value
         */
        public static ClientOfflineQueueBehavior getEnumValueFromInteger(int value) {
            ClientOfflineQueueBehavior enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal ClientOfflineQueueBehavior");
        }

        private static Map<Integer, ClientOfflineQueueBehavior> buildEnumMapping() {
            return Stream.of(ClientOfflineQueueBehavior.values())
                .collect(Collectors.toMap(ClientOfflineQueueBehavior::getValue, Function.identity()));
        }

        private static Map<Integer, ClientOfflineQueueBehavior> enumMapping = buildEnumMapping();
    }

    /**
     * All of the options for a Mqtt5Client. This includes the settings to make a connection, as well as the
     * event callbacks, publish callbacks, and more.
     *
     * !! Developer Preview !! - This class is currently in developer preview.
     * The interface is not guaranteed to be stable yet.
     * Please report any issues or make suggestions in https://github.com/awslabs/aws-crt-java/issues
     */
    static final public class Mqtt5ClientOptionsBuilder {

        private String hostName;
        private Long port;
        private ClientBootstrap bootstrap;
        private SocketOptions socketOptions;
        private TlsContext tlsContext;
        private HttpProxyOptions httpProxyOptions;
        private ConnectPacket connectOptions;
        private ClientSessionBehavior sessionBehavior = ClientSessionBehavior.DEFAULT;
        private ExtendedValidationAndFlowControlOptions extendedValidationAndFlowControlOptions = ExtendedValidationAndFlowControlOptions.NONE;
        private ClientOfflineQueueBehavior offlineQueueBehavior = ClientOfflineQueueBehavior.DEFAULT;
        private JitterMode retryJitterMode = JitterMode.Default;
        private Long minReconnectDelayMs;
        private Long maxReconnectDelayMs;
        private Long minConnectedTimeToResetReconnectDelayMs;
        private Long pingTimeoutMs;
        private Long connackTimeoutMs;
        private Long ackTimeoutSeconds;
        private LifecycleEvents lifecycleEvents;
        private Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketHandshakeTransform;
        private PublishEvents publishEvents;

        /**
         * Sets the host name of the MQTT server to connect to.
         *
         * @param hostName Host name of the MQTT server to connect to.
         * @return The Mqtt5ClientOptionsBuilder after setting the host name
         */
        public Mqtt5ClientOptionsBuilder withHostName(String hostName)
        {
            this.hostName = hostName;
            return this;
        }

        /**
         * Sets the network port of the MQTT server to connect to.
         *
         * @param port Network port of the MQTT server to connect to.
         * @return The Mqtt5ClientOptionsBuilder after setting the port
         */
        public Mqtt5ClientOptionsBuilder withPort(Long port)
        {
            this.port = port;
            return this;
        }

        /**
         * Sets the ClientBootstrap to use. In almost all cases, this should be left null.
         *
         * @param bootstrap The ClientBootstrap to use
         * @return The Mqtt5ClientOptionsBuilder after setting the ClientBootstrap
         */
        public Mqtt5ClientOptionsBuilder withBootstrap(ClientBootstrap bootstrap)
        {
            this.bootstrap = bootstrap;
            return this;
        }

        /**
         * Sets the socket properties of the underlying MQTT connections made by the client.  Leave null to use
         * defaults (no TCP keep alive, 10 second socket timeout).
         *
         * @param socketOptions The socket properties of the underlying MQTT connections made by the client.
         * @return The Mqtt5ClientOptionsBuilder after setting the socket options
         */
        public Mqtt5ClientOptionsBuilder withSocketOptions(SocketOptions socketOptions)
        {
            this.socketOptions = socketOptions;
            return this;
        }

        /**
         * Sets the TLS context for secure socket connections.
         * If null, then a plaintext connection will be used.
         *
         * @param tlsContext The TLS context for secure socket connections.
         * @return The Mqtt5ClientOptionsBuilder after setting the TlsContext
         */
        public Mqtt5ClientOptionsBuilder withTlsContext(TlsContext tlsContext)
        {
            this.tlsContext = tlsContext;
            return this;
        }

        /**
         * Sets the (tunneling) HTTP proxy usage when establishing MQTT connection.
         *
         * @param httpProxyOptions the (tunneling) HTTP proxy usage when establishing MQTT connection.
         * @return The Mqtt5ClientOptionsBuilder after setting the HttpProxyOptions
         */
        public Mqtt5ClientOptionsBuilder withHttpProxyOptions(HttpProxyOptions httpProxyOptions)
        {
            this.httpProxyOptions = httpProxyOptions;
            return this;
        }

        /**
         * Sets all configurable options with respect to the CONNECT packet sent by the client, including the Will.  These
         * connect properties will be used for every connection attempt made by the client.
         *
         * @param connectOptions Configurable options with respect to the CONNECT packet sent by the client, including the Will.
         * @return The Mqtt5ClientOptionsBuilder after setting the connect options
         */
        public Mqtt5ClientOptionsBuilder withConnectOptions(ConnectPacket connectOptions)
        {
            this.connectOptions = connectOptions;
            return this;
        }

        /**
         * Sets how the Mqtt5Client should behave with respect to MQTT sessions.
         *
         * @param sessionBehavior How the Mqtt5Client should behave with respect to MQTT sessions.
         * @return The Mqtt5ClientOptionsBuilder after setting the ClientSessionBehavior
         */
        public Mqtt5ClientOptionsBuilder withSessionBehavior(ClientSessionBehavior sessionBehavior)
        {
            this.sessionBehavior = sessionBehavior;
            return this;
        }

        /**
         * Sets the additional controls for client behavior with respect to operation validation and flow control; these checks
         * go beyond the base MQTT5 spec to respect limits of specific MQTT brokers.
         *
         * @param extendedValidationAndFlowControlOptions Additional controls for client behavior with respect to operation validation and flow control
         * @return The Mqtt5ClientOptionsBuilder after setting the ExtendedValidationAndFlowControlOptions
         */
        public Mqtt5ClientOptionsBuilder withExtendedValidationAndFlowControlOptions(ExtendedValidationAndFlowControlOptions extendedValidationAndFlowControlOptions)
        {
            this.extendedValidationAndFlowControlOptions = extendedValidationAndFlowControlOptions;
            return this;
        }

        /**
         * Sets how disconnects affect the queued and in-progress operations tracked by the client.  Also controls
         * how new operations are handled while the client is not connected.  In particular, if the client is not connected,
         * then any operation that would be failed on disconnect (according to these rules) will also be rejected.
         *
         * @param offlineQueueBehavior How disconnects affect the queued and in-progress operations tracked by the client
         * @return The Mqtt5ClientOptionsBuilder after setting the ClientOfflineQueueBehavior
         */
        public Mqtt5ClientOptionsBuilder withOfflineQueueBehavior(ClientOfflineQueueBehavior offlineQueueBehavior)
        {
            this.offlineQueueBehavior = offlineQueueBehavior;
            return this;
        }

        /**
         * Sets how the reconnect delay is modified in order to smooth out the distribution of reconnection attempt
         * time points for a large set of reconnecting clients.
         *
         * @param retryJitterMode How the reconnect delay is modified in order to smooth out the distribution of reconnection attempt
         * time points for a large set of reconnecting clients.
         * @return The Mqtt5ClientOptionsBuilder after setting the JitterMode
         */
        public Mqtt5ClientOptionsBuilder withRetryJitterMode(JitterMode retryJitterMode)
        {
            this.retryJitterMode = retryJitterMode;
            return this;
        }

        /**
         * Sets the minimum amount of time to wait to reconnect after a disconnect.  Exponential back-off is performed with jitter
         * after each connection failure.
         *
         * @param minReconnectDelayMs The minimum amount of time to wait to reconnect after a disconnect.
         * @return The Mqtt5ClientOptionsBuilder after setting the minimum reconnect delay
         */
        public Mqtt5ClientOptionsBuilder withMinReconnectDelayMs(Long minReconnectDelayMs)
        {
            this.minReconnectDelayMs = minReconnectDelayMs;
            return this;
        }

        /**
         * Sets the maximum amount of time to wait to reconnect after a disconnect.  Exponential back-off is performed with jitter
         * after each connection failure.
         *
         * @param maxReconnectDelayMs The maximum amount of time to wait to reconnect after a disconnect
         * @return The Mqtt5ClientOptionsBuilder after setting the maximum reconnect delay
         */
        public Mqtt5ClientOptionsBuilder withMaxReconnectDelayMs(Long maxReconnectDelayMs)
        {
            this.maxReconnectDelayMs = maxReconnectDelayMs;
            return this;
        }

        /**
         * Sets the minimum time needed to pass to reset the reconnect delay in milliseconds used when the Mqtt5Client connects.
         *
         * @param minConnectedTimeToResetReconnectDelayMs The minimum time needed to pass to reset the reconnect delay
         * @return The Mqtt5ClientOptionsBuilder after setting the minimum time needed to pass to reset the reconnect delay
         */
        public Mqtt5ClientOptionsBuilder withMinConnectedTimeToResetReconnectDelayMs(Long minConnectedTimeToResetReconnectDelayMs)
        {
            this.minConnectedTimeToResetReconnectDelayMs = minConnectedTimeToResetReconnectDelayMs;
            return this;
        }

        /**
         * Sets the time interval to wait after sending a PINGREQ for a PINGRESP to arrive.  If one does not arrive, the client will
         * close the current connection.
         *
         * @param pingTimeoutMs The time interval to wait after sending a PINGREQ for a PINGRESP to arrive.
         * @return The Mqtt5ClientOptionsBuilder after setting the ping timeout time
         */
        public Mqtt5ClientOptionsBuilder withPingTimeoutMs(Long pingTimeoutMs)
        {
            this.pingTimeoutMs = pingTimeoutMs;
            return this;
        }

        /**
         * Sets the time interval to wait after sending a CONNECT request for a CONNACK to arrive.  If one does not arrive, the
         * connection will be shut down.
         *
         * @param connackTimeoutMs The time interval to wait after sending a CONNECT request for a CONNACK to arrive.
         * @return The Mqtt5ClientOptionsBuilder after setting the timeout in milliseconds for getting a ConnAckPacket from the server
         */
        public Mqtt5ClientOptionsBuilder withConnackTimeoutMs(Long connackTimeoutMs)
        {
            this.connackTimeoutMs = connackTimeoutMs;
            return this;
        }

        /**
         * Sets the time interval to wait for an ack after sending a QoS 1+ PUBLISH, SUBSCRIBE, or UNSUBSCRIBE before
         * failing the operation.
         *
         * @param ackTimeoutSeconds The time interval to wait for an ack after sending a QoS 1+ PUBLISH, SUBSCRIBE, or UNSUBSCRIBE before
         * failing the operation.
         * @return The Mqtt5ClientOptionsBuilder after setting the timeout in milliseconds for getting an ACK packet
         * from the server when performing an operation
         */
        public Mqtt5ClientOptionsBuilder withAckTimeoutSeconds(Long ackTimeoutSeconds)
        {
            this.ackTimeoutSeconds = ackTimeoutSeconds;
            return this;
        }

        /**
         * Sets the Lifecycle Events interface that will be called when the client gets a LifecycleEvent.
         *
         * @param lifecycleEvents The LifecycleEvents interface that will be called
         * @return The Mqtt5ClientOptionsBuilder after setting the Lifecycle Events interface
         */
        public Mqtt5ClientOptionsBuilder withLifecycleEvents(LifecycleEvents lifecycleEvents) {
            this.lifecycleEvents = lifecycleEvents;
            return this;
        }

        /**
         * Sets the callback that allows a custom transformation of the HTTP request that acts as the websocket handshake.
         * Websockets will be used if this is set to a valid transformation callback.  To use websockets but not perform
         * a transformation, just set this as a trivial completion callback.  If null, the connection will be made
         * with direct MQTT.
         *
         * @param handshakeTransform Callback that allows a custom transformation of the HTTP request that acts as the websocket handshake.
         * @return The Mqtt5ClientOptionsBuilder after setting the websocket handshake transform callback
         */
        public Mqtt5ClientOptionsBuilder withWebsocketHandshakeTransform(Consumer<Mqtt5WebsocketHandshakeTransformArgs> handshakeTransform) {
            this.websocketHandshakeTransform = handshakeTransform;
            return this;
        }

        /**
         * Sets the PublishEvents interface that will be called when the client gets a message.
         *
         * @param publishEvents The PublishEvents interface that will be called when the client gets a message.
         * @return The Mqtt5ClientOptionsBuilder after setting the PublishEvents interface
         */
        public Mqtt5ClientOptionsBuilder withPublishEvents(PublishEvents publishEvents) {
            this.publishEvents = publishEvents;
            return this;
        }

        /**
         * Creates a new Mqtt5ClientOptionsBuilder instance
         *
         * @param hostName The host name of the MQTT server to connect to.
         * @param port The port of the MQTT server to connect to.
         */
        public Mqtt5ClientOptionsBuilder(String hostName, Long port) {
            this.hostName = hostName;
            this.port = port;
        }

        /**
         * Returns a Mqtt5ClientOptions class configured with all of the options set in the Mqtt5ClientOptionsBuilder.
         * This can then be used to make a new Mqtt5Client.
         *
         * @return A configured Mqtt5ClientOptions
         */
        public Mqtt5ClientOptions build()
        {
            return new Mqtt5ClientOptions(this);
        }
    }

}
