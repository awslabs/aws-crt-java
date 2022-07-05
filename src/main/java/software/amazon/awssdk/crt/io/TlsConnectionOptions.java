/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.io;

import java.util.List;
import java.util.ArrayList;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.utils.StringUtils;

/**
 * Connection-specific TLS options.
 */
public class TlsConnectionOptions extends CrtResource {
    private List<String> alpnList = new ArrayList<>();
    private String serverName = null;
    private int timeoutMs = 0;
    private TlsContext tlsContext;

    /**
     * Initialize the connection-specific TLS options with TLSContext.
     * @param tlsContext the TLS configuration contexts in the AWS Common Runtime.
     */
    public TlsConnectionOptions(TlsContext tlsContext) {
        this.tlsContext = tlsContext;
    }

    /**
     * Note: Once this gets invoked the native resource will not be able to changed.
     */
    @Override
    public long getNativeHandle() {
        if (super.getNativeHandle() == 0) {
            acquireNativeHandle(tlsConnectionOptionsNew(
                    alpnList.size() > 0 ? StringUtils.join(";", alpnList) : null,
                    serverName,
                    timeoutMs,
                    tlsContext.getNativeHandle()));
        }
        return super.getNativeHandle();
    }

    /**
     * Sets alpn list in the form <protocol1;protocol2;...>. A maximum of 4
     * protocols are supported.
     * alpnList is copied. This value is already inherited from TlsContext, but the
     * TlsContext is expensive, and should be used across as many connections as
     * possible. If you want to set this per connection, set it here.
     * @param alpnList alpn list in the form <protocol1;protocol2;...>
     * @return this
     */
    public TlsConnectionOptions withAlpnList(String alpnList) {
        String[] parts = alpnList.split(";");
        for (String part : parts) {
            this.alpnList.add(part);
        }
        return this;
    }

    /**
     * Sets server name to use for the SNI extension (supported everywhere), as well
     * as x.509 validation. If you don't set this, your x.509 validation will likely
     * fail.
     * @param serverName The server name to use for the SNI extension
     * @return this
     */
    public TlsConnectionOptions withServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }
    /**
     * Set the TLS negotiation timeout
     * @param timeoutMs The time out in ms
     * @return this
     */
    public TlsConnectionOptions withTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
        return this;
    }

    /**
     * Determines whether a resource releases its dependencies at the same time the
     * native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() {
        return true;
    }

    /**
     * Cleans up the client bootstrap's associated native handle
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            tlsConnectionOptionsDestroy(getNativeHandle());
        }
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long tlsConnectionOptionsNew(
            String alpn, String serverName, int connectTimeoutMs, long tlsContext);

    private static native void tlsConnectionOptionsDestroy(long tlsOptions);
}
