/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.Log;

/**
 * This class wraps the aws_client_bootstrap from aws-c-io to provide
 * a client context for all protocol stacks in the AWS Common Runtime.
 */
public final class ClientBootstrap extends CrtResource {

    private final CompletableFuture<Void> shutdownComplete = new CompletableFuture<>();

    /**
     * Creates a new ClientBootstrap with a default HostResolver and EventLoopGroup.
     * Most applications will only ever need one instance of this.
     */
    private ClientBootstrap() throws CrtRuntimeException {
        EventLoopGroup elg = EventLoopGroup.getOrCreateStaticDefault();
        HostResolver hr = HostResolver.getOrCreateStaticDefault();
        acquireNativeHandle(clientBootstrapNew(this, elg.getNativeHandle(), hr.getNativeHandle()));
        // Order is likely important here
        addReferenceTo(HostResolver.getOrCreateStaticDefault());
        addReferenceTo(EventLoopGroup.getOrCreateStaticDefault());
    }

    /**
     * Creates a new ClientBootstrap. Most applications will only ever need one instance of this.
     * @param hr A HostResolver instance, most applications only ever have one
     * @param elg An EventLoopGroup instance, most applications only ever have one
     * @throws CrtRuntimeException If the provided EventLoopGroup is null or invalid,
     * or if the system is unable to allocate space for a native client bootstrap object
     */
    public ClientBootstrap(EventLoopGroup elg, HostResolver hr) throws CrtRuntimeException {
        if (elg == null) {
            elg = EventLoopGroup.getOrCreateStaticDefault();
        }

        if (hr == null) {
            hr = HostResolver.getOrCreateStaticDefault();
        }

        acquireNativeHandle(clientBootstrapNew(this, elg.getNativeHandle(), hr.getNativeHandle()));

        // Order is likely important here
        addReferenceTo(hr);
        addReferenceTo(elg);
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return false; }

    /**
     * Cleans up the client bootstrap's associated native handle
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            clientBootstrapDestroy(getNativeHandle());
        }
    }

    /**
     * Called from Native when the asynchronous cleanup process needed for client bootstrap has completed.
     */
    private void onShutdownComplete() {
        Log.log(Log.LogLevel.Trace, Log.LogSubject.IoChannelBootstrap, "ClientBootstrap.onShutdownComplete");

        releaseReferences();

        this.shutdownComplete.complete(null);
    }

    public CompletableFuture<Void> getShutdownCompleteFuture() { return shutdownComplete; }

    /**
     * Closes the static ClientBootstrap, if it exists.  Primarily intended for tests that use the static
     * default ClientBootstrap, before they call waitForNoResources().
     */
    public static void closeStaticDefault() {
        synchronized (ClientBootstrap.class) {
            if (staticDefaultClientBootstrap != null) {
                staticDefaultClientBootstrap.close();
            }
            staticDefaultClientBootstrap = null;
        }
    }

    /**
     * This default will be used when a ClientBootstrap is not explicitly passed but is needed
     * to allow the process to function. An example of this would be in the MQTT connection creation workflow.
     * The default ClientBootstrap will use the default EventLoopGroup and HostResolver, creating them if
     * necessary.
     *
     * The default ClientBootstrap will be automatically managed and released when it's
     * resources are being freed, not requiring any manual memory management.
     * @return the static default ClientBootstrap
     */
    public static ClientBootstrap getOrCreateStaticDefault() {
        ClientBootstrap client = null;
        synchronized (ClientBootstrap.class) {
            if (staticDefaultClientBootstrap == null) {
                staticDefaultClientBootstrap = new ClientBootstrap();
            }

            client = staticDefaultClientBootstrap;
        }

        return client;
    }
    private static ClientBootstrap staticDefaultClientBootstrap;

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long clientBootstrapNew(ClientBootstrap bootstrap, long elg, long hr) throws CrtRuntimeException;
    private static native void clientBootstrapDestroy(long bootstrap);
};
