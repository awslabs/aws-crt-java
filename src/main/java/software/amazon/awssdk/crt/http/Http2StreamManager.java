package software.amazon.awssdk.crt.http;

import java.net.URI;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;

public class Http2StreamManager extends CrtResource {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final int DEFAULT_HTTP_PORT = 80;
    private static final int DEFAULT_HTTPS_PORT = 443;
    public static Http2StreamManager create(Http2StreamManagerOptions options) {
        return new Http2StreamManager(options);
    }

    private Http2StreamManager(Http2StreamManagerOptions options) {
    }

    public CompletableFuture<HttpStream> acquireStream(HttpRequestBase request, HttpStreamResponseHandler streamHandler) throws CrtRuntimeException{
        throw UnsupportedOperationException;
    }
}
