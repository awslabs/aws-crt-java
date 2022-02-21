package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.RuntimeErrorException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpStreamManager;
import software.amazon.awssdk.crt.http.Http2StreamManager;
import software.amazon.awssdk.crt.http.Http2Request;
import software.amazon.awssdk.crt.http.Http2Stream;
import software.amazon.awssdk.crt.http.HttpStreamManagerOptions;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.http.HttpStreamResponseHandler;
import software.amazon.awssdk.crt.http.HttpVersion;
import software.amazon.awssdk.crt.http.HttpStream;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.utils.ByteBufferUtils;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;

public class HttpStreamManagerTest extends HttpClientTestFixture {
    private final static String endpoint = "https://httpbin.org";
    private final static String path = "/anything";
    private final String EMPTY_BODY = "";
    private final static int NUM_CONNECTIONS = 20;
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private final static int EXPECTED_HTTP_STATUS = 200;

    private HttpStreamManager createStreamManager(URI uri, int numConnections, HttpVersion expectedVersion) {

        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                SocketOptions sockOpts = new SocketOptions();
                TlsContextOptions tlsOpts = expectedVersion == HttpVersion.HTTP_2
                        ? TlsContextOptions.createDefaultClient().withAlpnList("h2")
                        : TlsContextOptions.createDefaultClient().withAlpnList("http/1.1");
                TlsContext tlsContext = createHttpClientTlsContext(tlsOpts)) {
            HttpStreamManagerOptions options = new HttpStreamManagerOptions();
            options.withClientBootstrap(bootstrap)
                    .withSocketOptions(sockOpts)
                    .withTlsContext(tlsContext)
                    .withUri(uri)
                    .withMaxConnections(numConnections);

            return HttpStreamManager.create(options);
        }
    }

    private Http2Request createHttp2Request(String method, String endpoint, String path, String requestBody)
            throws Exception {
        URI uri = new URI(endpoint);
        HttpHeader[] requestHeaders = new HttpHeader[] {
                new HttpHeader(":method", method),
                new HttpHeader(":path", path),
                new HttpHeader(":scheme", uri.getScheme()),
                new HttpHeader(":authority", uri.getHost()),
                new HttpHeader("content-length", Integer.toString(requestBody.getBytes(UTF8).length))
        };
        final ByteBuffer payload = ByteBuffer.wrap(requestBody.getBytes());
        HttpRequestBodyStream payloadStream = new HttpRequestBodyStream() {
            @Override
            public boolean sendRequestBody(ByteBuffer outBuffer) {
                ByteBufferUtils.transferData(payload, outBuffer);
                return payload.remaining() == 0;
            }

            @Override
            public boolean resetPosition() {
                return true;
            }

            @Override
            public long getLength() {
                return payload.capacity();
            }
        };
        Http2Request request = new Http2Request(requestHeaders, payloadStream);

        return request;
    }

    private HttpRequest createHttp1Request(String method, String endpoint, String path, String requestBody)
            throws Exception {
        URI uri = new URI(endpoint);
        HttpHeader[] requestHeaders = new HttpHeader[] {
                new HttpHeader("host", uri.getHost()),
                new HttpHeader("content-length", Integer.toString(requestBody.getBytes(UTF8).length))
        };
        final ByteBuffer payload = ByteBuffer.wrap(requestBody.getBytes());
        HttpRequestBodyStream payloadStream = new HttpRequestBodyStream() {
            @Override
            public boolean sendRequestBody(ByteBuffer outBuffer) {
                ByteBufferUtils.transferData(payload, outBuffer);
                return payload.remaining() == 0;
            }

            @Override
            public boolean resetPosition() {
                return true;
            }

            @Override
            public long getLength() {
                return payload.capacity();
            }
        };
        return new HttpRequest(method, path, requestHeaders, payloadStream);
    }

    @Test
    public void testSanitizerHTTP1() throws Exception {
        URI uri = new URI(endpoint);
        try (HttpStreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, HttpVersion.HTTP_1_1)) {
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    @Test
    public void testSanitizerHTTP2() throws Exception {
        URI uri = new URI(endpoint);
        try (HttpStreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, HttpVersion.HTTP_2)) {
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    @Test
    public void testSingleHTTP2Requests() throws Exception {
        URI uri = new URI(endpoint);
        try (HttpStreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, HttpVersion.HTTP_2)) {
            Http2Request request = createHttp2Request("GET", endpoint, path, EMPTY_BODY);

            streamManager.acquireStream(request, new HttpStreamResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    Assert.assertEquals(responseStatusCode, EXPECTED_HTTP_STATUS);
                }

                @Override
                public void onResponseComplete(HttpStream stream, int errorCode) {
                    Assert.assertEquals(errorCode, CRT.AWS_CRT_SUCCESS);
                    stream.close();
                }
            }).get();
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    @Test
    public void testSingleHTTP1Request() throws Throwable {
        Log.initLoggingToStderr(LogLevel.Trace);
        URI uri = new URI(endpoint);
        try (HttpStreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, HttpVersion.HTTP_1_1)) {
            /*
             * http2 request which will have :method headers that is not allowed for
             * HTTP/1.1
             */
            HttpRequest request = createHttp1Request("GET", endpoint, path, EMPTY_BODY);

            streamManager.acquireStream(request, new HttpStreamResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    Assert.assertEquals(responseStatusCode, EXPECTED_HTTP_STATUS);
                }

                @Override
                public void onResponseComplete(HttpStream stream, int errorCode) {
                    Assert.assertEquals(errorCode, CRT.AWS_CRT_SUCCESS);
                    stream.close();
                }
            }).get();
        } catch (ExecutionException e) {
            Assert.assertNull(e);
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    /*
     * Create HTTP/1.1 stream manager, with HTTP/2 request, which should fail with
     * invalid header name. Make sure the exception pops out and everything clean up
     * correctly.
     */
    @Test
    public void testSingleHTTP1RequestsFailure() throws Throwable {
        URI uri = new URI(endpoint);
        try (HttpStreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, HttpVersion.HTTP_1_1)) {
            /*
             * http2 request which will have :method headers that is not allowed for
             * HTTP/1.1
             */
            Http2Request request = createHttp2Request("GET", endpoint, path, EMPTY_BODY);

            streamManager.acquireStream(request, new HttpStreamResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    Assert.assertEquals(responseStatusCode, EXPECTED_HTTP_STATUS);
                }

                @Override
                public void onResponseComplete(HttpStream stream, int errorCode) {
                    Assert.assertEquals(errorCode, CRT.AWS_CRT_SUCCESS);
                    stream.close();
                }
            }).get();
        } catch (ExecutionException e) {
            try {
                throw e.getCause();
            } catch (CrtRuntimeException causeException) {
                /**
                 * Assert the exceptions are set correctly.
                 */
                Assert.assertTrue(causeException.errorName.equals("AWS_ERROR_HTTP_INVALID_HEADER_NAME"));
            }
        }

        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }
}
