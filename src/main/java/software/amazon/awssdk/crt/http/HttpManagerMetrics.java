package software.amazon.awssdk.crt.http;

public class HttpManagerMetrics {
    private final long availableConcurrency;
    private final long pendingConcurrencyAcquires;

    HttpManagerMetrics(long availableConcurrency, long pendingConcurrencyAcquires) {
        this.availableConcurrency = availableConcurrency;
        this.pendingConcurrencyAcquires = pendingConcurrencyAcquires;
    }

    /**
     * @return The number of additional concurrent requests that can be supported by the HTTP manager without needing to
     * establish additional connections to the target server.
     *
     * For connection manager, this value represents idle connections.
     * For stream manager, this value represents the number of streams that are possible to be made without creating new
     * connections, although the implementation can create new connection without fully filling it.
     */
    public long getAvailableConcurrency() {
        return availableConcurrency;
    }

    /**
     * @return The number of requests that are awaiting concurrency to be made available from the HTTP manager.
     */
    public long getPendingConcurrencyAcquires() {
        return pendingConcurrencyAcquires;
    }
}
