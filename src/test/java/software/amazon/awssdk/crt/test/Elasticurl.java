package software.amazon.awssdk.crt.test;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.http.HttpVersion;
import software.amazon.awssdk.crt.http.Http2Request;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBase;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.http.HttpStream;
import software.amazon.awssdk.crt.http.HttpStreamResponseHandler;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.utils.ByteBufferUtils;

public class Elasticurl {

    static void exit() {
        System.exit(1);
    }

    static void exit(String msg) {
        System.out.println(msg);
        exit();
    }

    static CommandLine parseArgs(String args[]) {
        Options cliOpts = new Options();

        cliOpts.addOption("h", "help", false, "show this help message and exit");
        cliOpts.addOption(Option.builder().longOpt("cacert").hasArg().argName("file")
                .desc("path to a CA certificate file.").build());
        cliOpts.addOption(Option.builder().longOpt("capath").hasArg().argName("dir")
                .desc("path to a directory containing CA files.").build());
        cliOpts.addOption(Option.builder().longOpt("cert").hasArg().argName("file")
                .desc("path to a PEM encoded certificate to use with mTLS.").build());
        cliOpts.addOption(Option.builder().longOpt("key").hasArg().argName("file")
                .desc("path to a PEM encoded private key that matches cert.").build());
        cliOpts.addOption(Option.builder().longOpt("connect_timeout").hasArg().argName("int")
                .desc("time in milliseconds to wait for a connection.").build());
        cliOpts.addOption(Option.builder("H").longOpt("header").hasArgs().argName("str")
                .desc("line to send as a header in format 'name:value'. May be specified multiple times.").build());
        cliOpts.addOption(Option.builder("d").longOpt("data").hasArg().argName("str")
                .desc("data to send in POST or PUT.").build());
        cliOpts.addOption(Option.builder().longOpt("data_file").hasArg().argName("file")
                .desc("file to send in POST or PUT.").build());
        cliOpts.addOption(Option.builder("M").longOpt("method").hasArg().argName("str")
                .desc("request method. Default is GET)").build());
        cliOpts.addOption("G", "get", false, "uses GET for request method.");
        cliOpts.addOption("P", "post", false, "uses POST for request method.");
        cliOpts.addOption("I", "head", false, "uses HEAD for request method.");
        cliOpts.addOption("i", "include", false, "includes headers in output.");
        cliOpts.addOption("k", "insecure", false, "turns off x.509 validation.");
        cliOpts.addOption(Option.builder("o").longOpt("output").hasArg().argName("file")
                .desc("dumps content-body to FILE instead of stdout.").build());
        cliOpts.addOption(Option.builder("t").longOpt("trace").hasArg().argName("file")
                .desc("dumps logs to FILE instead of stderr.").build());
        cliOpts.addOption(Option.builder("p").longOpt("alpn").hasArgs().argName("str")
                .desc("protocol for ALPN. May be specified multiple times.").build());
        cliOpts.addOption(null, "http1_1", false, "HTTP/1.1 connection required.");
        cliOpts.addOption(null, "http2", false, "HTTP/2 connection required.");
        cliOpts.addOption(Option.builder("v").longOpt("verbose").hasArg().argName("str")
                .desc("logging level (ERROR|WARN|INFO|DEBUG|TRACE) default is none.").build());

        CommandLineParser cliParser = new DefaultParser();
        CommandLine cli = null;
        try {
            cli = cliParser.parse(cliOpts, args);

            if (cli.hasOption("help") || cli.getArgs().length == 0) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("elasticurl [OPTIONS]... URL", cliOpts);
                exit();
            }

        } catch (ParseException e) {
            exit(e.getMessage());
        }

