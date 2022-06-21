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
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

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
import software.amazon.awssdk.crt.http.Http2StreamManagerOptions;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBase;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.http.HttpStreamResponseHandler;
import software.amazon.awssdk.crt.http.HttpVersion;
import software.amazon.awssdk.crt.http.HttpStream;
import software.amazon.awssdk.crt.http.HttpStreamBase;
import software.amazon.awssdk.crt.http.HttpStreamBaseResponseHandler;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.utils.ByteBufferUtils;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;

public class HttpStreamManagerTest extends HttpRequestResponseFixture {
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
                Http2StreamManagerOptions options = new Http2StreamManagerOptions();
                HttpClientConnectionManagerOptions connectionManagerOptions = new HttpClientConnectionManagerOptions();
                connectionManagerOptions.withClientBootstrap(bootstrap)
                        .withSocketOptions(sockOpts)
                        .withTlsContext(tlsContext)
                        .withUri(uri)
                        .withMaxConnections(numConnections);
                options.withConnectionManagerOptions(connectionManagerOptions);

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

    private TestHttpResponse getResponseFromManager(HttpStreamManager streamManager, HttpRequestBase request)
            throws Exception {

        final CompletableFuture<Void> reqCompleted = new CompletableFuture<>();

        final TestHttpResponse response = new TestHttpResponse();

        try {
            HttpStreamBaseResponseHandler streamHandler = new HttpStreamBaseResponseHandler() {
                @Override
                public void onResponseHeaders(HttpStreamBase stream, int responseStatusCode, int blockType,
                        HttpHeader[] nextHeaders) {
                    response.statusCode = responseStatusCode;
                    Assert.assertEquals(responseStatusCode, stream.getResponseStatusCode());
                    response.headers.addAll(Arrays.asList(nextHeaders));
                }

                @Override
                public void onResponseHeadersDone(HttpStreamBase stream, int blockType) {
                    response.blockType = blockType;
                }

                @Override
                public int onResponseBody(HttpStreamBase stream, byte[] bodyBytesIn) {
                    try {
                        response.bodyBuffer.put(bodyBytesIn);
                    } catch (Exception e) {
                        Assert.assertNull(e);
                    }
                    int amountRead = bodyBytesIn.length;

                    // Slide the window open by the number of bytes just read
                    return amountRead;
                }

                @Override
                public void onResponseComplete(HttpStreamBase stream, int errorCode) {
                    response.onCompleteErrorCode = errorCode;
                    reqCompleted.complete(null);
                    stream.close();
                }
            };
            streamManager.acquireStream(request, streamHandler).get(60, TimeUnit.SECONDS);
            // Give the request up to 60 seconds to complete, otherwise throw a
            // TimeoutException
            reqCompleted.get(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw e;
        }

        return response;

    }

    @Test
    public void testSanitizerHTTP1() throws Exception {
        URI uri = new URI(endpoint);
        CompletableFuture<Void> shutdownComplete = null;
        try (HttpStreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, HttpVersion.HTTP_1_1)) {
            shutdownComplete = streamManager.getShutdownCompleteFuture();
            Assert.assertEquals(streamManager.getHttpVersion(), HttpVersion.HTTP_1_1);
        }

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    @Test
    public void testSanitizerHTTP2() throws Exception {
        URI uri = new URI(endpoint);
        CompletableFuture<Void> shutdownComplete = null;
        try (HttpStreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, HttpVersion.HTTP_2)) {
            shutdownComplete = streamManager.getShutdownCompleteFuture();
            Assert.assertEquals(streamManager.getHttpVersion(), HttpVersion.HTTP_2);
        }

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    @Test
    public void testSingleHTTP2Requests() throws Exception {
        URI uri = new URI(endpoint);
        CompletableFuture<Void> shutdownComplete = null;
        try (HttpStreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, HttpVersion.HTTP_2)) {
            shutdownComplete = streamManager.getShutdownCompleteFuture();
            Http2Request request = createHttp2Request("GET", endpoint, path, EMPTY_BODY);
            TestHttpResponse response = this.getResponseFromManager(streamManager, request);
            Assert.assertEquals(response.statusCode, EXPECTED_HTTP_STATUS);
        }

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }

    @Test
    public void testSingleHTTP1Request() throws Throwable {
        URI uri = new URI(endpoint);
        CompletableFuture<Void> shutdownComplete = null;
        try (HttpStreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, HttpVersion.HTTP_1_1)) {
            shutdownComplete = streamManager.getShutdownCompleteFuture();
            HttpRequest request = createHttp1Request("GET", endpoint, path, EMPTY_BODY);
            TestHttpResponse response = this.getResponseFromManager(streamManager, request);
            Assert.assertEquals(response.statusCode, EXPECTED_HTTP_STATUS);
        }

        shutdownComplete.get(60, TimeUnit.SECONDS);
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
        CompletableFuture<Void> shutdownComplete = null;
        try (HttpStreamManager streamManager = createStreamManager(uri, NUM_CONNECTIONS, HttpVersion.HTTP_1_1)) {
            /*
             * http2 request which will have :method headers that is not allowed for
             * HTTP/1.1
             */
            Http2Request request = createHttp2Request("GET", endpoint, path, EMPTY_BODY);
            shutdownComplete = streamManager.getShutdownCompleteFuture();
            TestHttpResponse response = this.getResponseFromManager(streamManager, request);
            Assert.assertEquals(response.statusCode, EXPECTED_HTTP_STATUS);
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

        shutdownComplete.get(60, TimeUnit.SECONDS);
        CrtResource.logNativeResources();
        CrtResource.waitForNoResources();
    }
}
