package software.amazon.awssdk.crt.http;

public class HttpManagerMetrics {
    private final long availableConcurrency;
    private final long pendingConcurrencyAcquires;
    private final long leasedConcurrency;

    HttpManagerMetrics(long availableConcurrency, long pendingConcurrencyAcquires, long leasedConcurrency) {
        this.availableConcurrency = availableConcurrency;
        this.pendingConcurrencyAcquires = pendingConcurrencyAcquires;
        this.leasedConcurrency = leasedConcurrency;
    }

    /**
     * @return The number of additional concurrent requests that can be supported by the HTTP manager without needing to
     * establish additional connections to the target server.
     * <p>
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

    /**
     * @return the amount of concurrency units currently out for lease. For http 1.1 this will be connections while
     * for http2 this will be number of streams leased out.
     */
    public long getLeasedConcurrency() {
        return this.leasedConcurrency;
    }
}
