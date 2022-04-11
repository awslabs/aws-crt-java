
/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt;

import java.util.function.Consumer;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.ClientTlsContext;
import software.amazon.awssdk.crt.io.SocketOptions;

/**
 * Encapsulates all per-mqtt-connection configuration
 */
public final class MqttConnectionConfig extends CrtResource {
    /* connection */
    private String endpoint;
    private int port;
    private SocketOptions socketOptions;

    /* mqtt */
    private MqttClient mqttClient;
    private String clientId;
    private String username;
    private String password;
    private MqttClientConnectionEvents connectionCallbacks;
    private int keepAliveSecs = 0;
    private int pingTimeoutMs = 0;
    private long minReconnectTimeoutSecs = 0L;
    private long maxReconnectTimeoutSecs = 0L;
    private int protocolOperationTimeoutMs = 0;
    private boolean cleanSession = true;

    /* will */
    private MqttMessage willMessage;
    private QualityOfService deprecatedWillQos;
    private Boolean deprecatedWillRetain;

    /* websockets */
    private boolean useWebsockets = false;
    private HttpProxyOptions proxyOptions;
    private Consumer<WebsocketHandshakeTransformArgs> websocketHandshakeTransform;

    public MqttConnectionConfig() {}


    /**
     * Required override method that must begin the release process of the acquired native handle
     */
    @Override
    protected void releaseNativeHandle() {}

    /**
     * Override that determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources with asynchronous shutdown processes should override this with false, and establish a callback from native code that
     * invokes releaseReferences() when the asynchronous shutdown process has completed.  See HttpClientConnectionManager for an example.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /**
     * Configures the connection-related callbacks for a connection
     *
     * @param connectionCallbacks connection event callbacks to use
     */
    public void setConnectionCallbacks(MqttClientConnectionEvents connectionCallbacks) {
        this.connectionCallbacks = connectionCallbacks;
    }

    /**
     * Queries the connection-related callbacks for a connection
     *
     * @return the connection event callbacks to use
     */
    public MqttClientConnectionEvents getConnectionCallbacks() {
        return connectionCallbacks;
    }

    /**
     * Configures the client_id to use with a connection
     *
     * @param clientId The client id for a connection. Needs to be unique across
     *                  all devices/clients.this.credentialsProvider
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * Queries the client_id being used by a connection
     *
     * @return The client id for a connection.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Configures the IoT endpoint for a connection
     *
     * @param endpoint The IoT endpoint to connect to
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Queries the IoT endpoint used by a connection
     *
     * @return The IoT endpoint used by a connection
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * Configures the port to connect to.
     *
     * @param port The port to connect to. Usually 8883 for MQTT, or 443 for websockets
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Queries the port to connect to.
     *
     * @return The port to connect to
     */
    public int getPort() {
        return port;
    }

    /**
     * Configures the common settings to use for a connection's socket
     *
     * @param socketOptions The socket settings
     */
    public void setSocketOptions(SocketOptions socketOptions) {
        swapReferenceTo(this.socketOptions, socketOptions);
        this.socketOptions = socketOptions;
    }

    /**
     * Queries the common settings to use for a connection's socket
     *
     * @return The socket settings
     */
    public SocketOptions getSocketOptions() {
        return socketOptions;
    }

    /**
     * Configures whether or not the service should try to resume prior subscriptions, if it has any
     *
     * @param cleanSession true if the session should drop prior subscriptions when
     *                     a connection is established, false to resume the session
     */
    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    /**
     * Queries whether or not the service should try to resume prior subscriptions, if it has any
     *
     * @return true if the session should drop prior subscriptions when
     *                     a connection is established, false to resume the session
     */
    public boolean getCleanSession() {
        return cleanSession;
    }

    /**
     * @deprecated Configures MQTT keep-alive via PING messages. Note that this is not TCP
     * keepalive. Please use setKeepAliveSecs instead.
     *
     * @param keepAliveMs How often in milliseconds to send an MQTT PING message to the
     *                   service to keep a connection alive
     *
     */
    @Deprecated
    public void setKeepAliveMs(int keepAliveMs) {
        this.keepAliveSecs = keepAliveMs/1000;
    }

    /**
     * @deprecated Queries the MQTT keep-alive via PING messages. Please use
     * getKeepAliveSecs instead.
     *
     * @return How often in milliseconds to send an MQTT PING message to the
     *                   service to keep a connection alive
     */
    @Deprecated
    public int getKeepAliveMs() {
        return keepAliveSecs*1000;
    }

    /**
     * Configures MQTT keep-alive via PING messages. Note that this is not TCP
     * keepalive. Note: AWS IoT Core only allows 30-1200 Secs. Anything larger than
     * 65535 will be capped.
     *
     * @param keepAliveSecs How often in seconds to send an MQTT PING message to the
     *                   service to keep a connection alive
     *
     */
    public void setKeepAliveSecs(int keepAliveSecs) {
        this.keepAliveSecs = keepAliveSecs;
    }

