/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.CrtPlatform;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;
import java.io.File;

import org.junit.Before;
import org.junit.After;
import org.junit.Assume;

import java.util.Optional;

public class CrtTestFixture {

    private CrtTestContext context;

    public final CrtTestContext getContext() {
        return context;
    }

    @Before
    public void setup() {
        if (System.getProperty("aws.crt.aws_trace_log_per_test") != null) {
            File logsFile = new File("log.txt");
            logsFile.delete();
            Log.initLoggingToFile(Log.LogLevel.Trace, "log.txt");
        }
        context = new CrtTestContext();
        CrtPlatform platform = CRT.getPlatformImpl();
        if (platform != null) {
            platform.testSetup(context);
        }
    }

    @After
    public void tearDown() {
        CrtPlatform platform = CRT.getPlatformImpl();
        if (platform != null) {
            platform.testTearDown(context);
        }

        context = null;

        EventLoopGroup.closeStaticDefault();
        HostResolver.closeStaticDefault();

        CrtResource.waitForNoResources();
        if (CRT.getOSIdentifier() != "android") {
            try {
                Runtime.getRuntime().gc();
                CrtMemoryLeakDetector.nativeMemoryLeakCheck();
            } catch (Exception e) {
                throw new RuntimeException("Memory leak from native resource detected!");
            }
        }
    }

    protected TlsContext createTlsContextOptions(byte[] trustStore) {
        try (TlsContextOptions tlsOpts = configureTlsContextOptions(TlsContextOptions.createDefaultClient(),
                trustStore)) {
            return new TlsContext(tlsOpts);
        }
    }

    protected TlsContextOptions configureTlsContextOptions(TlsContextOptions tlsOpts, byte[] trustStore) {
        if (trustStore != null) {
            tlsOpts.withCertificateAuthority(new String(trustStore));
        }
        return tlsOpts;
    }

    private Optional<Credentials> credentials = null;

    protected boolean hasAwsCredentials() {
        if (credentials == null) {
            try {
                try (EventLoopGroup elg = new EventLoopGroup(1);
                        HostResolver hostResolver = new HostResolver(elg);
                        ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver)) {

                    try (DefaultChainCredentialsProvider provider = ((new DefaultChainCredentialsProviderBuilder())
                            .withClientBootstrap(clientBootstrap)).build()) {
                        credentials = Optional.of(provider.getCredentials().get());
                    }
                }
            } catch (Exception ex) {
                credentials = Optional.empty();
            }
        }
        return credentials.isPresent();
    }

    protected void skipIfNetworkUnavailable() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
    }
}
