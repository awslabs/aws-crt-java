package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;

public class ShutdownTest {

    private static String SHUTDOWN_TEST_ENABLED = System.getenv("AWS_CRT_SHUTDOWN_TESTING");

    private static boolean doShutdownTest() {
        return SHUTDOWN_TEST_ENABLED != null;
    }

    private HttpClientConnectionManager createConnectionManager(URI uri) {
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
             HostResolver resolver = new HostResolver(eventLoopGroup);
             ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
             SocketOptions sockOpts = new SocketOptions();
             TlsContextOptions tlsOpts = TlsContextOptions.createDefaultClient();
             TlsContext tlsContext = new TlsContext(tlsOpts)) {

            HttpClientConnectionManagerOptions options = new HttpClientConnectionManagerOptions();
            options.withClientBootstrap(bootstrap)
                    .withSocketOptions(sockOpts)
                    .withTlsContext(tlsContext)
                    .withUri(uri)
                    .withMaxConnections(1);

            return HttpClientConnectionManager.create(options);
        }
    }

    @Test
    public void testShutdownDuringAcquire() throws Exception {
        Assume.assumeTrue(doShutdownTest());

        HttpClientConnectionManager manager = createConnectionManager(new URI("https://aws-crt-test-stuff.s3.amazonaws.com"));
        CompletableFuture<HttpClientConnection> connection = manager.acquireConnection();
    }
}
