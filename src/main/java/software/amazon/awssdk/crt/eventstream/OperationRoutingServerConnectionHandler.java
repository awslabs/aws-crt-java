package software.amazon.awssdk.crt.eventstream;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * ServerConnectionHandler implementation that routes continuations to specific
 * operation specific message handlers
 */
public class OperationRoutingServerConnectionHandler extends ServerConnectionHandler {
    private final Map<String, Function<ServerConnectionContinuation, ServerConnectionContinuationHandler>> operationMap;

    /**
     * binds an operation handler mapping to a server connection
     * @param serverConnection connection to route messages for
     * @param operationMapping mapping of operation names to message handlers.
     */
    public OperationRoutingServerConnectionHandler(final ServerConnection serverConnection,
                                                   final Map<String, Function<ServerConnectionContinuation, ServerConnectionContinuationHandler>> operationMapping) {
        super(serverConnection);
        this.operationMap = operationMapping;
    }

    /**
     * By default, automatically responds to pings when received, and routes connect requests.
     *
     * Feel free to override this function if you want different behavior.
     */
    @Override
    protected void onProtocolMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
        if (messageType == MessageType.Ping) {
            int responseMessageFlag = 0;
            MessageType responseMessageType = MessageType.PingResponse;

            connection.sendProtocolMessage(null, null, responseMessageType, responseMessageFlag);
        } else if (messageType == MessageType.Connect) {
            onConnectRequest(headers, payload);
        } else if (messageType != MessageType.PingResponse){
            int responseMessageFlag = 0;
            MessageType responseMessageType = MessageType.ServerError;

            String responsePayload =
                    "{ \"error\": \"Unrecognized Message Type\" }" +
                    "\"message\": \" message type value: " + messageType.getEnumValue() + " is not recognized as a valid request path.\" }";

            Header contentTypeHeader = Header.createHeader(":content-type", "application/json");
            List<Header> responseHeaders = new ArrayList<>();
            responseHeaders.add(contentTypeHeader);
            CompletableFuture<Void> voidCompletableFuture = connection.sendProtocolMessage(responseHeaders, responsePayload.getBytes(StandardCharsets.UTF_8), responseMessageType, responseMessageFlag);
            voidCompletableFuture.thenAccept(result -> {connection.closeConnection(0); this.close();});
        }
    }

    /**
     * To customize how the connect request is handled, override this function.
     * You're responsible for sending the response. By default, it accepts all incoming
     * connections.
     */
    protected void onConnectRequest(List<Header> headers, byte[] payload) {
        int responseMessageFlag = MessageFlags.ConnectionAccepted.getByteValue();
        MessageType acceptResponseType = MessageType.ConnectAck;

        connection.sendProtocolMessage(null, null, acceptResponseType, responseMessageFlag);
    }

    /**
     * When a new stream continuation is received, it routes to the configured operation name ->
     * handler mapping. If no such mapping exists, an Unsupported Operation message of type ApplicationError
     * is sent to the peer.
     *
     * You can't override this function, because well, if you do, you might as well not use this
     * class in the first place.
     */
    @Override
    protected final ServerConnectionContinuationHandler onIncomingStream(ServerConnectionContinuation continuation, String operationName) {
        Function<ServerConnectionContinuation, ServerConnectionContinuationHandler> registeredOperationHandlerFn = operationMap.get(operationName);

        if (registeredOperationHandlerFn != null) {
            return registeredOperationHandlerFn.apply(continuation);
        } else {
            return new ServerConnectionContinuationHandler(continuation) {
                @Override
                protected void onContinuationClosed() {
                    close();
                }

                @Override
                protected void onContinuationMessage(List<Header> headers, byte[] payload, MessageType messageType, int messageFlags) {
                    int responseMessageFlag = MessageFlags.TerminateStream.getByteValue();
                    MessageType responseMessageType = MessageType.ApplicationError;

                    String responsePayload =
                            "{ \"error\": \"Unsupported Operation\", " +
                              "\"message\": \"" + operationName + " is an unsupported operation.\" }";

                    Header contentTypeHeader = Header.createHeader(":content-type", "application/json");
                    List<Header> responseHeaders = new ArrayList<>();
                    responseHeaders.add(contentTypeHeader);

                    continuation.sendMessage(responseHeaders, responsePayload.getBytes(StandardCharsets.UTF_8), responseMessageType, responseMessageFlag);
                }
            };
        }
    }
}
