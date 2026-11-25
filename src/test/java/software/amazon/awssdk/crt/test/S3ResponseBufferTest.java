/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.DelegateCredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.StandardRetryOptions;
import software.amazon.awssdk.crt.io.ExponentialBackoffRetryOptions;
import software.amazon.awssdk.crt.s3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.Path;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/** Exercises the real native S3 engine without credentials or an external bucket. */
public class S3ResponseBufferTest extends CrtTestFixture {
    private static final int PART_SIZE = 1024 * 1024;
    private static final byte GUARD = (byte) 0xa7;

    private enum Behavior {
        NORMAL, SHORT_ONCE, SHORT_SECOND_ONCE, SHORT_ALWAYS, SHORT_DECLARED, STALL, STALL_FIRST, ERROR, BAD_CHECKSUM
    }

    private static byte[] payload(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; ++i) {
            data[i] = (byte) (i * 31 + i / 251);
        }
        return data;
    }

    private static final class BodyGate {
        final int offset;
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final CountDownLatch tailAttempted = new CountDownLatch(1);
        final CountDownLatch finished = new CountDownLatch(1);

        BodyGate(int offset) {
            this.offset = offset;
        }
    }

    private static final class Loopback implements AutoCloseable {
        final byte[] data;
        final HttpServer server;
        final ExecutorService executor = Executors.newCachedThreadPool();
        final AtomicInteger gets = new AtomicInteger();
        final AtomicInteger shortAttempts = new AtomicInteger();
        final CountDownLatch bodyStarted = new CountDownLatch(1);
        final CountDownLatch releaseBody = new CountDownLatch(1);
        final Map<Integer, BodyGate> bodyGates = new HashMap<>();
        final Behavior behavior;
        final EventLoopGroup elg;
        final HostResolver resolver;
        final ClientBootstrap bootstrap;
        final AwsSigningConfig signing;
        final S3Client client;

        Loopback(int size) throws IOException {
            this(size, Behavior.NORMAL, false, false);
        }

        Loopback(int size, Behavior behavior, boolean pool, boolean backpressure, BodyGate... gates) throws IOException {
            data = payload(size);
            this.behavior = behavior;
            for (BodyGate gate : gates) {
                bodyGates.put(gate.offset, gate);
            }
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(executor);
            server.createContext("/object", this::serve);
            server.start();
            elg = new EventLoopGroup(2);
            resolver = new HostResolver(elg);
            bootstrap = new ClientBootstrap(elg, resolver);
            signing = new AwsSigningConfig();
            signing.setRegion("us-east-1");
            signing.setService("s3");
            signing.setCredentials(Credentials.createAnonymousCredentials());
            S3ClientOptions options = new S3ClientOptions().withRegion("us-east-1")
                    .withClientBootstrap(bootstrap).withSigningConfig(signing)
                    .withPartSize(PART_SIZE).withReadBackpressureEnabled(backpressure)
                    .withInitialReadWindowSize(PART_SIZE)
                    .withStandardRetryOptions(new StandardRetryOptions().withBackoffRetryOptions(
                            new ExponentialBackoffRetryOptions().withMaxRetries(1).withBackoffScaleFactorMS(1)));
            if (pool) {
                options.withDirectBufferPoolOptions(S3DirectBufferPoolOptions.fixed(8L * PART_SIZE));
            }
            client = new S3Client(options);
        }

        private void serve(HttpExchange exchange) throws IOException {
            BodyGate gate = null;
            try {
                exchange.getResponseHeaders().set("ETag", "\"loopback-object\"");
                exchange.getResponseHeaders().set("Accept-Ranges", "bytes");
                if (behavior == Behavior.BAD_CHECKSUM) {
                    exchange.getResponseHeaders().set("x-amz-checksum-crc32", "AAAAAA==");
                }
                int start = 0;
                int end = data.length - 1;
                String range = exchange.getRequestHeaders().getFirst("Range");
                if (range != null) {
                    String[] bounds = range.substring("bytes=".length()).split("-", -1);
                    if (bounds[0].isEmpty()) {
                        start = Math.max(0, data.length - Integer.parseInt(bounds[1]));
                    } else {
                        start = Integer.parseInt(bounds[0]);
                        if (!bounds[1].isEmpty()) {
                            end = Math.min(end, Integer.parseInt(bounds[1]));
                        }
                    }
                }
                boolean partNumber = "partNumber=1".equals(exchange.getRequestURI().getQuery());
                if (data.length > 0 && (range != null || partNumber)) {
                    exchange.getResponseHeaders().set("Content-Range",
                            "bytes " + start + "-" + end + "/" + data.length);
                }
                int length = Math.max(0, end - start + 1);
                int status = range == null && !partNumber ? 200 : 206;
                boolean head = exchange.getRequestMethod().equals("HEAD");
                int attempt = head ? 0 : gets.incrementAndGet();
                if (head || length == 0) {
                    exchange.getResponseHeaders().set("Content-Length", Integer.toString(length));
                    exchange.sendResponseHeaders(status, -1);
                    return;
                }
                if (behavior == Behavior.ERROR) {
                    byte[] error = "<Error><Code>NoSuchKey</Code></Error>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(404, error.length);
                    exchange.getResponseBody().write(error);
                    return;
                }
                // Keep gated streams cancellable without first draining the withheld body.
                if (!bodyGates.containsKey(start)) {
                    exchange.getResponseHeaders().set("Connection", "close");
                }
                if (behavior == Behavior.SHORT_DECLARED && start == PART_SIZE) {
                    exchange.sendResponseHeaders(status, 4096);
                    exchange.getResponseBody().write(data, start, 4096);
                    return;
                }
                exchange.sendResponseHeaders(status, length);
                if (behavior == Behavior.SHORT_ALWAYS || (behavior == Behavior.SHORT_ONCE && attempt == 1)
                        || (behavior == Behavior.SHORT_SECOND_ONCE && start == PART_SIZE
                                && shortAttempts.getAndIncrement() == 0)) {
                    byte[] poison = new byte[Math.min(4096, length / 2)];
                    Arrays.fill(poison, (byte) 0x5c);
                    exchange.getResponseBody().write(poison);
                    exchange.getResponseBody().flush();
                    return;
                }
                gate = bodyGates.get(start);
                if (gate != null || behavior == Behavior.STALL || (behavior == Behavior.STALL_FIRST && start == 0)) {
                    int prefix = Math.min(4096, length);
                    exchange.getResponseBody().write(data, start, prefix);
                    exchange.getResponseBody().flush();
                    (gate == null ? bodyStarted : gate.started).countDown();
                    try {
                        if (!(gate == null ? releaseBody : gate.release).await(30, TimeUnit.SECONDS)) {
                            return;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    if (gate != null) {
                        gate.tailAttempted.countDown();
                    }
                    exchange.getResponseBody().write(data, start + prefix, length - prefix);
                } else {
                    exchange.getResponseBody().write(data, start, length);
                }
            } finally {
                exchange.close();
                if (gate != null) {
                    gate.finished.countDown();
                }
            }
        }

        S3MetaRequestOptions options(Collector collector, String range) {
            String authority = "127.0.0.1:" + server.getAddress().getPort();
            HttpHeader[] headers = range == null
                    ? new HttpHeader[] {new HttpHeader("Host", authority)}
                    : new HttpHeader[] {new HttpHeader("Host", authority), new HttpHeader("Range", range)};
            return new S3MetaRequestOptions().withMetaRequestType(S3MetaRequestOptions.MetaRequestType.GET_OBJECT)
                    .withHttpRequest(new HttpRequest("GET", "/object", headers, null))
                    .withEndpoint(URI.create("http://" + authority)).withResponseHandler(collector);
        }

        @Override
        public void close() throws Exception {
            releaseBody.countDown();
            for (BodyGate gate : bodyGates.values()) {
                gate.release.countDown();
            }
            if (!client.isNull()) {
                client.close();
            }
            client.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
            signing.close();
            bootstrap.close();
            resolver.close();
            elg.close();
            elg.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
            server.stop(0);
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private static class Collector implements S3MetaRequestResponseHandler {
        final byte[] copied;
        final AtomicInteger callbacks = new AtomicInteger();
        final AtomicLong heapBytes = new AtomicLong();
        final CompletableFuture<S3FinishedResponseContext> finished = new CompletableFuture<>();

        Collector(int size) {
            copied = new byte[size];
        }

        @Override
        public int onResponseBody(ByteBuffer body, long start, long end) {
            assertFalse("Legacy callback must expose the JNI heap copy", body.isDirect());
            assertTrue(body.hasArray());
            int count = body.remaining();
            callbacks.incrementAndGet();
            heapBytes.addAndGet(count);
            body.get(copied, (int) start, count);
            return count;
        }

        @Override
        public void onFinished(S3FinishedResponseContext context) {
            finished.complete(context);
        }
    }

    private static S3FinishedResponseContext download(Loopback fixture, S3MetaRequestOptions options,
            Collector collector) throws Exception {
        S3MetaRequest request = fixture.client.makeMetaRequest(options);
        try {
            return collector.finished.get(20, TimeUnit.SECONDS);
        } finally {
            request.close();
            request.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testLegacyCallbackCopiesPayload() throws Exception {
        for (int size : new int[] {8193, 3 * PART_SIZE + 137}) {
            try (Loopback fixture = new Loopback(size)) {
                Collector collector = new Collector(size);
                S3FinishedResponseContext result = download(fixture, fixture.options(collector, null), collector);
                assertEquals(0, result.getErrorCode());
                assertEquals(0, result.getResponseBufferLength());
                assertEquals(size, collector.heapBytes.get());
                assertTrue(collector.callbacks.get() > 0);
                assertArrayEquals(fixture.data, collector.copied);
                byte[] expected = fixture.data.clone();
                Arrays.fill(fixture.data, (byte) 0);
                assertArrayEquals("The callback consumer owns a copy", expected, collector.copied);
                System.out.println("Legacy S3 payload: bytes=" + size + ", JNI heap-copy bytes="
                        + collector.heapBytes.get() + ", consumer-copy bytes=" + collector.heapBytes.get()
                        + ", callbacks=" + collector.callbacks.get() + ", GETs=" + fixture.gets.get());
            }
        }
    }

    private static ByteBuffer guarded(int capacity) {
        ByteBuffer storage = ByteBuffer.allocateDirect(capacity);
        for (int i = 0; i < capacity; ++i) {
            storage.put(i, GUARD);
        }
        return storage;
    }

    private static void assertGuards(ByteBuffer storage, int start, int end) {
        for (int i = 0; i < storage.capacity(); ++i) {
            if (i < start || i >= end) {
                assertEquals("Guard at " + i, GUARD, storage.get(i));
            }
        }
    }

    private static void assertPayload(ByteBuffer storage, int offset, byte[] expected, int start, int length) {
        byte[] actual = new byte[length];
        ByteBuffer view = storage.duplicate();
        view.position(offset);
        view.get(actual);
        assertArrayEquals(Arrays.copyOfRange(expected, start, start + length), actual);
    }

    private void checkReceive(int size, String range, int sourceOffset, int length, int capacity) throws Exception {
        try (Loopback fixture = new Loopback(size)) {
            ByteBuffer storage = guarded(capacity + 83);
            storage.position(17);
            ByteBuffer destination = storage.slice();
            storage.clear();
            destination.position(31);
            destination.limit(31 + capacity);
            Collector collector = new Collector(size);
            S3MetaRequestOptions options = fixture.options(collector, range).withResponseBuffer(destination);
            assertSame(destination, options.getResponseBuffer());
            S3FinishedResponseContext result = download(fixture, options, collector);
            assertEquals(0, result.getErrorCode());
            assertEquals(length, result.getResponseBufferLength());
            assertEquals(0, collector.callbacks.get());
            assertEquals(0, collector.heapBytes.get());
            assertEquals(31, destination.position());
            assertEquals(31 + capacity, destination.limit());
            assertPayload(storage, 48, fixture.data, sourceOffset, length);
            assertGuards(storage, 48, 48 + length);
            System.out.println("Receive-buffer S3 payload: bytes=" + length
                    + ", JNI heap-copy bytes=0, consumer-copy bytes=0, callbacks=0, GETs=" + fixture.gets.get());
        }
    }

    @Test
    public void testSmallAndMultipartWithOffsetAndGuards() throws Exception {
        for (int size : new int[] {8193, PART_SIZE - 1, PART_SIZE, PART_SIZE + 1, 3 * PART_SIZE + 137}) {
            checkReceive(size, null, 0, size, size);
        }
    }

    @Test
    public void testRangesAndSuffixes() throws Exception {
        int size = 3 * PART_SIZE + 137;
        checkReceive(size, "bytes=12345-2345678", 12345, 2345679 - 12345, 2345679 - 12345);
        checkReceive(size, "bytes=3145728-", 3145728, 137, 137);
        checkReceive(size, "bytes=-1234567", size - 1234567, 1234567, 1234567);
        checkReceive(8193, "bytes=-10000", 0, 8193, 10000);
    }

    @Test
    public void testShortRangeAtEofAndLargerDestination() throws Exception {
        checkReceive(8193, "bytes=8000-10000", 8000, 193, 2001);
        checkReceive(8193, null, 0, 8193, 10000);
        checkReceive(0, null, 0, 0, 1);
    }

    @Test
    public void testRejectInvalidDestinationsAndModes() throws Exception {
        try (Loopback fixture = new Loopback(8193)) {
            Collector collector = new Collector(8193);
            ByteBuffer empty = ByteBuffer.allocateDirect(4);
            empty.position(4);
            for (ByteBuffer invalid : new ByteBuffer[] {ByteBuffer.allocate(8193),
                    ByteBuffer.allocateDirect(8193).asReadOnlyBuffer(), ByteBuffer.allocateDirect(0), empty}) {
                S3MetaRequestOptions options = fixture.options(collector, null).withResponseBuffer(invalid);
                assertThrows(IllegalArgumentException.class, () -> fixture.client.makeMetaRequest(options));
            }
            for (S3MetaRequestOptions.MetaRequestType type : S3MetaRequestOptions.MetaRequestType.values()) {
                if (type != S3MetaRequestOptions.MetaRequestType.GET_OBJECT) {
                    S3MetaRequestOptions options = fixture.options(collector, null)
                            .withResponseBuffer(ByteBuffer.allocateDirect(8193))
                            .withMetaRequestType(type).withOperationName("GetObject");
                    assertThrows(IllegalArgumentException.class, () -> fixture.client.makeMetaRequest(options));
                }
            }
            S3MetaRequestOptions file = fixture.options(collector, null)
                    .withResponseBuffer(ByteBuffer.allocateDirect(8193)).withResponseFilePath(Paths.get("unused"));
            assertThrows(IllegalArgumentException.class, () -> fixture.client.makeMetaRequest(file));
            S3MetaRequestOptions hint = fixture.options(collector, null)
                    .withResponseBuffer(ByteBuffer.allocateDirect(8193)).withObjectSizeHint(8192L);
            assertThrows(IllegalArgumentException.class, () -> fixture.client.makeMetaRequest(hint));
            assertEquals(0, fixture.gets.get());
        }
    }

    @Test
    public void testSnapshotAtSubmissionAndRetainUntilShutdown() throws Exception {
        int size = 8193;
        try (Loopback fixture = new Loopback(size, Behavior.STALL, false, false)) {
            ByteBuffer storage = guarded(size + 64);
            ByteBuffer destination = storage.duplicate();
            Collector collector = new Collector(size);
            S3MetaRequestOptions options = fixture.options(collector, null).withResponseBuffer(destination);
            destination.position(19);
            destination.limit(19 + size);
            S3MetaRequest request = fixture.client.makeMetaRequest(options);
            try {
                assertEquals(19, destination.position());
                assertEquals(19 + size, destination.limit());
                destination.clear();
                options.withResponseBuffer(null);
                assertTrue(fixture.bodyStarted.await(10, TimeUnit.SECONDS));
                assertFalse(collector.finished.isDone());
                assertFalse(request.getShutdownCompleteFuture().isDone());
                fixture.releaseBody.countDown();
                assertEquals(size, collector.finished.get(10, TimeUnit.SECONDS).getResponseBufferLength());
                assertPayload(storage, 19, fixture.data, 0, size);
                assertGuards(storage, 19, 19 + size);
                assertEquals(0, collector.callbacks.get());
                assertFalse(request.getShutdownCompleteFuture().isDone());
            } finally {
                fixture.releaseBody.countDown();
                request.close();
                request.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
            }
        }
    }

    private static WeakReference<ByteBuffer> receiveWithoutBufferOwner(Loopback fixture,
            ReferenceQueue<ByteBuffer> collected, boolean cancel) throws Exception {
        Collector collector = new Collector(8193);
        ByteBuffer destination = ByteBuffer.allocateDirect(8193);
        WeakReference<ByteBuffer> reference = new WeakReference<>(destination, collected);
        S3MetaRequestOptions options = fixture.options(collector, null).withResponseBuffer(destination);
        S3MetaRequest request = fixture.client.makeMetaRequest(options);
        destination = null;
        options.withResponseBuffer(null);
        try {
            assertTrue(fixture.bodyStarted.await(10, TimeUnit.SECONDS));
            System.gc();
            assertNotNull("Native view must keep direct allocation alive", reference.get());
            if (cancel) {
                request.cancel();
            }
            fixture.releaseBody.countDown();
            S3FinishedResponseContext result = collector.finished.get(10, TimeUnit.SECONDS);
            if (cancel) {
                assertNotEquals(0, result.getErrorCode());
                assertEquals(0, result.getResponseBufferLength());
            } else {
                assertEquals(0, result.getErrorCode());
                assertEquals(8193, result.getResponseBufferLength());
                assertPayload(reference.get(), 0, fixture.data, 0, 8193);
            }
            assertEquals(0, collector.callbacks.get());
            assertFalse(request.getShutdownCompleteFuture().isDone());
            System.gc();
            assertNotNull("View stays retained until shutdown, not just finish", reference.get());
        } finally {
            fixture.releaseBody.countDown();
            if (!request.isNull()) {
                request.close();
            }
            request.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
        }
        return reference;
    }

    private static void assertCollected(WeakReference<ByteBuffer> reference,
            ReferenceQueue<ByteBuffer> collected) throws Exception {
        // Run outside the submitting frame so temporary Java views cannot keep the allocation alive.
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        do {
            System.gc();
            if (collected.remove(100) == reference) {
                assertNull(reference.get());
                return;
            }
        } while (System.nanoTime() < deadline);
        fail("Native response-buffer reference must be collectible after request shutdown");
    }

    @Test
    public void testRetainsDirectAllocationUntilShutdownThenCollects() throws Exception {
        try (Loopback fixture = new Loopback(8193, Behavior.STALL, false, false)) {
            ReferenceQueue<ByteBuffer> collected = new ReferenceQueue<>();
            assertCollected(receiveWithoutBufferOwner(fixture, collected, false), collected);
        }
    }

    @Test
    public void testCancelledBufferCollectibleAfterShutdown() throws Exception {
        try (Loopback fixture = new Loopback(8193, Behavior.STALL, false, false)) {
            ReferenceQueue<ByteBuffer> collected = new ReferenceQueue<>();
            assertCollected(receiveWithoutBufferOwner(fixture, collected, true), collected);
        }
    }

    @Test
    public void testRetryOverwritesPartialAttempt() throws Exception {
        for (Behavior behavior : new Behavior[] {Behavior.SHORT_ONCE, Behavior.SHORT_SECOND_ONCE}) {
            int size = behavior == Behavior.SHORT_ONCE ? 8193 : 3 * PART_SIZE + 137;
            try (Loopback fixture = new Loopback(size, behavior, false, false)) {
                Collector collector = new Collector(size);
                ByteBuffer destination = guarded(size + 64);
                destination.position(23);
                destination.limit(23 + size);
                S3FinishedResponseContext result = download(fixture,
                        fixture.options(collector, null).withResponseBuffer(destination), collector);
                assertEquals(0, result.getErrorCode());
                assertEquals(size, result.getResponseBufferLength());
                assertTrue(fixture.gets.get() >= (behavior == Behavior.SHORT_ONCE ? 2 : 5));
                destination.clear();
                assertPayload(destination, 23, fixture.data, 0, size);
                assertGuards(destination, 23, 23 + size);
                assertEquals(0, collector.callbacks.get());
            }
        }
    }

    @Test
    public void testFailureLengthAndBounds() throws Exception {
        for (Behavior behavior : new Behavior[] {Behavior.NORMAL, Behavior.SHORT_ALWAYS, Behavior.ERROR}) {
            try (Loopback fixture = new Loopback(8193, behavior, false, false)) {
                int capacity = behavior == Behavior.NORMAL ? 8192 : 8193;
                ByteBuffer storage = guarded(capacity + 64);
                ByteBuffer destination = storage.duplicate();
                destination.position(23);
                destination.limit(23 + capacity);
                Collector collector = new Collector(8193);
                S3FinishedResponseContext result = download(fixture,
                        fixture.options(collector, null).withResponseBuffer(destination), collector);
                assertNotEquals(0, result.getErrorCode());
                assertEquals(0, result.getResponseBufferLength());
                assertEquals(0, collector.callbacks.get());
                assertGuards(storage, 23, 23 + capacity);
            }
        }
    }

    @Test
    public void testCancelAndCloseWithBodyInFlight() throws Exception {
        checkCancelledBufferReuse(8193, 0);
    }

    @Test
    public void testMultipartCancelAndCloseWithIndependentBodiesInFlight() throws Exception {
        checkCancelledBufferReuse(3 * PART_SIZE, PART_SIZE, 2 * PART_SIZE);
    }

    private static void checkCancelledBufferReuse(int size, int... gatedOffsets) throws Exception {
        for (boolean closeImmediately : new boolean[] {false, true}) {
            BodyGate[] gates = new BodyGate[gatedOffsets.length];
            for (int i = 0; i < gates.length; ++i) {
                gates[i] = new BodyGate(gatedOffsets[i]);
            }
            try (Loopback fixture = new Loopback(size, Behavior.NORMAL, false, false, gates);
                    Loopback replacement = new Loopback(size)) {
                for (int i = 0; i < size; ++i) {
                    replacement.data[i] ^= 0x5a;
                }
                ByteBuffer storage = guarded(size + 64);
                ByteBuffer destination = storage.duplicate();
                destination.position(23);
                destination.limit(23 + size);
                Collector collector = new Collector(size);
                S3MetaRequest request = fixture.client.makeMetaRequest(
                        fixture.options(collector, null).withResponseBuffer(destination));
                try {
                    for (BodyGate gate : gates) {
                        assertTrue("Body started at " + gate.offset, gate.started.await(10, TimeUnit.SECONDS));
                    }
                    assertFalse(collector.finished.isDone());
                    if (closeImmediately) {
                        request.close();
                    } else {
                        request.cancel();
                    }
                    S3FinishedResponseContext result = collector.finished.get(10, TimeUnit.SECONDS);
                    assertNotEquals(0, result.getErrorCode());
                    assertEquals(0, result.getResponseBufferLength());
                    assertEquals(0, collector.callbacks.get());
                } finally {
                    if (!request.isNull()) {
                        request.close();
                    }
                    request.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
                }
                assertGuards(storage, 23, 23 + size);
                for (BodyGate gate : gates) {
                    assertEquals("Old body still gated after shutdown", 1, gate.finished.getCount());
                    assertEquals(1, gate.tailAttempted.getCount());
                }
                // Reuse only after shutdown; every replacement byte differs from the old response.
                Collector second = new Collector(size);
                S3FinishedResponseContext reused = download(replacement, replacement.options(second, null)
                        .withResponseBuffer(destination), second);
                assertEquals(0, reused.getErrorCode());
                assertEquals(size, reused.getResponseBufferLength());
                assertPayload(storage, 23, replacement.data, 0, size);
                assertGuards(storage, 23, 23 + size);
                // Release multipart tails separately and in reverse order, after the replacement has finished.
                for (int i = gates.length - 1; i >= 0; --i) {
                    BodyGate gate = gates[i];
                    gate.release.countDown();
                    assertTrue("Old tail attempted at " + gate.offset, gate.tailAttempted.await(10, TimeUnit.SECONDS));
                    assertTrue("Old handler finished at " + gate.offset, gate.finished.await(10, TimeUnit.SECONDS));
                    long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(250);
                    do {
                        assertPayload(storage, 23, replacement.data, 0, size);
                        assertGuards(storage, 23, 23 + size);
                        Thread.sleep(10);
                    } while (System.nanoTime() < deadline);
                    for (int j = 0; j < i; ++j) {
                        assertEquals("Other old tails remain independently gated", 1, gates[j].tailAttempted.getCount());
                    }
                }
                assertEquals(0, collector.callbacks.get());
                assertEquals(0, second.callbacks.get());
            }
        }
    }

    private static final class BorrowedCollector extends Collector implements S3BorrowedBufferDelivery {
        final AtomicLong borrowedBytes = new AtomicLong();

        BorrowedCollector(int size) {
            super(size);
        }

        @Override
        public int onResponseBodyBorrowed(S3BorrowedBuffer buffer, long start, long end) {
            try (S3BorrowedBuffer owned = buffer) {
                ByteBuffer body = owned.asByteBuffer();
                int count = body.remaining();
                body.get(copied, (int) start, count);
                borrowedBytes.addAndGet(count);
                return count;
            }
        }
    }

    @Test
    public void testRejectPoolAndBorrowedDeliveryAndPreserveBothPaths() throws Exception {
        for (boolean pool : new boolean[] {false, true}) {
            try (Loopback fixture = new Loopback(8193, Behavior.NORMAL, pool, false)) {
                Collector collector = pool ? new Collector(8193) : new BorrowedCollector(8193);
                S3MetaRequestOptions options = fixture.options(collector, null)
                        .withResponseBuffer(ByteBuffer.allocateDirect(8193));
                assertThrows(IllegalArgumentException.class, () -> fixture.client.makeMetaRequest(options));
                assertEquals(0, fixture.gets.get());
                options.withResponseBuffer(null);
                assertEquals(0, download(fixture, options, collector).getResponseBufferLength());
                assertEquals(8193, collector.heapBytes.get());
                assertArrayEquals(fixture.data, collector.copied);
                if (pool) {
                    BorrowedCollector borrowed = new BorrowedCollector(8193);
                    S3FinishedResponseContext result = download(fixture, fixture.options(borrowed, null), borrowed);
                    assertEquals(0, result.getErrorCode());
                    assertEquals(0, result.getResponseBufferLength());
                    assertEquals(8193, borrowed.borrowedBytes.get());
                    assertEquals(0, borrowed.heapBytes.get());
                    assertArrayEquals(fixture.data, borrowed.copied);
                }
            }
        }
    }

    @Test
    public void testBackpressureAndFileDeliveryUnchanged() throws Exception {
        try (Loopback fixture = new Loopback(3 * PART_SIZE + 137, Behavior.NORMAL, false, true)) {
            Collector collector = new Collector(fixture.data.length);
            ByteBuffer destination = ByteBuffer.allocateDirect(fixture.data.length);
            assertEquals(fixture.data.length, download(fixture, fixture.options(collector, null)
                    .withResponseBuffer(destination), collector).getResponseBufferLength());
            assertPayload(destination, 0, fixture.data, 0, fixture.data.length);
            assertEquals(0, collector.callbacks.get());
        }
        Path path = Files.createTempFile("crt-response-buffer-", ".bin");
        try (Loopback fixture = new Loopback(8193)) {
            Collector collector = new Collector(8193);
            S3FinishedResponseContext result = download(fixture,
                    fixture.options(collector, null).withResponseFilePath(path), collector);
            assertEquals(0, result.getErrorCode());
            assertEquals(0, result.getResponseBufferLength());
            assertArrayEquals(fixture.data, Files.readAllBytes(path));
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    public void testConcurrentDisjointViewsCompleteOutOfOrder() throws Exception {
        int length = 8193;
        try (Loopback fixture = new Loopback(30000, Behavior.STALL_FIRST, false, false)) {
            ByteBuffer storage = guarded(2 * length + 96);
            ByteBuffer first = storage.duplicate();
            first.position(23);
            first.limit(23 + length);
            ByteBuffer second = storage.duplicate();
            second.position(length + 55);
            second.limit(2 * length + 55);
            Collector firstCollector = new Collector(30000);
            Collector secondCollector = new Collector(30000);
            S3MetaRequest firstRequest = fixture.client.makeMetaRequest(
                    fixture.options(firstCollector, "bytes=0-8192").withResponseBuffer(first));
            try {
                assertTrue(fixture.bodyStarted.await(10, TimeUnit.SECONDS));
                S3FinishedResponseContext secondResult = download(fixture,
                        fixture.options(secondCollector, "bytes=10000-18192").withResponseBuffer(second), secondCollector);
                assertEquals(0, secondResult.getErrorCode());
                assertEquals(length, secondResult.getResponseBufferLength());
                assertFalse(firstCollector.finished.isDone());
                fixture.releaseBody.countDown();
                S3FinishedResponseContext firstResult = firstCollector.finished.get(10, TimeUnit.SECONDS);
                assertEquals(0, firstResult.getErrorCode());
                assertEquals(length, firstResult.getResponseBufferLength());
                assertPayload(storage, 23, fixture.data, 0, length);
                assertPayload(storage, length + 55, fixture.data, 10000, length);
                assertEquals(0, firstCollector.callbacks.get());
                assertEquals(0, secondCollector.callbacks.get());
                for (int i = 0; i < storage.capacity(); ++i) {
                    if (i < 23 || (i >= length + 23 && i < length + 55) || i >= 2 * length + 55) {
                        assertEquals("Guard at " + i, GUARD, storage.get(i));
                    }
                }
            } finally {
                fixture.releaseBody.countDown();
                firstRequest.close();
                firstRequest.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
            }
        }
    }

    @Test
    public void testClientCloseDuringReceive() throws Exception {
        try (Loopback fixture = new Loopback(8193, Behavior.STALL, false, false)) {
            ByteBuffer destination = guarded(8193);
            Collector collector = new Collector(8193);
            S3MetaRequest request = fixture.client.makeMetaRequest(
                    fixture.options(collector, null).withResponseBuffer(destination));
            try {
                assertTrue(fixture.bodyStarted.await(10, TimeUnit.SECONDS));
                fixture.client.close();
                assertFalse(fixture.client.getShutdownCompleteFuture().isDone());
                fixture.releaseBody.countDown();
                S3FinishedResponseContext result = collector.finished.get(10, TimeUnit.SECONDS);
                assertEquals(0, result.getErrorCode());
                assertEquals(8193, result.getResponseBufferLength());
                assertPayload(destination, 0, fixture.data, 0, 8193);
            } finally {
                fixture.releaseBody.countDown();
                request.close();
                request.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
            }
            fixture.client.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testNativeSubmissionFailureReleasesRequestCredentialsProvider() throws Exception {
        try (Loopback fixture = new Loopback(16)) {
            DelegateCredentialsProvider provider = new DelegateCredentialsProvider.DelegateCredentialsProviderBuilder()
                    .withHandler(Credentials::createAnonymousCredentials).build();
            try {
                S3MetaRequestOptions options = fixture.options(new Collector(16), null)
                        .withResponseBuffer(ByteBuffer.allocateDirect(16)).withCredentialsProvider(provider);
                options.getHttpRequest().setEncodedPath("/object?partNumber=1");
                assertThrows(CrtRuntimeException.class, () -> fixture.client.makeMetaRequest(options));
                assertEquals(0, fixture.gets.get());
                provider.close();
                assertTrue("Temporary signing config must release its provider reference", provider.isNull());
                provider.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
            } finally {
                // Drain the leaked reference too if the regression assertion fails.
                if (!provider.isNull()) {
                    provider.close();
                }
                provider.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
            }
        }
    }

    @Test
    public void testNativeSubmissionFailurePreservesCallerSigningConfig() throws Exception {
        try (Loopback fixture = new Loopback(16)) {
            DelegateCredentialsProvider provider = new DelegateCredentialsProvider.DelegateCredentialsProviderBuilder()
                    .withHandler(Credentials::createAnonymousCredentials).build();
            try (AwsSigningConfig signing = AwsSigningConfig.getDefaultS3SigningConfig("us-east-1", provider)) {
                provider.close();
                Collector collector = new Collector(16);
                S3MetaRequestOptions options = fixture.options(collector, null)
                        .withResponseBuffer(ByteBuffer.allocateDirect(16)).withSigningConfig(signing);
                options.getHttpRequest().setEncodedPath("/object?partNumber=1");
                assertThrows(CrtRuntimeException.class, () -> fixture.client.makeMetaRequest(options));
                assertFalse("Caller signing config must retain its provider reference", provider.isNull());
                assertFalse(provider.getShutdownCompleteFuture().isDone());
                assertEquals(0, fixture.gets.get());
                options.getHttpRequest().setEncodedPath("/object");
                S3FinishedResponseContext result = download(fixture, options, collector);
                assertEquals(0, result.getErrorCode());
                assertEquals(16, result.getResponseBufferLength());
                assertFalse("Successful submission must also preserve caller ownership", provider.isNull());
            }
            assertTrue(provider.isNull());
            provider.getShutdownCompleteFuture().get(10, TimeUnit.SECONDS);
        }
    }

    @Test
    public void testNativeSubmissionFailureReleasesBufferAndAllowsReuse() throws Exception {
        try (Loopback fixture = new Loopback(8193)) {
            ByteBuffer destination = guarded(8193);
            Collector collector = new Collector(8193);
            S3MetaRequestOptions options = fixture.options(collector, null).withResponseBuffer(destination);
            options.getHttpRequest().setEncodedPath("/object?partNumber=1");
            assertThrows(CrtRuntimeException.class, () -> fixture.client.makeMetaRequest(options));
            assertEquals(0, fixture.gets.get());
            assertGuards(destination, 0, 0);
            options.getHttpRequest().setEncodedPath("/object");
            options.withObjectSizeHint(8193L);
            S3FinishedResponseContext result = download(fixture, options, collector);
            assertEquals(0, result.getErrorCode());
            assertEquals(8193, result.getResponseBufferLength());
            assertPayload(destination, 0, fixture.data, 0, 8193);
        }
    }

    @Test
    public void testChecksumAndHeaderCallbackFailureReportZeroLength() throws Exception {
        try (Loopback fixture = new Loopback(8193, Behavior.BAD_CHECKSUM, false, false)) {
            Collector collector = new Collector(8193);
            S3FinishedResponseContext result = download(fixture, fixture.options(collector, null)
                    .withResponseBuffer(ByteBuffer.allocateDirect(8193))
                    .withChecksumConfig(new ChecksumConfig().withValidateChecksum(true)), collector);
            assertNotEquals(0, result.getErrorCode());
            assertEquals(0, result.getResponseBufferLength());
            assertEquals(0, collector.callbacks.get());
        }
        try (Loopback fixture = new Loopback(8193)) {
            RuntimeException failure = new RuntimeException("Test header callback failure");
            Collector collector = new Collector(8193) {
                @Override
                public void onResponseHeaders(int status, HttpHeader[] headers) {
                    throw failure;
                }
            };
            S3FinishedResponseContext result = download(fixture, fixture.options(collector, null)
                    .withResponseBuffer(ByteBuffer.allocateDirect(8193)), collector);
            assertNotEquals(0, result.getErrorCode());
            assertSame(failure, result.getCause());
            assertEquals(0, result.getResponseBufferLength());
            assertEquals(0, collector.callbacks.get());
        }
    }

    @Test
    public void testLegacyHeaderCallbackFailure() throws Exception {
        try (Loopback fixture = new Loopback(8193)) {
            RuntimeException failure = new RuntimeException("Test legacy header callback failure");
            Collector collector = new Collector(8193) {
                @Override
                public void onResponseHeaders(int status, HttpHeader[] headers) {
                    throw failure;
                }
            };
            S3FinishedResponseContext result = download(fixture, fixture.options(collector, null), collector);
            assertNotEquals(0, result.getErrorCode());
            assertSame(failure, result.getCause());
        }
    }

    @Test
    public void testMultipartDestinationOneByteShort() throws Exception {
        int size = 3 * PART_SIZE + 137;
        try (Loopback fixture = new Loopback(size)) {
            ByteBuffer storage = guarded(size + 64);
            ByteBuffer destination = storage.duplicate();
            destination.position(23);
            destination.limit(23 + size - 1);
            Collector collector = new Collector(size);
            S3FinishedResponseContext result = download(fixture,
                    fixture.options(collector, null).withResponseBuffer(destination), collector);
            assertNotEquals(0, result.getErrorCode());
            assertEquals(0, result.getResponseBufferLength());
            assertEquals(0, collector.callbacks.get());
            assertGuards(storage, 23, 23 + size - 1);
        }
    }

    @Test
    public void testCompleteHttpBodyShorterThanS3RangeFails() throws Exception {
        int size = 3 * PART_SIZE + 137;
        try (Loopback fixture = new Loopback(size, Behavior.SHORT_DECLARED, false, false)) {
            ByteBuffer storage = guarded(size + 64);
            ByteBuffer destination = storage.duplicate();
            destination.position(23);
            destination.limit(23 + size);
            Collector collector = new Collector(size);
            S3FinishedResponseContext result = download(fixture,
                    fixture.options(collector, null).withResponseBuffer(destination), collector);
            assertNotEquals("A complete HTTP body must still cover the requested S3 range", 0, result.getErrorCode());
            assertEquals(0, result.getResponseBufferLength());
            assertEquals(0, collector.callbacks.get());
            assertGuards(storage, 23, 23 + size);
        }
    }
}
