/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

/*
 * Deliberately in software.amazon.awssdk.crt.s3 rather than the usual
 * ...crt.test package: the pool's accessors and allocation surface
 * (ceilingBytes, committedBytes, partSize, ceilingClamped, tryAcquire,
 * release, trim) are package-private, and reaching them here avoids either
 * widening production visibility or writing reflection into the tests.
 * CrtTestFixture is public with protected helpers, so extending it across
 * packages still works.
 */
package software.amazon.awssdk.crt.s3;

import org.junit.Assume;
import org.junit.Test;

import software.amazon.awssdk.crt.test.CrtTestFixture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for the S3 direct buffer pool.
 *
 * <p>Every test here needs neither credentials nor network, so they run in
 * every CI job on every platform we support. They cover the validation,
 * growth and trim rules that a pool configured for the happy path never
 * exercises.</p>
 */
public class S3DirectBufferPoolTest extends CrtTestFixture {

    private static final String REGION = "us-west-2";

    private static final int PART_SIZE = 8 * 1024 * 1024;
    private static final long MIB = 1024 * 1024;

    public S3DirectBufferPoolTest() {
    }

    /* ==================================================================== */
    /* Group A: configuration and validation. No credentials, no network.   */
    /* ==================================================================== */

    /*
     * A ceiling this large cannot fit within 80% of any plausible direct
     * memory limit, so the test doesn't need to know the actual limit to
     * know the check must reject it. Which limit applies is platform
     * dependent: -XX:MaxDirectMemorySize on a normal JVM, and the app's
     * heap growth limit on Android, where allocateDirect allocates on the
     * Java heap.
     */
    @Test
    public void testFixedCeilingBeyondDirectMemoryIsRejected() {
        S3ClientOptions options = baseOptions()
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.fixed(1024L * 1024 * MIB)); // 1 PiB

        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> S3DirectBufferPool.fromOptions(
                        options.getDirectBufferPoolOptions(), options));
        // The message has to name the limit it checked and a lever the
        // customer can actually change, or they cannot act on it. Android
        // has no -XX:MaxDirectMemorySize, so the pool's own ceiling is the
        // only lever there.
        assertTrue("error should name the limit it checked, was: " + ex.getMessage(),
                ex.getMessage().contains("MaxDirectMemorySize")
                        || ex.getMessage().contains("heap growth limit"));
        assertTrue("error should name a lever the customer can change, was: " + ex.getMessage(),
                ex.getMessage().contains("withMemoryLimitInBytes"));
    }

    /*
     * The client's memory limit and the pool's ceiling are one budget, not two.
     * A ceiling above the limit must fail at construction rather than letting
     * the pool exceed a limit the customer set.
     */
    @Test
    public void testCeilingAboveClientMemoryLimitIsRejected() {
        S3ClientOptions options = baseOptions()
                .withMemoryLimitInBytes(64 * MIB)
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.fixed(256 * MIB));

        assertThrows(IllegalArgumentException.class,
                () -> S3DirectBufferPool.fromOptions(options.getDirectBufferPoolOptions(), options));
    }

    @Test
    public void testNegativeSizesAreRejected() {
        S3ClientOptions negativeLimit = baseOptions()
                .withMemoryLimitInBytes(-1)
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.auto());
        assertThrows(IllegalArgumentException.class,
                () -> S3DirectBufferPool.fromOptions(negativeLimit.getDirectBufferPoolOptions(), negativeLimit));

        S3ClientOptions negativePart = new S3ClientOptions()
                .withRegion(REGION)
                .withPartSize(-1)
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.auto());
        assertThrows(IllegalArgumentException.class,
                () -> S3DirectBufferPool.fromOptions(negativePart.getDirectBufferPoolOptions(), negativePart));

        // The factories themselves reject nonsense before a client is involved.
        assertThrows(IllegalArgumentException.class, () -> S3DirectBufferPoolOptions.fixed(-1));
        assertThrows(IllegalArgumentException.class, () -> S3DirectBufferPoolOptions.elastic(-1, 256 * MIB));
    }

    @Test
    public void testElasticFloorAboveCeilingIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> S3DirectBufferPoolOptions.elastic(256 * MIB, 64 * MIB));
    }

    /* A ceiling that cannot hold even one part is unusable, so it must fail
     * rather than produce a pool that can never serve a request. */
    @Test
    public void testCeilingSmallerThanOnePartIsRejected() {
        S3ClientOptions options = baseOptions()
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.fixed(PART_SIZE / 2));

        assertThrows(IllegalArgumentException.class,
                () -> S3DirectBufferPool.fromOptions(options.getDirectBufferPoolOptions(), options));
    }

    /*
     * The pool allocates whole parts, so a ceiling between two multiples has to
     * round down. Rounding up would let the pool exceed a limit the customer set.
     */
    @Test
    public void testCeilingRoundsDownToWholeParts() {
        long requested = (3 * (long) PART_SIZE) + (PART_SIZE / 3); // 3 and a bit parts
        S3ClientOptions options = baseOptions()
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.fixed(requested));

        S3DirectBufferPool pool =
                S3DirectBufferPool.fromOptions(options.getDirectBufferPoolOptions(), options);
        try {
            assertEquals(PART_SIZE, pool.partSize());
            assertEquals(3L * PART_SIZE, pool.ceilingBytes());
            assertTrue("rounded ceiling must not exceed the requested size",
                    pool.ceilingBytes() <= requested);
        } finally {
            pool.close();
        }
    }

    /*
     * auto() derives its ceiling from the throughput target and, unlike an
     * explicit size, reduces it to fit the JVM's direct memory rather than
     * failing. Asserted as a biconditional against the derived default so the
     * test holds on any machine without needing to read the actual limit:
     * either the default fit and nothing was clamped, or it didn't and the
     * ceiling came down.
     */
    @Test
    public void testAutoClampsCeilingRatherThanFailing() {
        double throughputGbps = 400.0; // high enough to derive a large default
        long derivedDefault = S3Client.defaultMemoryLimitForThroughput(throughputGbps);
        Assume.assumeTrue("native default sizing unavailable", derivedDefault > 0);

        S3ClientOptions options = new S3ClientOptions()
                .withRegion(REGION)
                .withPartSize(PART_SIZE)
                .withThroughputTargetGbps(throughputGbps)
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.auto());

        S3DirectBufferPool pool =
                S3DirectBufferPool.fromOptions(options.getDirectBufferPoolOptions(), options);
        try {
            assertTrue("ceiling must never exceed the derived default",
                    pool.ceilingBytes() <= derivedDefault);
            assertEquals("ceilingClamped must say exactly whether the ceiling was reduced",
                    pool.ceilingBytes() < derivedDefault, pool.ceilingClamped());
        } finally {
            pool.close();
        }
    }

    /*
     * An explicit -XX:MaxDirectMemorySize=0 allows no direct memory at all, so a
     * pool cannot be created.
     *
     * The gate detects that JVM rather than trusting a property somebody has to
     * remember to set: a one-byte direct allocation fails if and only if the flag
     * is in force. The previous property-based gate named a property no job or
     * profile ever set, so this test always skipped while appearing to cover the
     * path.
     *
     * NOTE: it still cannot run in this project today. pom.xml sets forkCount=0,
     * so tests share the Maven JVM, and Maven's own launcher needs direct buffers
     * to start -- MAVEN_OPTS=-XX:MaxDirectMemorySize=0 kills Maven before any test
     * runs. Covering this path requires a forked test JVM.
     */
    @Test
    public void testZeroDirectMemoryLimitIsRejected() {
        Assume.assumeTrue("needs a JVM started with -XX:MaxDirectMemorySize=0",
                directMemoryIsForbidden());

        S3ClientOptions options = baseOptions()
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.auto());
        IllegalStateException ex =
                assertThrows(IllegalStateException.class, () -> S3DirectBufferPool.fromOptions(
                        options.getDirectBufferPoolOptions(), options));
        assertTrue("error should explain that no direct memory is allowed, was: " + ex.getMessage(),
                ex.getMessage().contains("MaxDirectMemorySize"));
    }

    /* ==================================================================== */
    /* Group A2: allocation, growth and trim. No credentials, no network.   */
    /*                                                                     */
    /* tryAcquire/release/trim/close are the pool's whole allocation        */
    /* surface and are called only from native, so a transfer is the only   */
    /* other way to reach them -- which makes their coverage credential-    */
    /* gated and indirect. Driving them here asserts the ceiling, growth    */
    /* and trim rules directly. A small part size keeps each block at       */
    /* BLOCK_BYTES so these run anywhere, whatever the JVM's direct memory  */
    /* limit.                                                               */
    /* ==================================================================== */

    /** 64 KiB: well under a real part, so the whole test pool is a few MiB. */
    private static final int SMALL_PART = 64 * 1024;
    /** One block of SMALL_PART slots -- the granularity the pool allocates in. */
    private static final long BLOCK_BYTES = (long) S3DirectBufferPool.BLOCK_SLOTS * SMALL_PART;

    /*
     * Growth is per block, not per part, and stops dead at the ceiling. The
     * per-block jump matters to a customer sizing a pool: committing one part
     * commits a whole block's worth of memory.
     */
    @Test
    public void testElasticPoolGrowsBlockByBlockToTheCeilingThenRefuses() {
        S3DirectBufferPool pool = elasticPool(0, 2 * BLOCK_BYTES);
        try {
            assertEquals(2 * BLOCK_BYTES, pool.ceilingBytes());
            assertEquals("a pool with no floor commits nothing until something is acquired",
                    0, pool.committedBytes());

            List<Long> leases = new ArrayList<>();
            leases.add(acquireOnePart(pool));
            assertEquals("the first acquire backs a whole block, not one part",
                    BLOCK_BYTES, pool.committedBytes());

            // Fill the rest of block 0. Still one block: growth is driven by
            // running out of slots, not by the number of leases.
            while (leases.size() < S3DirectBufferPool.BLOCK_SLOTS) {
                leases.add(acquireOnePart(pool));
            }
            assertEquals(BLOCK_BYTES, pool.committedBytes());

            // Block 0 is full, so this one has to grow the pool.
            leases.add(acquireOnePart(pool));
            assertEquals("a full block must grow the pool, not fail",
                    2 * BLOCK_BYTES, pool.committedBytes());

            while (leases.size() < 2 * S3DirectBufferPool.BLOCK_SLOTS) {
                leases.add(acquireOnePart(pool));
            }
            assertEquals(2 * BLOCK_BYTES, pool.committedBytes());

            // At the ceiling: refuse rather than exceed it. Native pends its
            // future on EXHAUSTED and retries when memory frees.
            assertEquals("acquiring past the ceiling must report EXHAUSTED",
                    S3DirectBufferPool.EXHAUSTED, pool.tryAcquire(SMALL_PART));
            assertEquals("a refused acquire must not commit anything",
                    2 * BLOCK_BYTES, pool.committedBytes());

            // A release makes room without changing what is committed: the
            // block stays backed and its slot is reused.
            pool.release(leases.remove(leases.size() - 1));
            leases.add(acquireOnePart(pool));
            assertEquals(2 * BLOCK_BYTES, pool.committedBytes());

            for (long lease : leases) {
                pool.release(lease);
            }
        } finally {
            pool.close();
        }
        // close() frees every unbacked block, including the ones growth added.
        assertEquals("close must give all pool memory back", 0, pool.committedBytes());
    }

    /*
     * trim() is what gives idle memory back, and it has two bounds that are
     * easy to get wrong in opposite directions: it must not free a block that
     * still has a lease (use-after-free), and it must not free below the floor
     * (the floor is the memory the customer asked to keep allocated).
     */
    @Test
    public void testTrimFreesAboveTheFloorButNotLeasedBlocksOrTheFloor() {
        S3DirectBufferPool pool = elasticPool(BLOCK_BYTES, 2 * BLOCK_BYTES);
        try {
            assertEquals("the floor is allocated when the pool is created",
                    BLOCK_BYTES, pool.committedBytes());

            List<Long> floorLeases = new ArrayList<>();
            while (floorLeases.size() < S3DirectBufferPool.BLOCK_SLOTS) {
                floorLeases.add(acquireOnePart(pool));
            }
            long aboveFloor = acquireOnePart(pool); // grows past the floor
            assertEquals(2 * BLOCK_BYTES, pool.committedBytes());

            pool.trim();
            assertEquals("trim must not free a block with a lease outstanding",
                    2 * BLOCK_BYTES, pool.committedBytes());

            pool.release(aboveFloor);
            pool.trim();
            assertEquals("trim must free an unused block above the floor",
                    BLOCK_BYTES, pool.committedBytes());

            // Completely idle now, so this is the case where trim could wrongly
            // free the floor as well.
            for (long lease : floorLeases) {
                pool.release(lease);
            }
            pool.trim();
            assertEquals("the floor is kept even when the pool is completely idle",
                    BLOCK_BYTES, pool.committedBytes());
        } finally {
            pool.close();
        }
        // Only shutdown releases the floor.
        assertEquals("close must free the floor too", 0, pool.committedBytes());
    }

    /**
     * An elastic pool over {@link #SMALL_PART} slots, built without a client
     * so no credentials or native client are involved.
     */
    private static S3DirectBufferPool elasticPool(long floorBytes, long ceilingBytes) {
        S3ClientOptions options = new S3ClientOptions()
                .withRegion(REGION)
                .withPartSize(SMALL_PART)
                .withDirectBufferPoolOptions(S3DirectBufferPoolOptions.elastic(floorBytes, ceilingBytes));
        return S3DirectBufferPool.fromOptions(options.getDirectBufferPoolOptions(), options);
    }

    /** One part, asserting it was actually leased (handles are {@code >= 0}). */
    private static long acquireOnePart(S3DirectBufferPool pool) {
        long handle = pool.tryAcquire(SMALL_PART);
        assertTrue("expected a lease handle, got " + handle, handle >= 0);
        return handle;
    }
    /* ==================================================================== */
    /* Helpers                                                              */
    /* ==================================================================== */

    /** Options with everything the pool's sizing needs and nothing else. */
    private static S3ClientOptions baseOptions() {
        return new S3ClientOptions().withRegion(REGION).withPartSize(PART_SIZE);
    }

    /**
     * True only in a JVM that allows no direct memory at all, which is what
     * -XX:MaxDirectMemorySize=0 produces. Probing beats trusting a property:
     * the answer cannot drift out of sync with how the JVM was launched.
     */
    private static boolean directMemoryIsForbidden() {
        try {
            ByteBuffer probe = ByteBuffer.allocateDirect(1);
            return probe.capacity() != 1;
        } catch (OutOfMemoryError expectedWhenForbidden) {
            return true;
        }
    }
}
