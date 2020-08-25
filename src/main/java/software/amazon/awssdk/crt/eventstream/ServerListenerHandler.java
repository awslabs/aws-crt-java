package software.amazon.awssdk.crt.eventstream;

public interface ServerListenerHandler {
    ServerConnectionHandler onNewConnection(final ServerConnection serverConnection, int errorCode);
    void onConnectionShutdown(final ServerConnection serverConnection, int errorCode);
}
