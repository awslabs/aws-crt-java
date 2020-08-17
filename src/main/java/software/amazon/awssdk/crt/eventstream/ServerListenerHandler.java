package software.amazon.awssdk.crt.eventstream;

public abstract class ServerListenerHandler implements AutoCloseable {
    protected ServerListener listener;

    protected ServerListenerHandler(final ServerListener listener) {
        this.listener = listener;
        this.listener.addRef();
    }

    protected abstract ServerConnectionHandler onNewConnection(final ServerConnection serverConnection, int errorCode);
    protected abstract void onConnectionShutdown(final ServerConnection serverConnection, int errorCode);

    @Override
    public void close() {
        listener.decRef();
        listener = null;
    }
}
