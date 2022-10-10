package software.amazon.awssdk.crt.http;

public class HttpManagerMetrics {
    private final long availableConcurrency;
    private final long pendingConcurrencyAcquires;

    HttpManagerMetrics(long availableConcurrency, long pendingConcurrencyAcquires) {
        this.availableConcurrency = availableConcurrency;
        this.pendingConcurrencyAcquires = pendingConcurrencyAcquires;
    }

    /**
     * The number of additional concurrent requests that can be supported by the HTTP manager without needing to
     * establish additional connections to the target server.
     *
     * For connection manager, it equals to connections that's idle.
     * For stream manager, it equals to the number of streams that are possible to be made without creating new
     * connection, although the implementation can create new connection without fully filling it.
     */
    public long getAvailableConcurrency() {
        return availableConcurrency;
    }

    /**
     * The number of requests that are awaiting concurrency to be made available from the HTTP manager.
     */
    public long getPendingConcurrencyAcquires() {
        return pendingConcurrencyAcquires;
    }
}