        return cli;
    }

    private static HttpRequestBase buildHttpRequest(CommandLine cli, HttpVersion requiredVersion, URI uri)
            throws Exception {
        String method = cli.getOptionValue("method");
        if (cli.hasOption("get")) {
            method = "GET";
        } else if (cli.hasOption("post")) {
            method = "POST";
        } else if (cli.hasOption("head")) {
            method = "HEAD";
        }
        if (method == null) {
            method = "GET";
        }
        String pathAndQuery = uri.getQuery() == null ? uri.getPath() : uri.getPath() + "?" + uri.getQuery();
        if (pathAndQuery.length() == 0) {
            pathAndQuery = "/";
        }

        /* body */
        ByteBuffer tmpPayload = null;
        if (cli.getOptionValue("data") != null) {
            tmpPayload = ByteBuffer.wrap(cli.getOptionValue("data").getBytes());
        } else if (cli.getOptionValue("data_file") != null) {
            Path path = Paths.get(cli.getOptionValue("data_file"));
            tmpPayload = ByteBuffer.wrap(Files.readAllBytes(path));
        }
        HttpRequestBodyStream payloadStream = null;
        if (tmpPayload != null) {
            final ByteBuffer payload = tmpPayload;
            payloadStream = new HttpRequestBodyStream() {
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
        }
        /* initial headers */
        HttpHeader[] headers = new HttpHeader[] {};
        HttpRequestBase request = requiredVersion == HttpVersion.HTTP_2 ? new Http2Request(headers, payloadStream)
                : new HttpRequest(method, pathAndQuery, headers, payloadStream);

        /* Version specific headers */
        if (requiredVersion == HttpVersion.HTTP_2) {
            request.addHeader(new HttpHeader(":method", method));
            request.addHeader(new HttpHeader(":scheme", uri.getScheme()));
            request.addHeader(new HttpHeader(":authority", uri.getAuthority()));
            request.addHeader(new HttpHeader(":path", pathAndQuery));
        } else {
            request.addHeader(new HttpHeader("Host", uri.getHost()));
        }

        /* General headers */
        request.addHeader(new HttpHeader("accept", "*/*"));
        request.addHeader(new HttpHeader("user-agent", "elasticurl 1.0, Powered by the AWS Common Runtime."));

        /* Customized headers */
        String[] customizedHeaders = cli.getOptionValues("header");
        if (customizedHeaders != null) {
            for (String header : customizedHeaders) {
                String[] pair = header.split(":");
                request.addHeader(new HttpHeader(pair[0].trim(), pair[1].trim()));
            }
        }
        return request;
    }

    public static void main(String args[]) throws Exception {
        CommandLine cli = parseArgs(args);

        // enable logging
        String verbose = cli.getOptionValue("verbose");
        if (verbose != null) {
            LogLevel logLevel = LogLevel.None;
            if (verbose.equals("ERROR")) {
                logLevel = LogLevel.Error;
            } else if (verbose.equals("WARN")) {
                logLevel = LogLevel.Warn;
            } else if (verbose.equals("INFO")) {
                logLevel = LogLevel.Info;
            } else if (verbose.equals("DEBUG")) {
                logLevel = LogLevel.Debug;
            } else if (verbose.equals("TRACE")) {
                logLevel = LogLevel.Trace;
            } else {
                exit(logLevel + " unsupported value for verbose option");
            }

            String trace = cli.getOptionValue("trace");
            if (trace != null) {
                Log.initLoggingToFile(logLevel, trace);
            } else {
                Log.initLoggingToStderr(logLevel);
            }
        }

        if (cli.getArgs().length == 0) {
            exit("missing URL");
        }

        URI uri = new URI(cli.getArgs()[0]);
        boolean useTls = true;
        int port = 443;
        if (uri.getScheme().equals("http")) {
            useTls = false;
            port = 80;
        }

        HttpVersion requiredVersion = HttpVersion.UNKNOWN;
        if (cli.hasOption("http1_1")) {
            requiredVersion = HttpVersion.HTTP_1_1;
        } else if (cli.hasOption("http2")) {
            requiredVersion = HttpVersion.HTTP_2;
        }

        TlsContextOptions tlsOpts = null;
        TlsContext tlsCtx = null;
        try {
            // set up TLS (if https)
            if (useTls) {
                String cert = cli.getOptionValue("cert");
                String key = cli.getOptionValue("key");
                if (cert != null && key != null) {
                    tlsOpts = TlsContextOptions.createWithMtlsFromPath(cert, key);
                } else {
                    tlsOpts = TlsContextOptions.createDefaultClient();
                }

                String caPath = cli.getOptionValue("capath");
                String caCert = cli.getOptionValue("cacert");
                if (caPath != null || caCert != null) {
                    tlsOpts.overrideDefaultTrustStoreFromPath(caPath, caCert);
                }

                if (cli.hasOption("insecure")) {
                    tlsOpts.verifyPeer = false;
                }

                String[] alpn = cli.getOptionValues("alpn");
                if (alpn == null) {
                    if (requiredVersion == HttpVersion.HTTP_1_1) {
                        alpn = new String[] { "http/1.1" };
                    } else if (requiredVersion == HttpVersion.HTTP_2) {
                        alpn = new String[] { "h2" };
                    } else {
                        alpn = new String[] { "h2", "http/1.1" };
                    }
                }
                tlsOpts.alpnList = Arrays.asList(alpn);

                tlsCtx = new TlsContext(tlsOpts);
            }

            CompletableFuture<Void> connMgrShutdownComplete = null;
            final BufferedOutputStream out = cli.getOptionValue("output") == null ? new BufferedOutputStream(System.out)
                    : new BufferedOutputStream(new FileOutputStream(cli.getOptionValue("output")));
            try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                    HostResolver resolver = new HostResolver(eventLoopGroup);
                    ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                    SocketOptions socketOpts = new SocketOptions()) {
                if (cli.getOptionValue("connect_timeout") != null) {
                    int timeout = Integer.parseInt(cli.getOptionValue("connect_timeout"));
                    socketOpts.connectTimeoutMs = timeout;
                }
                HttpClientConnectionManagerOptions connMgrOpts = new HttpClientConnectionManagerOptions()
                        .withClientBootstrap(bootstrap).withSocketOptions(socketOpts).withTlsContext(tlsCtx)
                        .withUri(uri).withPort(port);

                try (HttpClientConnectionManager connMgr = HttpClientConnectionManager.create(connMgrOpts)) {
                    connMgrShutdownComplete = connMgr.getShutdownCompleteFuture();
                    try (HttpClientConnection conn = connMgr.acquireConnection().get(60, TimeUnit.SECONDS)) {

                        final CompletableFuture<Void> reqCompleted = new CompletableFuture<>();
                        HttpStreamResponseHandler streamHandler = new HttpStreamResponseHandler() {
                            boolean statusWritten = false;

                            @Override
                            public void onResponseHeaders(HttpStream stream, int responseStatusCode, int blockType,
                                    HttpHeader[] nextHeaders) {
                                if (blockType == 1) {
                                    /* Ignore informational headers */
                                    return;
                                }
                                if (cli.hasOption("include")) {
                                    if (!statusWritten) {
                                        System.out.println(String.format("Response Status: %d", responseStatusCode));
                                        statusWritten = true;
                                    }
                                    for (HttpHeader header : nextHeaders) {
                                        System.out.println(header.getName() + ": " + header.getValue());
                                    }
                                }
                            }

                            @Override
                            public int onResponseBody(HttpStream stream, byte[] bodyBytesIn) {
                                try {
                                    out.write(bodyBytesIn, 0, bodyBytesIn.length);
                                    out.flush();
                                } catch (Exception e) {
                                    exit("Failed to write the body");
                                }
                                return bodyBytesIn.length;
                            }

                            @Override
                            public void onResponseComplete(HttpStream stream, int errorCode) {
                                if (errorCode!=0) {
                                    reqCompleted.completeExceptionally(new CrtRuntimeException(errorCode));
                                } else{
                                    reqCompleted.complete(null);
                                }
                                stream.close();
                            }
                        };
                        HttpRequestBase request = buildHttpRequest(cli, requiredVersion, uri);
                        HttpStream stream = conn.makeRequest(request, streamHandler);
                        stream.activate();

                        // Give the request up to 60 seconds to complete, otherwise throw a
                        // TimeoutException
                        reqCompleted.get(60, TimeUnit.SECONDS);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } finally {
                out.close();
                if (connMgrShutdownComplete != null) {
                    connMgrShutdownComplete.get();
                }
            }

        } finally {
            if (tlsCtx != null) {
                tlsCtx.close();
            }

            if (tlsOpts != null) {
                tlsOpts.close();
            }
        }
    }
}
