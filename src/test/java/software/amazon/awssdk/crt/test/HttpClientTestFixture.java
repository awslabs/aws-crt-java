/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.http.HttpVersion;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;

import java.net.URI;

public class HttpClientTestFixture extends CrtTestFixture {

    private HttpClientConnectionManager createHTTP2ConnectionPoolManager(URI uri) {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                SocketOptions sockOpts = new SocketOptions();
                TlsContextOptions tlsContextOptions = TlsContextOptions.createDefaultClient()
                        .withAlpnList("h2;http/1.1");
                TlsContext tlsContext = createHttpClientTlsContext(tlsContextOptions)) {

            HttpClientConnectionManagerOptions options = new HttpClientConnectionManagerOptions()
                    .withClientBootstrap(bootstrap).withSocketOptions(sockOpts).withTlsContext(tlsContext).withUri(uri);

            return HttpClientConnectionManager.create(options);
        }
    }

    private HttpClientConnectionManager createHTTP1ConnectionPoolManager(URI uri) {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                SocketOptions sockOpts = new SocketOptions();
                TlsContext tlsContext = createHttpClientTlsContext()) {

            HttpClientConnectionManagerOptions options = new HttpClientConnectionManagerOptions()
                    .withClientBootstrap(bootstrap).withSocketOptions(sockOpts).withTlsContext(tlsContext).withUri(uri);

            return HttpClientConnectionManager.create(options);
        }
    }

    public TlsContext createHttpClientTlsContext() {
        return createTlsContextOptions(getContext().trustStore);
    }

    public TlsContext createHttpClientTlsContext(TlsContextOptions tlsOpts) {
        return new TlsContext(configureTlsContextOptions(tlsOpts, getContext().trustStore));
    }

    public HttpClientConnectionManager createConnectionPoolManager(URI uri,
            HttpVersion expectedVersion) {
        if (expectedVersion == HttpVersion.HTTP_2) {
            return createHTTP2ConnectionPoolManager(uri);
        } else {
            return createHTTP1ConnectionPoolManager(uri);
        }

    }
}
