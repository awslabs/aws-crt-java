/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogSubject;
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
        // We only want to see the CRT logs if the test fails.
        // Surefire has a redirectTestOutputToFile option, but that doesn't
        // capture what the CRT logger writes to stdout or stderr.
        // Our workaround is to have the CRT logger write to log.txt.
        // We clear the file for each new test by restarting the logger.
        // We stop all tests when one fails (see FailFastListener) so that
        // a valuable log.txt isn't overwritten.
        if (System.getProperty("aws.crt.aws_trace_log_per_test") != null) {
            Log.initLoggingToFile(Log.LogLevel.Trace, "log.txt");
        }
        System.out.println("Android TEST: CrtTestFixture.setup() Started\n");
        Log.log(Log.LogLevel.Debug, LogSubject.JavaCrtGeneral, "CrtTestFixture setup begin");
        context = new CrtTestContext();
        CrtPlatform platform = CRT.getPlatformImpl();
        if (platform != null) {
            System.out.println("Android TEST: CrtTestFixture.setup() platform != null");
            platform.testSetup(context);
        } else {
            System.out.println("Android TEST: CrtTestFixture.setup() platform == null, testSetup not called");
        }
        Log.log(Log.LogLevel.Debug, LogSubject.JavaCrtGeneral, "CrtTestFixture setup end");
        System.out.println("Android TEST: CrtTestFixture.setup() complete\n");
    }

    @After
    public void tearDown() {
        System.out.println("Android TEST: CrtTestFixture.teardown() started");
        Log.log(Log.LogLevel.Debug, LogSubject.JavaCrtGeneral, "CrtTestFixture tearDown begin");
        CrtPlatform platform = CRT.getPlatformImpl();
        if (platform != null) {
            System.out.println("Android TEST: CrtTestFixture.tearDown() platform != null");
            platform.testTearDown(context);
        } else {
            System.out.println("Android TEST: CrtTestFixture.tearDown() platform == null, testTearDown not called");
        }

        context = null;

        EventLoopGroup.closeStaticDefault();
        HostResolver.closeStaticDefault();
        ClientBootstrap.closeStaticDefault();

        CrtResource.waitForNoResources();
        System.out.println("Android TEST: CrtTestFixture.teardown() calling CRT.getOSIdentifier next");
        if (CRT.getOSIdentifier() != "android") {
            try {
                Runtime.getRuntime().gc();
                CrtMemoryLeakDetector.nativeMemoryLeakCheck();
            } catch (Exception e) {
                throw new RuntimeException("Memory leak from native resource detected!");
            }
        }
        Log.log(Log.LogLevel.Debug, LogSubject.JavaCrtGeneral, "CrtTestFixture tearDown end");
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

    protected void skipIfLocalhostUnavailable() {
        Assume.assumeTrue(System.getProperty("aws.crt.localhost") != null);
    }
}
