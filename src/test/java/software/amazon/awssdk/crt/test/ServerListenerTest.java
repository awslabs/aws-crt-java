package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.eventstream.*;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.ServerBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

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
        socketOptions.close();
    }

    @Test
    public void testBindErrorPropagates() throws ExecutionException, InterruptedException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        ServerListener listener1 = new ServerListener("127.0.0.1", (short)8032, socketOptions, null, bootstrap, new ServerListenerHandler() {
            public ServerConnectionHandler onNewConnection(ServerConnection serverConnection, int errorCode) {
                return null;
            }

            public void onConnectionShutdown(ServerConnection serverConnection, int errorCode) {
            }
        });

        assertNotNull(listener1);
        boolean exceptionThrown = false;
        try {
        ServerListener listener2 = new ServerListener("127.0.0.1", (short)8032, socketOptions, null, bootstrap, new ServerListenerHandler() {
            public ServerConnectionHandler onNewConnection(ServerConnection serverConnection, int errorCode) {
                return null;
            }

            public void onConnectionShutdown(ServerConnection serverConnection, int errorCode) {
            }
        });
        } catch (CrtRuntimeException ex) {
            assertTrue(ex.getMessage().contains("AWS_IO_SOCKET_ADDRESS_IN_USE(1054), Socket address already in use."));
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        listener1.close();
        listener1.getShutdownCompleteFuture().get();
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

        // this should be damn near instant, but give it a second just to be safe.
        Thread.sleep(1000);
        clientSocket.close();
        // also, should fire within a millisecond, but just give it a second.
        Thread.sleep(1000);
        assertTrue(connectionReceived[0]);
        assertTrue(connectionShutdown[0]);

        listener.close();
        listener.getShutdownCompleteFuture().get();
        bootstrap.close();
        elGroup.close();
        socketOptions.close();
    }

    @Test
    public void testConnectionProtocolMessageHandling() throws ExecutionException, InterruptedException, IOException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        final boolean[] connectionReceived = {false};
        final boolean[] connectionShutdown = {false};

        final List<Header>[] receivedMessageHeaders = new List[]{null};
        final byte[][] receivedPayload = {null};
        final MessageType[] receivedMessageType = {null};
        final int[] receivedMessageFlags = {-1};

        ServerListener listener = new ServerListener("127.0.0.1", (short)8033, socketOptions, null, bootstrap, new ServerListenerHandler() {
            private ServerConnectionHandler connectionHandler = null;

            public ServerConnectionHandler onNewConnection(ServerConnection serverConnection, int errorCode) {
                connectionReceived[0] = true;

                connectionHandler = new ServerConnectionHandler(serverConnection) {

                    @Override
                    protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                        receivedMessageHeaders[0] = headers;
                        receivedPayload[0] = payload;
                        receivedMessageType[0] = messageType;
                        receivedMessageFlags[0] = messageFlags;
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

        Header messageType = Header.createHeader(":message-type", (int)MessageType.Connect.getEnumValue());
        Header messageFlags = Header.createHeader(":message-flags", 0);
        Header streamId = Header.createHeader(":stream-id", 0);

        List<Header> messageHeaders = new ArrayList<>(3);
        messageHeaders.add(messageType);
        messageHeaders.add(messageFlags);
        messageHeaders.add(streamId);

        String payload = "{\"message\": \"connect payload\"}";
        Message connectMessage = new Message(messageHeaders, payload.getBytes(StandardCharsets.UTF_8));
        ByteBuffer connectMessageBuf = connectMessage.getMessageBuffer();
        byte[] toSend = new byte[connectMessageBuf.remaining()];
        connectMessageBuf.get(toSend);
        clientSocket.getOutputStream().write(toSend);

        // this should be damn near instant, but give it a second just to be safe.
        Thread.sleep(1000);
        clientSocket.close();
        // also, should fire within a millisecond, but just give it a second.
        Thread.sleep(1000);
        assertTrue(connectionReceived[0]);
        assertTrue(connectionShutdown[0]);
        assertNotNull(receivedMessageHeaders[0]);
        assertEquals(3, receivedMessageHeaders[0].size());
        assertEquals(":message-type", receivedMessageHeaders[0].get(0).getName());
        assertEquals((int)MessageType.Connect.getEnumValue(), receivedMessageHeaders[0].get(0).getValueAsInt());
        assertEquals(":message-flags", receivedMessageHeaders[0].get(1).getName());
        assertEquals(0, receivedMessageHeaders[0].get(1).getValueAsInt());
        assertEquals(":stream-id", receivedMessageHeaders[0].get(2).getName());
        assertEquals(0, receivedMessageHeaders[0].get(2).getValueAsInt());
        assertNotNull(receivedPayload[0]);
        assertEquals(payload, new String(receivedPayload[0], StandardCharsets.UTF_8));
        assertEquals(MessageType.Connect, receivedMessageType[0]);
        assertEquals(0, receivedMessageFlags[0]);

        assertEquals(payload, new String(receivedPayload[0], StandardCharsets.UTF_8));
        listener.close();
        listener.getShutdownCompleteFuture().get();
        bootstrap.close();
        elGroup.close();
        socketOptions.close();
    }

    @Test
    public void testContinuationMessageHandling() throws ExecutionException, InterruptedException, IOException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        final boolean[] connectionReceived = {false};
        final boolean[] connectionShutdown = {false};
        final boolean[] continuationClosed = {false};

        final String[] receivedOperationName = new String[]{null};
        final String[] receivedContinuationPayload = new String[]{null};

        ServerListener listener = new ServerListener("127.0.0.1", (short)8033, socketOptions, null, bootstrap, new ServerListenerHandler() {
            private ServerConnectionHandler connectionHandler = null;

            public ServerConnectionHandler onNewConnection(ServerConnection serverConnection, int errorCode) {
                connectionReceived[0] = true;

                connectionHandler = new ServerConnectionHandler(serverConnection) {

                    @Override
                    protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                        int responseMessageFlag = MessageFlags.ConnectionAccepted.getByteValue();
                        MessageType acceptResponseType = MessageType.ConnectAck;

                        connection.sendProtocolMessage(null, null, acceptResponseType, responseMessageFlag);
                    }

                    @Override
                    protected ServerConnectionContinuationHandler onIncomingStream(ServerConnectionContinuation continuation, String operationName) {
                        receivedOperationName[0] = operationName;

                        return new ServerConnectionContinuationHandler(continuation) {
                            @Override
                            protected void onContinuationClosed() {
                                continuationClosed[0] = true;
                                this.close();
                            }

                            @Override
                            protected void onContinuationMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                                receivedContinuationPayload[0] = new String(payload, StandardCharsets.UTF_8);

                                String responsePayload = "{ \"message\": \"this is a response message\" }";
                                continuation.sendMessage(null, responsePayload.getBytes(StandardCharsets.UTF_8),
                                        MessageType.ApplicationMessage,
                                        MessageFlags.TerminateStream.getByteValue())
                                        .whenComplete((res, ex) ->  {
                                            connection.closeConnection(0);
                                        });
                            }
                        };
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

        Header messageType = Header.createHeader(":message-type", (int)MessageType.Connect.getEnumValue());
        Header messageFlags = Header.createHeader(":message-flags", 0);
        Header streamId = Header.createHeader(":stream-id", 0);

        List<Header> messageHeaders = new ArrayList<>(3);
        messageHeaders.add(messageType);
        messageHeaders.add(messageFlags);
        messageHeaders.add(streamId);

        Message connectMessage = new Message(messageHeaders, null);
        ByteBuffer connectMessageBuf = connectMessage.getMessageBuffer();
        byte[] toSend = new byte[connectMessageBuf.remaining()];
        connectMessageBuf.get(toSend);
        connectMessage.close();
        clientSocket.getOutputStream().write(toSend);

        // this should be damn near instant, but give it a second just to be safe.
        Thread.sleep(1000);

        String operationName = "testOperation";
        messageHeaders = new ArrayList<>(3);
        messageHeaders.add(Header.createHeader(":message-type", (int)MessageType.ApplicationMessage.getEnumValue()));
        messageHeaders.add(Header.createHeader(":message-flags", 0));
        messageHeaders.add(Header.createHeader(":stream-id", 1));
        messageHeaders.add(Header.createHeader("operation", operationName));
        String payload = "{\"message\": \"message payload\"}";
        Message continuationMessage = new Message(messageHeaders, payload.getBytes(StandardCharsets.UTF_8));
        ByteBuffer continuationMessageBuf = continuationMessage.getMessageBuffer();
        toSend = new byte[continuationMessageBuf.remaining()];
        continuationMessageBuf.get(toSend);
        continuationMessage.close();
        clientSocket.getOutputStream().write(toSend);

        // also, should fire within a millisecond, but just give it a second.
        Thread.sleep(1000);
        clientSocket.close();
        // also, should fire within a millisecond, but just give it a second.
        Thread.sleep(1000);
        assertTrue(connectionReceived[0]);
        assertTrue(connectionShutdown[0]);
        assertNotNull(receivedOperationName[0]);
        assertEquals(operationName, receivedOperationName[0]);
        assertEquals(payload, receivedContinuationPayload[0]);
        listener.close();
        listener.getShutdownCompleteFuture().get();
        bootstrap.close();
        elGroup.close();
        socketOptions.close();
    }
}
