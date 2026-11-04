/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;
import software.amazon.awssdk.crt.http.HttpClientConnection;
import software.amazon.awssdk.crt.http.HttpClientConnectionManager;
import software.amazon.awssdk.crt.http.HttpClientConnectionManagerOptions;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;

/**
 * Closing an {@link HttpClientConnectionManager} while another thread is in
 * {@link HttpClientConnectionManager#acquireConnection()} must fail the acquire, not the process.
 *
 * The race runs in a child JVM against a local ServerSocket, and the test asserts the child exits cleanly.
 */
public class HttpClientConnectionManagerCloseRaceTest extends CrtTestFixture {
    private static final int ITERATIONS = 50;
    private static final long CHILD_TIMEOUT_SECONDS = 120;

    @Test
    public void testCloseDuringAcquireDoesNotAbortProcess() throws Exception {
        skipIfAndroid(); // needs to spawn a JVM

        List<String> command = new ArrayList<>();
        command.add(Paths.get(System.getProperty("java.home"), "bin", "java").toString());
        // Forward CRT settings so the child loads the same native build.
        for (String name : System.getProperties().stringPropertyNames()) {
            if (name.startsWith("aws.crt.")) {
                command.add("-D" + name + "=" + System.getProperty(name));
            }
        }
        command.add("-cp");
        command.add(childClasspath());
        command.add(Child.class.getName());
        command.add(Integer.toString(ITERATIONS));

        Process child = new ProcessBuilder(command).redirectErrorStream(true).start();
        CompletableFuture<String> output = readAllAsync(child.getInputStream());

        if (!child.waitFor(CHILD_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            child.destroyForcibly();
            Assert.fail("Child JVM did not finish in " + CHILD_TIMEOUT_SECONDS + "s:\n" + output.get());
        }
        Assert.assertEquals("Child JVM died racing close() with acquireConnection():\n" + output.get(),
                0, child.exitValue());
    }

    // Surefire runs in-process (forkCount=0), so java.class.path is not the test classpath.
    private static String childClasspath() throws Exception {
        String crt = Paths.get(HttpClientConnectionManager.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).toString();
        String tests = Paths.get(Child.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).toString();
        return crt + File.pathSeparator + tests;
    }

    private static CompletableFuture<String> readAllAsync(InputStream in) {
        return CompletableFuture.supplyAsync(() -> {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            try {
                for (int n; (n = in.read(buf)) != -1; ) {
                    out.write(buf, 0, n);
                }
            } catch (IOException e) {
                // Child is gone; return what we have.
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        });
    }

    /**
     * Runs in the child JVM. Must not reference JUnit or the outer class.
     */
    public static final class Child {
        public static void main(String[] args) throws Exception {
            int iterations = Integer.parseInt(args[0]);

            try (ServerSocket server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())) {
                startAcceptor(server);
                URI uri = new URI("http://127.0.0.1:" + server.getLocalPort());

                try (EventLoopGroup elg = new EventLoopGroup(1);
                        HostResolver resolver = new HostResolver(elg);
                        ClientBootstrap bootstrap = new ClientBootstrap(elg, resolver);
                        SocketOptions socketOptions = new SocketOptions()) {
                    for (int i = 0; i < iterations; i++) {
                        raceCloseAgainstAcquire(bootstrap, socketOptions, uri);
                    }
                }
            }
            System.out.println("survived " + iterations + " close/acquire races");
            System.exit(0);
        }

        private static void raceCloseAgainstAcquire(ClientBootstrap bootstrap, SocketOptions socketOptions,
                URI uri) throws Exception {
            HttpClientConnectionManager manager = HttpClientConnectionManager.create(
                    new HttpClientConnectionManagerOptions()
                            .withClientBootstrap(bootstrap)
                            .withSocketOptions(socketOptions)
                            .withUri(uri)
                            .withMaxConnections(1));

            // Hold the only connection so the manager stays alive through close() and every
            // acquire below pends instead of completing.
            HttpClientConnection held = manager.acquireConnection().get(10, TimeUnit.SECONDS);

            List<CompletableFuture<HttpClientConnection>> pending = new ArrayList<>();
            AtomicLong attempts = new AtomicLong();
            Thread requester = new Thread(() -> {
                try {
                    while (true) {
                        pending.add(manager.acquireConnection());
                        attempts.incrementAndGet();
                    }
                } catch (IllegalStateException expected) {
                    // Manager closed.
                }
            }, "acquire-spinner");
            requester.start();

            // Close while the requester is mid-loop.
            while (attempts.get() < 100 && requester.isAlive()) {
                Thread.yield();
            }
            manager.close();

            requester.join(TimeUnit.SECONDS.toMillis(10));
            if (requester.isAlive()) {
                throw new IllegalStateException("acquireConnection() never observed close()");
            }

            held.close();
            for (CompletableFuture<HttpClientConnection> f : pending) {
                try {
                    f.get(10, TimeUnit.SECONDS).close();
                } catch (ExecutionException expected) {
                    // Failed by manager shutdown.
                }
            }
            manager.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
        }

        private static void startAcceptor(ServerSocket server) {
            Thread acceptor = new Thread(() -> {
                List<Socket> open = new ArrayList<>();
                try {
                    while (true) {
                        open.add(server.accept());
                    }
                } catch (IOException e) {
                    // Server closed.
                }
            }, "test-acceptor");
            acceptor.setDaemon(true);
            acceptor.start();
        }
    }
}
