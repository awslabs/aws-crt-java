/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.CRT;
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
import java.util.concurrent.Callable;

public class HttpClientTestFixture extends CrtTestFixture {
    private final static int NUM_ITERATIONS = 10;
    private final static int GROWTH_PER_THREAD = 0; // expected VM footprint growth per thread

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

    public void testParallelRequests(int numThreads, int numRequests) throws Exception {
        throw new UnsupportedOperationException("Only supported from override");
    }


    protected void testParallelRequestsWithLeakCheck(int numThreads, int numRequests) throws Exception {
        skipIfNetworkUnavailable();
        Callable<Void> fn = () -> {
            testParallelRequests(numThreads, numRequests);
            return null;
        };

        // For Android: Dalvik is SUPER STOCHASTIC about when it frees JVM memory, it has no
        // observable correlation to when System.gc() is called. Therefore, we cannot reliably
        // sample it, so we don't bother.
        // For GraalVM: It's not using JVM, there is no reason to check the JVM memory.
        // If we have a leak, we should have it on all platforms, and we'll catch it
        // elsewhere.
        if (CRT.getOSIdentifier() != "android" && System.getProperty("org.graalvm.nativeimage.imagecode") == null) {
            int fixedGrowth = CrtMemoryLeakDetector.expectedFixedGrowth();
            fixedGrowth += (numThreads * GROWTH_PER_THREAD);
            // On Mac, JVM seems to expand by about 4K no matter how careful we are. With
            // the workload
            // we're running, 8K worth of growth (an additional 4K for an increased healthy
            // margin)
            // in the JVM only is acceptable.
            fixedGrowth = Math.max(fixedGrowth, 8192);
            CrtMemoryLeakDetector.leakCheck(NUM_ITERATIONS, fixedGrowth, fn);
        }
    }

}
