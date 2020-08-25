package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.eventstream.ServerConnection;
import software.amazon.awssdk.crt.eventstream.ServerConnectionHandler;
import software.amazon.awssdk.crt.eventstream.ServerListener;
import software.amazon.awssdk.crt.eventstream.ServerListenerHandler;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.ServerBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;

import java.util.concurrent.ExecutionException;

public class ServerListenerTest extends CrtTestFixture {
    public ServerListenerTest() {}

    @Test
    public void testSetupAndTearDown() throws ExecutionException, InterruptedException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        ServerListener listener = new ServerListener("127.0.0.1", (short)86754, socketOptions, null, bootstrap, new ServerListenerHandler() {
            public ServerConnectionHandler onNewConnection(ServerConnection serverConnection, int errorCode) {
                return null;
            }

            public void onConnectionShutdown(ServerConnection serverConnection, int errorCode) {
            }
        });

        listener.close();
        listener.getShutdownCompleteFuture().get();
        bootstrap.close();
        elGroup.close();
    }
}