    /**
     * Queries the MQTT keep-alive via PING messages.
     *
     * @return How often in seconds to send an MQTT PING message to the
     *                   service to keep a connection alive
     */
    public int getKeepAliveSecs() {
        return keepAliveSecs;
    }

    /**
     * Configures ping timeout value.  If a response is not received within this
     * interval, the connection will be reestablished.
     *
     * @param pingTimeoutMs How long to wait for a ping response (in milliseconds) before resetting the connection
     */
    public void setPingTimeoutMs(int pingTimeoutMs) {
        this.pingTimeoutMs = pingTimeoutMs;
    }

    /**
     * Queries ping timeout value.  If a response is not received within this
     * interval, the connection will be reestablished.
     *
     * @return How long to wait for a ping response before resetting the connection
     */
    public int getPingTimeoutMs() {
        return pingTimeoutMs;
    }

    /**
     * Configures the minimum and maximum reconnect timeouts.
     *
     * The time between reconnect attempts will start at min and multiply by 2 until max is reached.
     * Default value for min is 1, for max 128. Set either one to zero will use the default setting.
     *
     * @param minTimeoutSecs The timeout to start with
     * @param maxTimeoutSecs The highest allowable wait time between reconnect attempts
     */
    public void setReconnectTimeoutSecs(long minTimeoutSecs, long maxTimeoutSecs) {
        this.minReconnectTimeoutSecs = minTimeoutSecs;
        this.maxReconnectTimeoutSecs = maxTimeoutSecs;
    }

    /**
     * Return the minimum reconnect timeout.
     *
     * @return The timeout to start with
     */
    public long getMinReconnectTimeoutSecs() {
        return minReconnectTimeoutSecs;
    }

    /**
     * Return the maximum reconnect timeout.
     *
     * @return The highest allowable wait time between reconnect attempts
     */
    public long getMaxReconnectTimeoutSecs() {
        return maxReconnectTimeoutSecs;
    }

    /**
     * Configures timeout value for requests that response is required on healthy connection.
     * If a response is not received within this interval, the request will fail as server not receiving it.
     * Applied to publish (QoS&gt;0) and unsubscribe
     *
     * @param protocolOperationTimeoutMs How long to wait for a request response (in milliseconds) before failing
     */
    public void setProtocolOperationTimeoutMs(int protocolOperationTimeoutMs) {
        this.protocolOperationTimeoutMs = protocolOperationTimeoutMs;
    }

    /**
     * Queries timeout value for requests that response is required on healthy connection.
     * If a response is not received within this interval, the request will fail as server not receiving it.
     * Applied to publish (QoS&gt;0) and unsubscribe
     *
     * @return How long to wait for a request response (in milliseconds) before failing
     */
    public int getProtocolOperationTimeoutMs() {
        return protocolOperationTimeoutMs;
    }

    /**
     * Configures the mqtt client to use for a connection
     *
     * @param mqttClient the mqtt client to use
     */
    public void setMqttClient(MqttClient mqttClient) {
        swapReferenceTo(this.mqttClient, mqttClient);
        this.mqttClient = mqttClient;
    }

    /**
     * Queries the mqtt client to use for a connection
     *
     * @return the mqtt client to use
     */
    public MqttClient getMqttClient() {
        return mqttClient;
    }

    /**
     * Sets the login credentials for a connection.
     *
     * @param user Login username
     * @param pass Login password
     */
    public void setLogin(String user, String pass) throws MqttException {
        this.username = user;
        this.password = pass;
    }

    /**
     * Configures the username to use as part of the CONNECT attempt
     *
     * @param username username to use for the mqtt connect operation
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Queries the username to use as part of the CONNECT attempt
     *
     * @return username to use for the mqtt connect operation
     */
    public String getUsername() {
        return username;
    }

    /**
     * Configures the password to use as part of the CONNECT attempt
     *
     * @param password password to use for the mqtt connect operation
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Queries the password to use as part of the CONNECT attempt
     *
     * @return password to use for the mqtt connect operation
     */
    public String getPassword() {
        return password;
    }

    /**
     * Configures the last will and testament message to be delivered to a topic when a connection disconnects
     *
     * @param willMessage the message to publish as the will
     */
    public void setWillMessage(MqttMessage willMessage) {
        this.willMessage = willMessage;
    }

    /**
     * Queries the last will and testament message to be delivered to a topic when a connection disconnects
     *
     * @return the message to publish as the will
     */
    public MqttMessage getWillMessage() {
        if (willMessage == null || (deprecatedWillRetain == null && deprecatedWillQos == null)) {
            return willMessage;
        }
        QualityOfService qos = deprecatedWillQos == null ? willMessage.getQos() : deprecatedWillQos;
        boolean retain = deprecatedWillRetain == null ? willMessage.getRetain() : deprecatedWillRetain;
        return new MqttMessage(willMessage.getTopic(), willMessage.getPayload(), qos, retain);
    }

