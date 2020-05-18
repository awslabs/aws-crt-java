package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.http.HttpVersion;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;

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
        cliOpts.addOption(Option.builder()
            .longOpt("cacert")
            .hasArg()
            .argName("file")
            .desc("path to a CA certificate file.")
            .build());
        cliOpts.addOption(Option.builder()
            .longOpt("capath")
            .hasArg()
            .argName("dir")
            .desc("path to a directory containing CA files.")
            .build());
        cliOpts.addOption(Option.builder()
            .longOpt("cert")
            .hasArg()
            .argName("file")
            .desc("path to a PEM encoded certificate to use with mTLS.")
            .build());
        cliOpts.addOption(Option.builder()
            .longOpt("key")
            .hasArg()
            .argName("file")
            .desc("path to a PEM encoded private key that matches cert.")
            .build());
        cliOpts.addOption(Option.builder()
            .longOpt("connect_timeout")
            .hasArg()
            .argName("int")
            .desc("time in milliseconds to wait for a connection.")
            .build());
        cliOpts.addOption(Option.builder("H")
            .longOpt("header")
            .hasArgs()
            .argName("str")
            .desc("line to send as a header in format 'name:value'. May be specified multiple times.")
            .build());
        cliOpts.addOption(Option.builder("d")
            .longOpt("data")
            .hasArg()
            .argName("str")
            .desc("data to send in POST or PUT.")
            .build());
        cliOpts.addOption(Option.builder()
            .longOpt("data_file")
            .hasArg()
            .argName("file")
            .desc("file to send in POST or PUT.")
            .build());
        cliOpts.addOption(Option.builder("M")
            .longOpt("method")
            .hasArg()
            .argName("str")
            .desc("request method. Default is GET)")
            .build());
        cliOpts.addOption("G", "get", false, "uses GET for request method.");
        cliOpts.addOption("P", "post", false, "uses POST for request method.");
        cliOpts.addOption("I", "head", false, "uses HEAD for request method.");
        cliOpts.addOption("i", "include", false, "includes headers in output.");
        cliOpts.addOption("k", "insecure", false, "turns off x.509 validation.");
        cliOpts.addOption(Option.builder("o")
            .longOpt("output")
            .hasArg()
            .argName("file")
            .desc("dumps content-body to FILE instead of stdout.")
            .build());
        cliOpts.addOption(Option.builder("t")
            .longOpt("trace")
            .hasArg()
            .argName("file")
            .desc("dumps logs to FILE instead of stderr.")
            .build());
        cliOpts.addOption(Option.builder("p")
            .longOpt("alpn")
            .hasArgs()
            .argName("str")
            .desc("protocol for ALPN. May be specified multiple times.")
            .build());
        cliOpts.addOption(null, "http1_1", false, "HTTP/1.1 connection required.");
        cliOpts.addOption(null, "http2", false, "HTTP/2 connection required.");
        cliOpts.addOption(Option.builder("v")
            .longOpt("verbose")
            .hasArg()
            .argName("str")
            .desc("logging level (ERROR|INFO|DEBUG|TRACE) default is none.")
            .build());

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

    public static void main(String args[]) throws Exception {
        CommandLine cli = parseArgs(args);

        // enable logging
        String verbose = cli.getOptionValue("verbose");
        if (verbose != null) {
            LogLevel logLevel = LogLevel.None;
            if (verbose == "ERROR") {
                logLevel = LogLevel.Error;
            } else if (verbose == "INFO") {
                logLevel = LogLevel.Info;
            } else if (verbose == "DEBUG") {
                logLevel = LogLevel.Debug;
            } else if (verbose == "TRACE") {
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
        if (uri.getScheme() == "http") {
            useTls = true;
            port = 80;
        }

        HttpVersion requiredVersion = HttpVersion.UNKNOWN;
        if (cli.hasOption("http1_1")) {
            requiredVersion = HttpVersion.HTTP1_1;
        } else if (cli.hasOption("http2")) {
            requiredVersion = HttpVersion.HTTP2;
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
                    if (requiredVersion == HttpVersion.HTTP1_1) {
                        alpn = new String[]{"http/1.1"};
                    } else if (requiredVersion == HttpVersion.HTTP2) {
                        alpn = new String[]{"h2"};
                    } else {
                        alpn = new String[]{"h2", "http/1.1"};
                    }
                }
                tlsOpts.alpnList = Arrays.asList(alpn);

                tlsCtx = new TlsContext(tlsOpts);
            }

            CompletableFuture connMgrShutdownComplete = null;

            try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                 HostResolver resolver = new HostResolver(eventLoopGroup);
                 ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                 SocketOptions socketOpts = new SocketOptions()) {

                HttpClientConnectionManagerOptions connMgrOpts = new HttpClientConnectionManagerOptions()
                    .withClientBootstrap(bootstrap)
                    .withSocketOptions(socketOpts)
                    .withTlsContext(tlsCtx)
                    .withUri(uri)
                    .withPort(port);

                try (HttpClientConnectionManager connMgr = HttpClientConnectionManager.create(connMgrOpts)) {
                    connMgrShutdownComplete = connMgr.getShutdownCompleteFuture();
                    try () { YOU ARE HERE
                    }
                } finally {
                    if (connMgrShutdownComplete != null) {
                        connMgrShutdownComplete.get();
                    }
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
