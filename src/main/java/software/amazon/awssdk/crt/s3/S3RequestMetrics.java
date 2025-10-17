package software.amazon.awssdk.crt.s3;

/**
 * Metrics collected upon completion of an S3 Request
 */
public class S3RequestMetrics {
    private long startTimestampNs;
    private long endTimestampNs;
    private long totalDurationNs;
    private long sendStartTimestampNs;
    private long sendEndTimestampNs;
    private long sendingDurationNs;
    private long receiveStartTimestampNs;
    private long receiveEndTimestampNs;
    private long receivingDurationNs;
    private long signStartTimestampNs;
    private long signEndTimestampNs;
    private long signingDurationNs;
    private long memAcquireStartTimestampNs;
    private long memAcquireEndTimestampNs;
    private long memAcquireDurationNs;
    private long deliverStartTimestampNs;
    private long deliverEndTimestampNs;
    private long deliverDurationNs;

    // Request/Response info metrics
    private int responseStatus;
    private String requestId;
//    private String extendedRequestId;
    private String operationName;
    private String requestPathQuery;
    private String hostAddress;
    private int requestType;

    // CRT info metrics
    private String ipAddress;
    private long connectionId;
    private long threadId;
    private int streamId;
    private int errorCode;
    private int retryAttempt;

    // Native Adapted members
    private ErrorType errorType;

//    long getApiCallDurationNs() {
//
//    }

    public boolean isApiCallSuccessful() {
        return this.errorCode == 0;
    }

    public String getOperationName() {
        return this.operationName;
    }

    public int getRetryCount() {
        return this.retryAttempt;
    }

    public String getServiceId() {
        return "s3";
    }

    public String getServiceEndpoint() {
        return this.hostAddress;
    }

//    public String getAwsExtendedRequestId() {
//        return
//    }

    public String getAwsRequestId() {
        return this.requestId;
    }

//    long getBackoffDelayDurationNs() {
//
//    }

    public ErrorType getErrorType() {
        return this.errorType;
    }

//    long getServiceCallDurationNs() {
//
//    }

    public long getSigningDurationNs() {
        return this.signingDurationNs;
    }

    public long getTimeToFirstByte() {
        return this.receiveStartTimestampNs;
    }

    public long getTimeToLastByte() {
        return this.receiveEndTimestampNs;
    }
}
