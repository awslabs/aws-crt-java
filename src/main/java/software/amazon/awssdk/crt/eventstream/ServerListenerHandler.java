package software.amazon.awssdk.crt.eventstream;

public abstract class ServerListenerHandler {
    protected abstract ServerConnectionHandler onNewConnection(final ServerConnection serverConnection, int errorCode);
    protected abstract void onConnectionShutdown(final ServerConnection serverConnection, int errorCode);

    void onConnectionShutdownShim(final ServerConnection serverConnection, int errorCode) {
        onConnectionShutdown(serverConnection, errorCode);
        serverConnection.closedFuture.complete(errorCode);
    }
}
