package software.amazon.awssdk.crt.eventstream;

/**
 * Handler interface for processing incoming event-stream-rpc connections and their lifetimes.
 */
public abstract class ServerListenerHandler {
    /**
     * Invoked upon receiving a new connection, or if an error happened upon connection
     * creation. If errorCode is non-zero, onConnectionShutdown() will never be invoked.
     *
     * @param serverConnection The new server connection to use for communications. Is non-null
     *                         when errorCode is 0.
     * @param errorCode represents any error that occurred during connection establishment
     * @return Return an instance of ServerConnectionHandler, for processing connection specific events.
     */
    protected abstract ServerConnectionHandler onNewConnection(final ServerConnection serverConnection, int errorCode);

    /**
     * Invoked upon connection shutdown. serverConnection will never be null. This function is
     * only invoked if onNewConnection() was invoked with a zero errorCode.
     * @param serverConnection connection the shutdown occurred on.
     * @param errorCode shutdown reason. 0 means clean shutdown.
     */
    protected abstract void onConnectionShutdown(final ServerConnection serverConnection, int errorCode);

    /**
     * Invoked from JNI. Completes the closure future and invokes onConnectionShutdown()
     */
    void onConnectionShutdownShim(final ServerConnection serverConnection, int errorCode) {
        onConnectionShutdown(serverConnection, errorCode);
        serverConnection.closedFuture.complete(errorCode);
    }
}
