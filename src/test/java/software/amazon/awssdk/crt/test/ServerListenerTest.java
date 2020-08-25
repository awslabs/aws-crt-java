package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.eventstream.*;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.ServerBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;

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
        ServerListener listener = new ServerListener("127.0.0.1", (short)8032, socketOptions, null, bootstrap, new ServerListenerHandler() {
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

    @Test
    public void testConnectionHandling() throws ExecutionException, InterruptedException, IOException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        final boolean[] connectionReceived = {false};
        final boolean[] connectionShutdown = {false};

        ServerListener listener = new ServerListener("127.0.0.1", (short)8033, socketOptions, null, bootstrap, new ServerListenerHandler() {
            private ServerConnectionHandler connectionHandler = null;

            public ServerConnectionHandler onNewConnection(ServerConnection serverConnection, int errorCode) {
                connectionReceived[0] = true;

                connectionHandler = new ServerConnectionHandler(serverConnection) {

                    @Override
                    protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                    }

                    @Override
                    protected ServerConnectionContinuationHandler onIncomingStream(ServerConnectionContinuation continuation, String operationName) {
                        return null;
                    }
                };

                return connectionHandler;
            }

            public void onConnectionShutdown(ServerConnection serverConnection, int errorCode) {
                connectionShutdown[0] = true;
                connectionHandler.close();
            }
        });

        Socket clientSocket = new Socket();
        SocketAddress address = new InetSocketAddress("127.0.0.1", 8033);
        clientSocket.connect(address, 3000);

        Thread.sleep(1000);
        clientSocket.close();
        Thread.sleep(1000);
        assertTrue(connectionReceived[0]);
        assertTrue(connectionShutdown[0]);

        listener.close();
        listener.getShutdownCompleteFuture().get();
        bootstrap.close();
        elGroup.close();
    }
}
