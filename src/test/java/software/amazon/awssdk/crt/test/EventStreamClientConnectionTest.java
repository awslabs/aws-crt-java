package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.CleanableCrtResource;
import software.amazon.awssdk.crt.eventstream.*;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.ServerBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.*;

public class EventStreamClientConnectionTest extends CrtTestFixture {
    public EventStreamClientConnectionTest() {}

    @Test
    public void testConnectionHandling() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        HostResolver hr = new HostResolver(elGroup);
        ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, hr);
        final boolean[] connectionReceived = {false};
        final boolean[] connectionShutdown = {false};
        final ServerConnection[] serverConnections = {null};
        final CompletableFuture<ServerConnection> serverConnectionAccepted = new CompletableFuture<>();

        ServerListener listener = new ServerListener("127.0.0.1", (short)8033, socketOptions, null, bootstrap, new ServerListenerHandler() {
            private ServerConnectionHandler connectionHandler = null;

            public ServerConnectionHandler onNewConnection(ServerConnection serverConnection, int errorCode) {
                serverConnections[0] = serverConnection;
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

                serverConnectionAccepted.complete(serverConnection);
                return connectionHandler;
            }

            public void onConnectionShutdown(ServerConnection serverConnection, int errorCode) {
                connectionShutdown[0] = true;
            }
        });

        final boolean[] clientConnected = {false};
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
        });

        connectFuture.get();
        assertNotNull(clientConnectionArray[0]);
        serverConnectionAccepted.get();
        assertNotNull(serverConnections[0]);
        clientConnectionArray[0].closeConnection(0);
        clientConnectionArray[0].getClosedFuture().get();
        serverConnections[0].getClosedFuture().get();
        assertTrue(connectionReceived[0]);
        assertTrue(connectionShutdown[0]);
        assertTrue(clientConnected[0]);
        listener.close();
        bootstrap.close();
        hr.close();
        clientBootstrap.close();
        elGroup.close();
        socketOptions.close();

        CleanableCrtResource.debugWaitForNoResources();
    }

    @Test
    public void testConnectionProtocolMessageHandling() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, null);
        final List<Header>[] receivedMessageHeaders = new List[]{null};
        final byte[][] receivedPayload = {null};
        final MessageType[] receivedMessageType = {null};
        final int[] receivedMessageFlags = {-1};
        final ServerConnection[] serverConnectionArray = {null};
        final boolean[] serverConnShutdown = {false};

        final byte[] responseMessage = "{ \"message\": \"connect ack\" }".getBytes(StandardCharsets.UTF_8);
        final CompletableFuture<ServerConnection> serverConnectionAccepted = new CompletableFuture<>();
        final CompletableFuture<ServerConnection> serverMessageSent = new CompletableFuture<>();

        ServerListener listener = new ServerListener("127.0.0.1", (short)8034, socketOptions, null, bootstrap, new ServerListenerHandler() {
            private ServerConnectionHandler connectionHandler = null;

            public ServerConnectionHandler onNewConnection(ServerConnection serverConnection, int errorCode) {
                serverConnectionArray[0] = serverConnection;

                connectionHandler = new ServerConnectionHandler(serverConnection) {

                    @Override
                    protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                        receivedMessageHeaders[0] = headers;
                        receivedPayload[0] = payload;
                        receivedMessageType[0] = messageType;
                        receivedMessageFlags[0] = messageFlags;
                        serverConnection.sendProtocolMessage(null, responseMessage, MessageType.ConnectAck, MessageFlags.ConnectionAccepted.getByteValue())
                            .whenComplete((res, ex) -> {
                                serverMessageSent.complete(serverConnection);
                            });
                    }

                    @Override
                    protected ServerConnectionContinuationHandler onIncomingStream(ServerConnectionContinuation continuation, String operationName) {
                        return null;
                    }
                };
                serverConnectionAccepted.complete(serverConnection);
                return connectionHandler;
            }

            @Override
            protected void onConnectionShutdown(ServerConnection serverConnection, int errorCode) {
                serverConnShutdown[0] = true;
            }
        });

        final ClientConnection[] clientConnectionArray = {null};
        final List<Header>[] clientReceivedMessageHeaders = new List[]{null};
        final byte[][] clientReceivedPayload = {null};
        final MessageType[] clientReceivedMessageType = {null};
        final int[] clientReceivedMessageFlags = {-1};
        final CompletableFuture<Void> clientMessageReceived = new CompletableFuture<>();

        CompletableFuture<Void> connectFuture = ClientConnection.connect("127.0.0.1", (short)8034, socketOptions, null, clientBootstrap, new ClientConnectionHandler() {
            @Override
            protected void onConnectionSetup(ClientConnection connection, int errorCode) {
                clientConnectionArray[0] = connection;
            }

            @Override
            protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                clientReceivedMessageHeaders[0] = headers;
                clientReceivedPayload[0] = payload;
                clientReceivedMessageType[0] = messageType;
                clientReceivedMessageFlags[0] = messageFlags;
                clientMessageReceived.complete(null);
            }
        });

        final byte[] connectPayload = "test connect payload".getBytes(StandardCharsets.UTF_8);
        connectFuture.get(1, TimeUnit.SECONDS);
        assertNotNull(clientConnectionArray[0]);
        serverConnectionAccepted.get(1, TimeUnit.SECONDS);
        assertNotNull(serverConnectionArray[0]);

        clientConnectionArray[0].sendProtocolMessage(null, connectPayload, MessageType.Connect, 0);

        clientMessageReceived.get(1, TimeUnit.SECONDS);
        assertEquals(MessageType.Connect, receivedMessageType[0]);
        assertArrayEquals(connectPayload, receivedPayload[0]);
        assertEquals(MessageType.ConnectAck, clientReceivedMessageType[0]);
        assertEquals(MessageFlags.ConnectionAccepted.getByteValue(), clientReceivedMessageFlags[0]);
        assertArrayEquals(responseMessage, clientReceivedPayload[0]);
        clientConnectionArray[0].closeConnection(0);
        clientConnectionArray[0].getClosedFuture().get(1, TimeUnit.SECONDS);
        serverConnectionArray[0].getClosedFuture().get(1, TimeUnit.SECONDS);

        assertTrue(serverConnShutdown[0]);
        listener.close();
        listener.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        bootstrap.close();
        clientBootstrap.close();
        clientBootstrap.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        elGroup.close();
        elGroup.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        socketOptions.close();
    }

    @Test
    public void testConnectionProtocolMessageWithExtraHeadersHandling() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, null);
        final boolean[] connectionShutdown = {false};
        final List<Header>[] receivedMessageHeaders = new List[]{null};
        final byte[][] receivedPayload = {null};
        final MessageType[] receivedMessageType = {null};
        final int[] receivedMessageFlags = {-1};
        final ServerConnection[] serverConnections = {null};

        final byte[] responseMessage = "{ \"message\": \"connect ack\" }".getBytes(StandardCharsets.UTF_8);

        Header serverStrHeader = Header.createHeader("serverStrHeaderName", "serverStrHeaderValue");
        Header serverIntHeader = Header.createHeader("serverIntHeaderName", 25);

        Lock semaphoreLock = new ReentrantLock();
        Condition semaphore = semaphoreLock.newCondition();

        ServerListener listener = new ServerListener("127.0.0.1", (short)8035, socketOptions, null, bootstrap, new ServerListenerHandler() {
            private ServerConnectionHandler connectionHandler = null;

            public ServerConnectionHandler onNewConnection(ServerConnection serverConnection, int errorCode) {
                serverConnections[0] = serverConnection;
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

                semaphoreLock.lock();
                semaphore.signal();
                semaphoreLock.unlock();
                return connectionHandler;
            }

            public void onConnectionShutdown(ServerConnection serverConnection, int errorCode) {
                connectionShutdown[0] = true;
            }
        });

        final boolean[] clientConnected = {false};
        final ClientConnection[] clientConnectionArray = {null};
        final List<Header>[] clientReceivedMessageHeaders = new List[]{null};
        final byte[][] clientReceivedPayload = {null};
        final MessageType[] clientReceivedMessageType = {null};
        final int[] clientReceivedMessageFlags = {-1};

        CompletableFuture<Void> connectFuture = ClientConnection.connect("127.0.0.1", (short)8035, socketOptions, null, clientBootstrap, new ClientConnectionHandler() {
            @Override
            protected void onConnectionSetup(ClientConnection connection, int errorCode) {
                clientConnected[0] = true;
                clientConnectionArray[0] = connection;
            }

            @Override
            protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                semaphoreLock.lock();
                clientReceivedMessageHeaders[0] = headers;
                clientReceivedPayload[0] = payload;
                clientReceivedMessageType[0] = messageType;
                clientReceivedMessageFlags[0] = messageFlags;
                semaphore.signal();
                semaphoreLock.unlock();
            }
        });

        final byte[] connectPayload = "test connect payload".getBytes(StandardCharsets.UTF_8);
        connectFuture.get(1, TimeUnit.SECONDS);
        assertNotNull(clientConnectionArray[0]);
        semaphoreLock.lock();
        semaphore.await(1, TimeUnit.SECONDS);
        assertNotNull(serverConnections[0]);

        Header clientStrHeader = Header.createHeader("clientStrHeaderName", "clientStrHeaderValue");
        Header clientIntHeader = Header.createHeader("clientIntHeaderName", 35);
        List<Header> clientHeaders = new ArrayList<>();
        clientHeaders.add(clientStrHeader);
        clientHeaders.add(clientIntHeader);

        clientConnectionArray[0].sendProtocolMessage(clientHeaders, connectPayload, MessageType.Connect, 0);

        semaphore.await(1, TimeUnit.SECONDS);
        semaphoreLock.unlock();
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
        clientConnectionArray[0].getClosedFuture().get(1, TimeUnit.SECONDS);
        serverConnections[0].getClosedFuture().get(1, TimeUnit.SECONDS);

        assertTrue(connectionShutdown[0]);
        assertTrue(clientConnected[0]);
        listener.close();
        listener.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        bootstrap.close();
        clientBootstrap.close();
        clientBootstrap.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        elGroup.close();
        elGroup.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        socketOptions.close();
    }

    @Test
    public void testContinuationMessageHandling() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, null);

        final boolean[] connectionShutdown = {false};
        final String[] receivedOperationName = new String[]{null};
        final String[] receivedContinuationPayload = new String[]{null};

        final byte[] responsePayload = "{ \"message\": \"this is a response message\" }".getBytes(StandardCharsets.UTF_8);
        final ServerConnection[] serverConnections = {null};
        Lock semaphoreLock = new ReentrantLock();
        Condition semaphore = semaphoreLock.newCondition();

        ServerListener listener = new ServerListener("127.0.0.1", (short)8036, socketOptions, null, bootstrap, new ServerListenerHandler() {
            private ServerConnectionHandler connectionHandler = null;

            public ServerConnectionHandler onNewConnection(ServerConnection serverConnection, int errorCode) {
                serverConnections[0] = serverConnection;
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

                semaphoreLock.lock();
                semaphore.signal();
                semaphoreLock.unlock();
                return connectionHandler;
            }

            public void onConnectionShutdown(ServerConnection serverConnection, int errorCode) {
                connectionShutdown[0] = true;
            }
        });

        final ClientConnection[] clientConnectionArray = {null};
        final List<Header>[] clientReceivedMessageHeaders = new List[]{null};
        final byte[][] clientReceivedPayload = {null};
        final MessageType[] clientReceivedMessageType = {null};
        final int[] clientReceivedMessageFlags = {-1};
        final boolean[] clientContinuationClosed = {false};

        CompletableFuture<Void> connectFuture = ClientConnection.connect("127.0.0.1", (short)8036, socketOptions, null, clientBootstrap, new ClientConnectionHandler() {
            @Override
            protected void onConnectionSetup(ClientConnection connection, int errorCode) {
                clientConnectionArray[0] = connection;
            }

            @Override
            protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                semaphoreLock.lock();
                semaphore.signal();
                semaphoreLock.unlock();
            }
        });

        final byte[] connectPayload = "test connect payload".getBytes(StandardCharsets.UTF_8);
        connectFuture.get(1, TimeUnit.SECONDS);
        assertNotNull(clientConnectionArray[0]);
        semaphoreLock.lock();
        semaphore.await(1, TimeUnit.SECONDS);
        assertNotNull(serverConnections[0]);
        clientConnectionArray[0].sendProtocolMessage(null, connectPayload, MessageType.Connect, 0);
        semaphore.await(1, TimeUnit.SECONDS);
        String operationName = "testOperation";

        ClientConnectionContinuation continuation = clientConnectionArray[0].newStream(new ClientConnectionContinuationHandler() {
            @Override
            protected void onContinuationMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                semaphoreLock.lock();
                clientReceivedMessageHeaders[0] = headers;
                clientReceivedMessageType[0] = messageType;
                clientReceivedMessageFlags[0] = messageFlags;
                clientReceivedPayload[0] = payload;
                semaphoreLock.unlock();
            }

            @Override
            protected void onContinuationClosed() {
                semaphoreLock.lock();
                clientContinuationClosed[0] = true;
                semaphore.signal();
                semaphoreLock.unlock();
                super.onContinuationClosed();
            }
        });
        assertNotNull(continuation);

        final byte[] operationPayload = "{\"message\": \"message payload\"}".getBytes(StandardCharsets.UTF_8);
        continuation.activate(operationName, null, operationPayload, MessageType.ApplicationMessage, 0);
        semaphore.await(1, TimeUnit.SECONDS);

        assertArrayEquals(responsePayload, clientReceivedPayload[0]);
        assertEquals(MessageType.ApplicationError, clientReceivedMessageType[0]);
        assertEquals(MessageFlags.TerminateStream.getByteValue(), clientReceivedMessageFlags[0]);
        assertTrue(clientContinuationClosed[0]);

        clientConnectionArray[0].getClosedFuture().get(1, TimeUnit.SECONDS);
        serverConnections[0].getClosedFuture().get(1, TimeUnit.SECONDS);
        semaphoreLock.unlock();

        assertTrue(connectionShutdown[0]);
        assertNotNull(receivedOperationName[0]);
        assertEquals(operationName, receivedOperationName[0]);
        assertEquals(new String(operationPayload, StandardCharsets.UTF_8), receivedContinuationPayload[0]);
        listener.close();
        listener.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        bootstrap.close();
        clientBootstrap.close();
        clientBootstrap.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        elGroup.close();
        elGroup.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        socketOptions.close();
    }

    @Test
    public void testContinuationMessageWithExtraHeadersHandling() throws ExecutionException, InterruptedException, IOException, TimeoutException {
        SocketOptions socketOptions = new SocketOptions();
        socketOptions.connectTimeoutMs = 3000;
        socketOptions.domain = SocketOptions.SocketDomain.IPv4;
        socketOptions.type = SocketOptions.SocketType.STREAM;

        EventLoopGroup elGroup = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elGroup);
        ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, null);

        final boolean[] connectionShutdown = {false};

        final String[] receivedOperationName = new String[]{null};
        final String[] receivedContinuationPayload = new String[]{null};
        final List<Header>[] receivedHeadersServer = new List[]{null};

        Header serverStrHeader = Header.createHeader("serverStrHeaderName", "serverStrHeaderValue");
        Header serverIntHeader = Header.createHeader("serverIntHeaderName", 25);

        final byte[] responsePayload = "{ \"message\": \"this is a response message\" }".getBytes(StandardCharsets.UTF_8);
        final ServerConnection[] serverConnections = {null};
        Lock semaphoreLock = new ReentrantLock();
        Condition semaphore = semaphoreLock.newCondition();

        ServerListener listener = new ServerListener("127.0.0.1", (short)8037, socketOptions, null, bootstrap, new ServerListenerHandler() {
            private ServerConnectionHandler connectionHandler = null;

            public ServerConnectionHandler onNewConnection(ServerConnection serverConnection, int errorCode) {
                serverConnections[0] = serverConnection;
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

                semaphoreLock.lock();
                semaphore.signal();
                semaphoreLock.unlock();
                return connectionHandler;
            }

            public void onConnectionShutdown(ServerConnection serverConnection, int errorCode) {
                connectionShutdown[0] = true;
            }
        });

        final ClientConnection[] clientConnectionArray = {null};
        final List<Header>[] clientReceivedMessageHeaders = new List[]{null};
        final byte[][] clientReceivedPayload = {null};
        final MessageType[] clientReceivedMessageType = {null};
        final int[] clientReceivedMessageFlags = {-1};
        final boolean[] clientContinuationClosed = {false};

        CompletableFuture<Void> connectFuture = ClientConnection.connect("127.0.0.1", (short)8037, socketOptions, null, clientBootstrap, new ClientConnectionHandler() {
            @Override
            protected void onConnectionSetup(ClientConnection connection, int errorCode) {
                clientConnectionArray[0] = connection;
            }

            @Override
            protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                semaphoreLock.lock();
                semaphore.signal();
                semaphoreLock.unlock();
            }
        });

        final byte[] connectPayload = "test connect payload".getBytes(StandardCharsets.UTF_8);
        connectFuture.get(1, TimeUnit.SECONDS);
        assertNotNull(clientConnectionArray[0]);
        semaphoreLock.lock();
        semaphore.await(1, TimeUnit.SECONDS);
        assertNotNull(serverConnections[0]);
        clientConnectionArray[0].sendProtocolMessage(null, connectPayload, MessageType.Connect, 0);
        semaphore.await(1, TimeUnit.SECONDS);
        String operationName = "testOperation";

        ClientConnectionContinuation continuation = clientConnectionArray[0].newStream(new ClientConnectionContinuationHandler() {
            @Override
            protected void onContinuationMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                semaphoreLock.lock();
                clientReceivedMessageHeaders[0] = headers;
                clientReceivedMessageType[0] = messageType;
                clientReceivedMessageFlags[0] = messageFlags;
                clientReceivedPayload[0] = payload;
                semaphoreLock.unlock();
            }

            @Override
            protected void onContinuationClosed() {
                semaphoreLock.lock();
                clientContinuationClosed[0] = true;
                semaphore.signal();
                semaphoreLock.unlock();
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
        continuation.activate(operationName, clientHeaders, operationPayload, MessageType.ApplicationMessage, 0).get(1, TimeUnit.SECONDS);
        semaphore.await(1, TimeUnit.SECONDS);

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
        assertTrue(clientContinuationClosed[0]);

        clientConnectionArray[0].getClosedFuture().get(1, TimeUnit.SECONDS);
        serverConnections[0].getClosedFuture().get(1, TimeUnit.SECONDS);
        semaphoreLock.unlock();
        assertTrue(connectionShutdown[0]);
        assertNotNull(receivedOperationName[0]);
        assertEquals(operationName, receivedOperationName[0]);
        assertEquals(new String(operationPayload, StandardCharsets.UTF_8), receivedContinuationPayload[0]);
        listener.close();
        listener.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        bootstrap.close();
        clientBootstrap.close();
        clientBootstrap.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        elGroup.close();
        elGroup.getShutdownCompleteFuture().get(1, TimeUnit.SECONDS);
        socketOptions.close();
    }
}
