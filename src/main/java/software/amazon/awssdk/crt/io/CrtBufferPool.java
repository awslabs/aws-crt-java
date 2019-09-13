package software.amazon.awssdk.crt.io;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import software.amazon.awssdk.crt.CrtResource;


/**
 * A Pool of CrtByteBuffers
 */
public class CrtBufferPool extends CrtResource {
    private final int numBuffers;
    private final int bufferSize;

    private final Queue<CrtByteBuffer> idleBuffers = new ConcurrentLinkedQueue<>();
    private final Queue<CompletableFuture<CrtByteBuffer>> bufferAcquisitionRequests = new ConcurrentLinkedQueue<>();
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public CrtBufferPool(int numBuffers, int bufferSize) {
        this.numBuffers = numBuffers;
        this.bufferSize = bufferSize;

        for (int i = 0; i < numBuffers; i++) {
            idleBuffers.add(own(CrtByteBuffer.alloc(bufferSize).withPool(this)));
        }
    }

    private synchronized void completeFutureIfPossible() {
        // Peek at the front of each Queue
        CompletableFuture<CrtByteBuffer> bufferRequest = bufferAcquisitionRequests.peek();
        CrtByteBuffer idleBuffer = idleBuffers.peek();

        // If either is null, do nothing.
        if (bufferRequest == null || idleBuffer == null) {
            return;
        }

        // If both are present, remove each from the front of the queue
        bufferAcquisitionRequests.remove();
        idleBuffers.remove();

        // And complete the request.
        bufferRequest.complete(idleBuffer);
    }

    /**
     * Acquires a CrtByteBuffer from this Buffer Pool. When the object using this buffer is done with it,
     * the buffer should be released back into this Pool.
     * @return A Future for a CrtByteBuffer
     */
    public CompletableFuture<CrtByteBuffer> acquireBuffer() {
        if (isClosed.get()) {
            throw new IllegalStateException("CrtBufferPool has been closed, can't acquire new Buffers");
        }

        CompletableFuture<CrtByteBuffer> bufferRequest = new CompletableFuture<>();

        // Add bufferRequest to end of the queue
        bufferAcquisitionRequests.add(bufferRequest);


        completeFutureIfPossible();
        return bufferRequest;
    }

    public void releaseBuffer(CrtByteBuffer idleBuffer) {
        idleBuffer.wipe();
        idleBuffers.add(idleBuffer);

        // Add idleBuffer to end of the queue
        completeFutureIfPossible();
    }

    private void closePendingAcquisitions(Throwable throwable) {
        while (bufferAcquisitionRequests.size() > 0) {
            // Remove and complete future from connectionAcquisitionRequests Queue
            CompletableFuture<CrtByteBuffer> future = bufferAcquisitionRequests.poll();
            if (future != null) {
                future.completeExceptionally(throwable);
            }
        }
    }

    @Override
    public void close() {
        isClosed.set(true);

        closePendingAcquisitions(new RuntimeException("CrtBufferPool is Closing. Closing Pending Buffer Acquisitions."));

        if (idleBuffers.size() != numBuffers) {
            throw new IllegalStateException("Can't close CrtBufferPool yet since some buffers are still in use");
        }

        super.close();
    }
}
