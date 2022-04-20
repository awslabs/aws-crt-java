/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.CleanableCrtResource;
import software.amazon.awssdk.crt.io.ServerBootstrap;
import software.amazon.awssdk.crt.io.ServerTlsContext;
import software.amazon.awssdk.crt.io.SocketOptions;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * Event-stream-rpc server listener. Once it begins listening, it will provide
 * new connections as they arrive.
 */
public class ServerListener extends CleanableCrtResource {
    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    /**
     * Instantiates a server listener. Once this function completes, the server is configured
     * and listening for new connections.
     * @param hostName name of the host to listen on. Can be a dns name, ip address, or unix
     *                 domain socket (or named pipe on windows) name.
     * @param port port to listen on. Ignored for local domain sockets.
     * @param socketOptions socket options to apply to the listening socket.
     * @param tlsContext optional tls context to apply to the connection if you want to use TLS.
     * @param serverBootstrap bootstrap object for handling connections.
     * @param handler functor interface for handling incoming connections and connection closures.
     */
    public ServerListener(final String hostName, short port, final SocketOptions socketOptions,
                          final ServerTlsContext tlsContext, final ServerBootstrap serverBootstrap,
                          final ServerListenerHandler handler) {

        long tlsContextPtr = tlsContext != null ? tlsContext.getNativeHandle(): 0;
        acquireNativeHandle(serverListenerNew(shutdownComplete, hostName.getBytes(StandardCharsets.UTF_8), port,
                socketOptions.getNativeHandle(), tlsContextPtr, serverBootstrap.getNativeHandle(),
                handler), ServerListener::release);

    }

    /**
     * @return future to synchronize shutdown completion of this object.
     */
    public CompletableFuture<Void> getShutdownCompleteFuture() { return shutdownComplete; }

    private static native long serverListenerNew(CompletableFuture<Void> shutdownCallback, byte[] hostName,
                                                 short port, long socketOptionsHandle,
                                                 long tlsContextHandle, long bootstrapHandle,
                                                 ServerListenerHandler handler);
    private static native void release(long serverListenerPtr);

}
