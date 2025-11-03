package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.ErrorType;

/**
 * Metrics collected upon completion of an S3 Request
 */
public class S3RequestMetrics {
    private long s3RequestFirstAttemptStartTimestampNs;
    private long s3RequestLastAttemptEndTimestampNs;
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
    private long retryDelayStartTimestampNs;
    private long retryDelayEndTimestampNs;
    private long retryDelayDurationNs;
    private long serviceCallDurationNs;

    // Request/Response info metrics
    private int responseStatus;
    private String requestId;
    private String extendedRequestId;
    private String operationName;
    private String requestPathQuery;
    private String hostAddress;
    private int requestType;

    // CRT info metrics
    private String ipAddress;
    
    // Request ptr and connection id are internal metrics for crt that directly points to the
    // connection's and S3 request's addresses for this request attempt. This does not need to be exposed,
    // but it might prove useful for logs.
    private long connectionId;
    private long requestPtr;
    
    private long threadId;
    private int streamId;
    private int errorCode;
    private int retryAttempt;

    public long getApiCallDurationNs() {
        return this.s3RequestLastAttemptEndTimestampNs - this.s3RequestFirstAttemptStartTimestampNs;
    }

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

    public String getAwsExtendedRequestId() {
        return this.extendedRequestId;
    }

    public String getAwsRequestId() {
        return this.requestId;
    }

    public long getBackoffDelayDurationNs() {
        return this.retryDelayDurationNs;
    }

    public ErrorType getErrorType() {
        return CRT.awsGetErrorType(this.errorCode);
    }

    public long getServiceCallDurationNs() {
        return this.serviceCallDurationNs;
    }    

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