    /**
     * @deprecated Set QoS directly on the will's {@link MqttMessage}.
     */
    @Deprecated
    public void setWillQos(QualityOfService qos) {
        this.deprecatedWillQos = qos;
    }

    /**
     * @deprecated Query QoS directly from the will's {@link MqttMessage}.
     */
    @Deprecated
    public QualityOfService getWillQos() {
        if (deprecatedWillQos != null) {
            return deprecatedWillQos;
        }
        if (willMessage != null) {
            return willMessage.getQos();
        }
        return null;
    }

    /**
     * @deprecated Set retain directly on the will's {@link MqttMessage}.
     */
    @Deprecated
    public void setWillRetain(boolean retain) {
        this.deprecatedWillRetain = retain;
    }

    /**
     * @deprecated Query retain directly from the will's {@link MqttMessage}.
     */
    @Deprecated
    public boolean getWillRetain() {
        if (deprecatedWillRetain != null) {
            return deprecatedWillRetain;
        }
        if (willMessage != null) {
            return willMessage.getRetain();
        }
        return false;
    }

    /**
     * Configures whether or not to use websockets for the mqtt connection
     *
     * @param useWebsockets whether or not to use websockets
     */
    public void setUseWebsockets(boolean useWebsockets) {
        this.useWebsockets = useWebsockets;
    }

    /**
     * Queries whether or not to use websockets for the mqtt connection
     *
     * @return whether or not to use websockets
     */
    public boolean getUseWebsockets() {
        return useWebsockets;
    }

    /**
     * @deprecated use setHttpProxyOptions instead
     * Configures proxy options for a websocket-based mqtt connection
     *
     * @param proxyOptions proxy options to use for the base http connection
     */
    public void setWebsocketProxyOptions(HttpProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
    }

    /**
     * @deprecated use getHttpProxyOptions instead
     * Queries proxy options for a websocket-based mqtt connection
     *
     * @return proxy options to use for the base http connection
     */
    public HttpProxyOptions getWebsocketProxyOptions() {
        return proxyOptions;
    }


    /**
     * Configures proxy options for the mqtt connection
     *
     * @param proxyOptions proxy options to use for the connection
     */
    public void setHttpProxyOptions(HttpProxyOptions proxyOptions) {
        this.proxyOptions = proxyOptions;
    }

    /**
     * Queries proxy options for an mqtt connection
     *
     * @return proxy options to use for the connection
     */
    public HttpProxyOptions getHttpProxyOptions() {
        return proxyOptions;
    }

    /**
     * Set a transform operation to use on each websocket handshake http request.
     * The transform may modify the http request before it is sent to the server.
     * The transform MUST call handshakeTransform.complete() or handshakeTransform.completeExceptionally()
     * when the transform is complete, failure to do so will stall the mqtt connection indefinitely.
     * The transform operation may be asynchronous.
     *
     * The default websocket handshake http request uses path "/mqtt".
     * All required headers for a websocket handshake are present,
     * plus the optional header "Sec-WebSocket-Protocol: mqtt".
     *
     * This is only applicable to websocket-based mqtt connections.
     *
     * @param handshakeTransform http request handshake transform
     */
    public void setWebsocketHandshakeTransform(Consumer<WebsocketHandshakeTransformArgs> handshakeTransform) {
        this.websocketHandshakeTransform = handshakeTransform;
    }

    /**
     * Queries the handshake http request transform to use when upgrading the connection
     *
     * @return http request handshake transform
     */
    public Consumer<WebsocketHandshakeTransformArgs> getWebsocketHandshakeTransform() {
        return websocketHandshakeTransform;
    }

    /**
     * Creates a (shallow) clone of this config object
     *
     * @return shallow clone of this config object
     */
    public MqttConnectionConfig clone() {
        try (MqttConnectionConfig clone = new MqttConnectionConfig()) {
            clone.setEndpoint(getEndpoint());
            clone.setPort(getPort());
            clone.setSocketOptions(getSocketOptions());

            clone.setMqttClient(getMqttClient());
            clone.setClientId(getClientId());
            clone.setUsername(getUsername());
            clone.setPassword(getPassword());
            clone.setConnectionCallbacks(getConnectionCallbacks());
            clone.setKeepAliveSecs(getKeepAliveSecs());
            clone.setPingTimeoutMs(getPingTimeoutMs());
            clone.setProtocolOperationTimeoutMs(getProtocolOperationTimeoutMs());
            clone.setCleanSession(getCleanSession());

            clone.setWillMessage(getWillMessage());

            clone.setUseWebsockets(getUseWebsockets());
            clone.setHttpProxyOptions(getHttpProxyOptions());
            clone.setWebsocketHandshakeTransform(getWebsocketHandshakeTransform());

            clone.setReconnectTimeoutSecs(getMinReconnectTimeoutSecs(), getMaxReconnectTimeoutSecs());
                
            // success, bump up the ref count so we can escape the try-with-resources block
            clone.addRef();
            return clone;
        }
    }
}
