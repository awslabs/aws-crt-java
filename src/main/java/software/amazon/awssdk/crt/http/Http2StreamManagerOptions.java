package software.amazon.awssdk.crt.http;

import java.util.List;
import java.util.ArrayList;

/**
 * Contains all the configuration options for a Http2StreamManager
 * instance
 */
public class Http2StreamManagerOptions {
    public static final int DEFAULT_MAX_WINDOW_SIZE = Integer.MAX_VALUE;
    public static final int DEFAULT_MAX = Integer.MAX_VALUE;
    public static final int DEFAULT_MAX_CONNECTIONS = 2;

    private HttpClientConnectionManagerOptions connectionManagerOptions;

    private int idealConcurrentStreamsPerConnection = 100;
    private boolean connectionManualWindowManagement = false;
    private int maxConcurrentStreamsPerConnection = DEFAULT_MAX;

    private List<Http2ConnectionSetting> initialSettingsList = new ArrayList<Http2ConnectionSetting>();

    /**
     * Default constructor
     */
    public Http2StreamManagerOptions() {
    }

    /**
     * For HTTP/2 stream manager only.
     *
     * The initial settings for the HTTP/2 connections made by stream manger.
     * `Http2ConnectionSettingListBuilder` can help to build the settings list.
     *
     * To control the initial stream-level flow-control window, set the INITIAL_WINDOW_SIZE setting in the initial settings.
     *
     * @param initialSettingsList The List of initial settings
     * @return this
     */
    public Http2StreamManagerOptions withInitialSettingsList(List<Http2ConnectionSetting> initialSettingsList) {
        this.initialSettingsList.addAll(initialSettingsList);
        return this;
    }

    /**
     * @return The List of initial settings
     */
    public List<Http2ConnectionSetting> getInitialSettingsList() {
        return this.initialSettingsList;
    }

    /**
     * For HTTP/2 stream manager only.
     *
     * The ideal number of concurrent streams for a connection. Stream manager will
     * try to create a new connection if one connection reaches this number. But, if
     * the max connections reaches, manager will reuse connections to create the
     * acquired steams as much as possible.
     *
     * @param idealConcurrentStreamsPerConnection The ideal number of concurrent
     *                                            streams for a connection
     * @return this
     */
    public Http2StreamManagerOptions withIdealConcurrentStreamsPerConnection(int idealConcurrentStreamsPerConnection) {
        this.idealConcurrentStreamsPerConnection = idealConcurrentStreamsPerConnection;
        return this;
    }

    /**
     * @return The ideal number of concurrent streams for a connection used for
     *         manager
     */
    public int getIdealConcurrentStreamsPerConnection() {
        return idealConcurrentStreamsPerConnection;
    }

    /**
     * Default is no limit, which will use the limit from the server. 0 will be
     * considered as using the default value.
     * The real number of concurrent streams per connection will be controlled by
     * the minimal value of the setting from other end and the value here.
     *
     * @param maxConcurrentStreamsPerConnection The max number of concurrent
     *                                          streams for a connection
     * @return this
     */
    public Http2StreamManagerOptions withMaxConcurrentStreamsPerConnection(int maxConcurrentStreamsPerConnection) {
        this.maxConcurrentStreamsPerConnection = maxConcurrentStreamsPerConnection;
        return this;
    }

    /**
     * @return The max number of concurrent streams for a connection set for
     *         manager.
     *         It could be different than the real limits, which is the minimal set
     *         for manager and the settings from the other side.
     */
    public int getMaxConcurrentStreamsPerConnection() {
        return maxConcurrentStreamsPerConnection;
    }

    /**
     * @return The connection level manual flow control enabled or not.
     */
    public boolean isConnectionManualWindowManagement() {
        return connectionManualWindowManagement;
    }

    /**
     * Set to true to manually manage the flow-control window of whole HTTP/2 connection.
     * The stream level flow-control window is controlled by the manualWindowManagement in connectionManagerOptions.
     *
     * @param connectionManualWindowManagement Enable connection level manual flow control or not.
     * @return this
     */
    public Http2StreamManagerOptions withConnectionManualWindowManagement(boolean connectionManualWindowManagement) {
        this.connectionManualWindowManagement = connectionManualWindowManagement;
        return this;
    }

    /**
     * @return The connection manager options for the underlying connection manager.
     */
    public HttpClientConnectionManagerOptions getConnectionManagerOptions() {
        return connectionManagerOptions;
    }

    /**
     * Required.
     *
     * The configuration options for the connection manager under the hood.
     * It controls the connection specific thing for the stream manager. See `HttpClientConnectionManagerOptions` for details.
     *
     * Note:
     *    1. the windowSize of connection manager will be ignored, as the initial flow-control window size for HTTP/2 stream
     *       is controlled by the initial settings.
     *    2. The expectedHttpVersion will also be ignored.
     *
     * @param connectionManagerOptions The connection manager options for the underlying connection manager
     * @return this
     */
    public Http2StreamManagerOptions withConnectionManagerOptions(HttpClientConnectionManagerOptions connectionManagerOptions) {
        this.connectionManagerOptions = connectionManagerOptions;
        return this;
    }

    /**
     * Validate the stream manager options are valid to use. Throw exceptions if not.
     */
    public void validateOptions() {
        if(connectionManagerOptions == null) {
            throw new IllegalArgumentException("Connection manager options are required.");
        }
        connectionManagerOptions.validateOptions();
        if (maxConcurrentStreamsPerConnection <= 0) {
            throw new IllegalArgumentException("Max Concurrent Streams Per Connection must be greater than zero.");
        }
        if (idealConcurrentStreamsPerConnection <= 0
                || idealConcurrentStreamsPerConnection > maxConcurrentStreamsPerConnection) {
            throw new IllegalArgumentException(
                    "Ideal Concurrent Streams Per Connection must be greater than zero and smaller than max.");
        }
    }
}
