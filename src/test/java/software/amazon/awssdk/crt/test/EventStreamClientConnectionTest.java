package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.eventstream.*;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.ServerBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.*;

public class EventStreamClientConnectionTest extends CrtTestFixture {
    public EventStreamClientConnectionTest() {}

    @Test
    public void testConnectionHandling() throws ExecutionException, InterruptedException, IOException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, null);
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

        final boolean[] clientConnected = {false};
        final boolean[] clientConnectionClosed = {false};
        final ClientConnection[] clientConnectionArray = {null};

        CompletableFuture<Void> connectFuture = ClientConnection.connect("127.0.0.1", (short)8033, socketOptions, null, clientBootstrap, new ClientConnectionHandler() {
            @Override
            protected void onConnectionSetup(ClientConnection connection, int errorCode) {
                clientConnected[0] = true;
                clientConnectionArray[0] = connection;
            }

            @Override
            protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {

            }

            @Override
            protected void onConnectionClosed(int closeReason) {
                clientConnectionClosed[0] = true;
                this.close();
            }
        });

        connectFuture.get();
        assertNotNull(clientConnectionArray[0]);
        clientConnectionArray[0].closeConnection(0);
        // this should be damn near instant, but give it a second just to be safe.
        Thread.sleep(1000);
        assertTrue(connectionReceived[0]);
        assertTrue(connectionShutdown[0]);
        assertTrue(clientConnected[0]);
        assertTrue(clientConnectionClosed[0]);
        listener.close();
        listener.getShutdownCompleteFuture().get();
        bootstrap.close();
        clientBootstrap.close();
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
        ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, null);
        final boolean[] connectionReceived = {false};
        final boolean[] connectionShutdown = {false};
        final List<Header>[] receivedMessageHeaders = new List[]{null};
        final byte[][] receivedPayload = {null};
        final MessageType[] receivedMessageType = {null};
        final int[] receivedMessageFlags = {-1};

        final byte[] responseMessage = "{ \"message\": \"connect ack\" }".getBytes(StandardCharsets.UTF_8);

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

                        serverConnection.sendProtocolMessage(null, responseMessage, MessageType.ConnectAck, MessageFlags.ConnectionAccepted.getByteValue());
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

        final boolean[] clientConnected = {false};
        final boolean[] clientConnectionClosed = {false};
        final ClientConnection[] clientConnectionArray = {null};
        final List<Header>[] clientReceivedMessageHeaders = new List[]{null};
        final byte[][] clientReceivedPayload = {null};
        final MessageType[] clientReceivedMessageType = {null};
        final int[] clientReceivedMessageFlags = {-1};


        CompletableFuture<Void> connectFuture = ClientConnection.connect("127.0.0.1", (short)8033, socketOptions, null, clientBootstrap, new ClientConnectionHandler() {
            @Override
            protected void onConnectionSetup(ClientConnection connection, int errorCode) {
                clientConnected[0] = true;
                clientConnectionArray[0] = connection;
            }

            @Override
            protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                clientReceivedMessageHeaders[0] = headers;
                clientReceivedPayload[0] = payload;
                clientReceivedMessageType[0] = messageType;
                clientReceivedMessageFlags[0] = messageFlags;
            }

            @Override
            protected void onConnectionClosed(int closeReason) {
                clientConnectionClosed[0] = true;
                this.close();
            }
        });

        final byte[] connectPayload = "test connect payload".getBytes(StandardCharsets.UTF_8);
        connectFuture.get();
        assertNotNull(clientConnectionArray[0]);
        clientConnectionArray[0].sendProtocolMessage(null, connectPayload, MessageType.Connect, 0).get();
        // this should be damn near instant, but give it a second just to be safe.
        Thread.sleep(1000);
        assertEquals(MessageType.Connect, receivedMessageType[0]);
        assertArrayEquals(connectPayload, receivedPayload[0]);
        assertEquals(MessageType.ConnectAck, clientReceivedMessageType[0]);
        assertEquals(MessageFlags.ConnectionAccepted.getByteValue(), clientReceivedMessageFlags[0]);
        assertArrayEquals(responseMessage, clientReceivedPayload[0]);
        clientConnectionArray[0].closeConnection(0);
        Thread.sleep(1000);

        assertTrue(connectionReceived[0]);
        assertTrue(connectionShutdown[0]);
        assertTrue(clientConnected[0]);
        assertTrue(clientConnectionClosed[0]);
        listener.close();
        listener.getShutdownCompleteFuture().get();
        bootstrap.close();
        clientBootstrap.close();
        elGroup.close();
        socketOptions.close();
    }

    @Test
    public void testConnectionProtocolMessageWithExtraHeadersHandling() throws ExecutionException, InterruptedException, IOException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, null);
        final boolean[] connectionReceived = {false};
        final boolean[] connectionShutdown = {false};
        final List<Header>[] receivedMessageHeaders = new List[]{null};
        final byte[][] receivedPayload = {null};
        final MessageType[] receivedMessageType = {null};
        final int[] receivedMessageFlags = {-1};

        final byte[] responseMessage = "{ \"message\": \"connect ack\" }".getBytes(StandardCharsets.UTF_8);

        Header serverStrHeader = Header.createHeader("serverStrHeaderName", "serverStrHeaderValue");
        Header serverIntHeader = Header.createHeader("serverIntHeaderName", 25);

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

                        List<Header> respHeaders = new ArrayList<>();
                        respHeaders.add(serverStrHeader);
                        respHeaders.add(serverIntHeader);
                        serverConnection.sendProtocolMessage(respHeaders, responseMessage, MessageType.ConnectAck, MessageFlags.ConnectionAccepted.getByteValue());
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

        final boolean[] clientConnected = {false};
        final boolean[] clientConnectionClosed = {false};
        final ClientConnection[] clientConnectionArray = {null};
        final List<Header>[] clientReceivedMessageHeaders = new List[]{null};
        final byte[][] clientReceivedPayload = {null};
        final MessageType[] clientReceivedMessageType = {null};
        final int[] clientReceivedMessageFlags = {-1};


        CompletableFuture<Void> connectFuture = ClientConnection.connect("127.0.0.1", (short)8033, socketOptions, null, clientBootstrap, new ClientConnectionHandler() {
            @Override
            protected void onConnectionSetup(ClientConnection connection, int errorCode) {
                clientConnected[0] = true;
                clientConnectionArray[0] = connection;
            }

            @Override
            protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                clientReceivedMessageHeaders[0] = headers;
                clientReceivedPayload[0] = payload;
                clientReceivedMessageType[0] = messageType;
                clientReceivedMessageFlags[0] = messageFlags;
            }

            @Override
            protected void onConnectionClosed(int closeReason) {
                clientConnectionClosed[0] = true;
                this.close();
            }
        });

        final byte[] connectPayload = "test connect payload".getBytes(StandardCharsets.UTF_8);
        connectFuture.get();
        assertNotNull(clientConnectionArray[0]);

        Header clientStrHeader = Header.createHeader("clientStrHeaderName", "clientStrHeaderValue");
        Header clientIntHeader = Header.createHeader("clientIntHeaderName", 35);
        List<Header> clientHeaders = new ArrayList<>();
        clientHeaders.add(clientStrHeader);
        clientHeaders.add(clientIntHeader);
        clientConnectionArray[0].sendProtocolMessage(clientHeaders, connectPayload, MessageType.Connect, 0).get();
        // this should be damn near instant, but give it a second just to be safe.
        Thread.sleep(1000);
        assertEquals(MessageType.Connect, receivedMessageType[0]);
        assertArrayEquals(connectPayload, receivedPayload[0]);
        assertNotNull(receivedMessageHeaders[0]);
        assertEquals(clientStrHeader.getName(), receivedMessageHeaders[0].get(0).getName());
        assertEquals(clientStrHeader.getValueAsString(), receivedMessageHeaders[0].get(0).getValueAsString());
        assertEquals(clientIntHeader.getName(), receivedMessageHeaders[0].get(1).getName());
        assertEquals(clientIntHeader.getValueAsInt(), receivedMessageHeaders[0].get(1).getValueAsInt());
        assertEquals(MessageType.ConnectAck, clientReceivedMessageType[0]);
        assertEquals(MessageFlags.ConnectionAccepted.getByteValue(), clientReceivedMessageFlags[0]);
        assertArrayEquals(responseMessage, clientReceivedPayload[0]);
        assertEquals(serverStrHeader.getName(), clientReceivedMessageHeaders[0].get(0).getName());
        assertEquals(serverStrHeader.getValueAsString(), clientReceivedMessageHeaders[0].get(0).getValueAsString());
        assertEquals(serverIntHeader.getName(), clientReceivedMessageHeaders[0].get(1).getName());
        assertEquals(serverIntHeader.getValueAsInt(), clientReceivedMessageHeaders[0].get(1).getValueAsInt());
        clientConnectionArray[0].closeConnection(0);
        Thread.sleep(1000);

        assertTrue(connectionReceived[0]);
        assertTrue(connectionShutdown[0]);
        assertTrue(clientConnected[0]);
        assertTrue(clientConnectionClosed[0]);
        listener.close();
        listener.getShutdownCompleteFuture().get();
        bootstrap.close();
        clientBootstrap.close();
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
        ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, null);

        final boolean[] connectionReceived = {false};
        final boolean[] connectionShutdown = {false};
        final boolean[] continuationClosed = {false};

        final String[] receivedOperationName = new String[]{null};
        final String[] receivedContinuationPayload = new String[]{null};

        final byte[] responsePayload = "{ \"message\": \"this is a response message\" }".getBytes(StandardCharsets.UTF_8);

        ServerListener listener = new ServerListener("127.0.0.1", (short)8034, socketOptions, null, bootstrap, new ServerListenerHandler() {
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
                                close();
                            }

                            @Override
                            protected void onContinuationMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                                receivedContinuationPayload[0] = new String(payload, StandardCharsets.UTF_8);

                                continuation.sendMessage(null, responsePayload,
                                        MessageType.ApplicationError,
                                        MessageFlags.TerminateStream.getByteValue())
                                        .whenComplete((res, ex) ->  {
                                            connection.closeConnection(0);
                                            this.close();
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

        final boolean[] clientConnected = {false};
        final boolean[] clientConnectionClosed = {false};
        final ClientConnection[] clientConnectionArray = {null};
        final List<Header>[] clientReceivedMessageHeaders = new List[]{null};
        final byte[][] clientReceivedPayload = {null};
        final MessageType[] clientReceivedMessageType = {null};
        final int[] clientReceivedMessageFlags = {-1};
        final boolean[] clientContinuationClosed = {false};


        CompletableFuture<Void> connectFuture = ClientConnection.connect("127.0.0.1", (short)8034, socketOptions, null, clientBootstrap, new ClientConnectionHandler() {
            @Override
            protected void onConnectionSetup(ClientConnection connection, int errorCode) {
                clientConnected[0] = true;
                clientConnectionArray[0] = connection;
            }

            @Override
            protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
            }

            @Override
            protected void onConnectionClosed(int closeReason) {
                clientConnectionClosed[0] = true;
                this.close();
            }
        });

        final byte[] connectPayload = "test connect payload".getBytes(StandardCharsets.UTF_8);
        connectFuture.get();
        assertNotNull(clientConnectionArray[0]);
        clientConnectionArray[0].sendProtocolMessage(null, connectPayload, MessageType.Connect, 0).get();
        // this should be damn near instant, but give it a second just to be safe.
        Thread.sleep(1000);
        String operationName = "testOperation";

        ClientConnectionContinuation continuation = clientConnectionArray[0].newStream(new ClientConnectionContinuationHandler() {
            @Override
            protected void onContinuationMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                clientReceivedMessageHeaders[0] = headers;
                clientReceivedMessageType[0] = messageType;
                clientReceivedMessageFlags[0] = messageFlags;
                clientReceivedPayload[0] = payload;
            }

            @Override
            protected void onContinuationClosed() {
                clientContinuationClosed[0] = true;
                super.onContinuationClosed();
            }
        });
        assertNotNull(continuation);

        final byte[] operationPayload = "{\"message\": \"message payload\"}".getBytes(StandardCharsets.UTF_8);
        continuation.activate(operationName, null, operationPayload, MessageType.ApplicationMessage, 0).get();
        Thread.sleep(1000);

        assertArrayEquals(responsePayload, clientReceivedPayload[0]);
        assertEquals(MessageType.ApplicationError, clientReceivedMessageType[0]);
        assertEquals(MessageFlags.TerminateStream.getByteValue(), clientReceivedMessageFlags[0]);
        assertTrue(continuationClosed[0]);
        assertTrue(clientContinuationClosed[0]);

        // also, should fire within a millisecond, but just give it a second.
        Thread.sleep(1000);

        assertTrue(connectionReceived[0]);
        assertTrue(connectionShutdown[0]);
        assertNotNull(receivedOperationName[0]);
        assertEquals(operationName, receivedOperationName[0]);
        assertEquals(new String(operationPayload, StandardCharsets.UTF_8), receivedContinuationPayload[0]);
        listener.close();
        listener.getShutdownCompleteFuture().get();
        bootstrap.close();
        clientBootstrap.close();
        elGroup.close();
        socketOptions.close();
    }

    @Test
    public void testContinuationMessageWithExtraHeadersHandling() throws ExecutionException, InterruptedException, IOException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, null);

        final boolean[] connectionReceived = {false};
        final boolean[] connectionShutdown = {false};
        final boolean[] continuationClosed = {false};

        final String[] receivedOperationName = new String[]{null};
        final String[] receivedContinuationPayload = new String[]{null};
        final List<Header>[] receivedHeadersServer = new List[]{null};

        Header serverStrHeader = Header.createHeader("serverStrHeaderName", "serverStrHeaderValue");
        Header serverIntHeader = Header.createHeader("serverIntHeaderName", 25);

        final byte[] responsePayload = "{ \"message\": \"this is a response message\" }".getBytes(StandardCharsets.UTF_8);

        ServerListener listener = new ServerListener("127.0.0.1", (short)8034, socketOptions, null, bootstrap, new ServerListenerHandler() {
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
                                close();
                            }

                            @Override
                            protected void onContinuationMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                                receivedContinuationPayload[0] = new String(payload, StandardCharsets.UTF_8);
                                receivedHeadersServer[0] = headers;
                                List<Header> responseHeaders = new ArrayList<>();
                                responseHeaders.add(serverStrHeader);
                                responseHeaders.add(serverIntHeader);

                                continuation.sendMessage(responseHeaders, responsePayload,
                                        MessageType.ApplicationError,
                                        MessageFlags.TerminateStream.getByteValue())
                                        .whenComplete((res, ex) ->  {
                                            connection.closeConnection(0);
                                            this.close();
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

        final boolean[] clientConnected = {false};
        final boolean[] clientConnectionClosed = {false};
        final ClientConnection[] clientConnectionArray = {null};
        final List<Header>[] clientReceivedMessageHeaders = new List[]{null};
        final byte[][] clientReceivedPayload = {null};
        final MessageType[] clientReceivedMessageType = {null};
        final int[] clientReceivedMessageFlags = {-1};
        final boolean[] clientContinuationClosed = {false};


        CompletableFuture<Void> connectFuture = ClientConnection.connect("127.0.0.1", (short)8034, socketOptions, null, clientBootstrap, new ClientConnectionHandler() {
            @Override
            protected void onConnectionSetup(ClientConnection connection, int errorCode) {
                clientConnected[0] = true;
                clientConnectionArray[0] = connection;
            }

            @Override
            protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
            }

            @Override
            protected void onConnectionClosed(int closeReason) {
                clientConnectionClosed[0] = true;
                this.close();
            }
        });

        final byte[] connectPayload = "test connect payload".getBytes(StandardCharsets.UTF_8);
        connectFuture.get();
        assertNotNull(clientConnectionArray[0]);
        clientConnectionArray[0].sendProtocolMessage(null, connectPayload, MessageType.Connect, 0).get();
        // this should be damn near instant, but give it a second just to be safe.
        Thread.sleep(1000);
        String operationName = "testOperation";

        ClientConnectionContinuation continuation = clientConnectionArray[0].newStream(new ClientConnectionContinuationHandler() {
            @Override
            protected void onContinuationMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                clientReceivedMessageHeaders[0] = headers;
                clientReceivedMessageType[0] = messageType;
                clientReceivedMessageFlags[0] = messageFlags;
                clientReceivedPayload[0] = payload;
            }

            @Override
            protected void onContinuationClosed() {
                clientContinuationClosed[0] = true;
                super.onContinuationClosed();
            }
        });
        assertNotNull(continuation);

        final byte[] operationPayload = "{\"message\": \"message payload\"}".getBytes(StandardCharsets.UTF_8);
        Header clientStrHeader = Header.createHeader("clientStrHeaderName", "clientStrHeaderValue");
        Header clientIntHeader = Header.createHeader("clientIntHeaderName", 35);
        List<Header> clientHeaders = new ArrayList<>();
        clientHeaders.add(clientStrHeader);
        clientHeaders.add(clientIntHeader);
        continuation.activate(operationName, clientHeaders, operationPayload, MessageType.ApplicationMessage, 0).get();
        Thread.sleep(1000);

        assertArrayEquals(responsePayload, clientReceivedPayload[0]);
        assertEquals(MessageType.ApplicationError, clientReceivedMessageType[0]);
        assertEquals(MessageFlags.TerminateStream.getByteValue(), clientReceivedMessageFlags[0]);
        assertNotNull(receivedHeadersServer[0]);
        assertEquals(clientStrHeader.getName(), receivedHeadersServer[0].get(0).getName());
        assertEquals(clientStrHeader.getValueAsString(), receivedHeadersServer[0].get(0).getValueAsString());
        assertEquals(clientIntHeader.getName(), receivedHeadersServer[0].get(1).getName());
        assertEquals(clientIntHeader.getValueAsInt(), receivedHeadersServer[0].get(1).getValueAsInt());
        assertEquals(serverStrHeader.getName(), clientReceivedMessageHeaders[0].get(0).getName());
        assertEquals(serverStrHeader.getValueAsString(), clientReceivedMessageHeaders[0].get(0).getValueAsString());
        assertEquals(serverIntHeader.getName(), clientReceivedMessageHeaders[0].get(1).getName());
        assertEquals(serverIntHeader.getValueAsInt(), clientReceivedMessageHeaders[0].get(1).getValueAsInt());
        assertTrue(continuationClosed[0]);
        assertTrue(clientContinuationClosed[0]);

        // also, should fire within a millisecond, but just give it a second.
        Thread.sleep(1000);

        assertTrue(connectionReceived[0]);
        assertTrue(connectionShutdown[0]);
        assertNotNull(receivedOperationName[0]);
        assertEquals(operationName, receivedOperationName[0]);
        assertEquals(new String(operationPayload, StandardCharsets.UTF_8), receivedContinuationPayload[0]);
        listener.close();
        listener.getShutdownCompleteFuture().get();
        bootstrap.close();
        clientBootstrap.close();
        elGroup.close();
        socketOptions.close();
    }
}
