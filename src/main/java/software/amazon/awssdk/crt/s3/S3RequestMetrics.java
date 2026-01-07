/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;

/**
 * An Request is any HTTP request made to the S3 Server. Within CRT,
 * a large enough request (S3MetaRequest) from the customer might be divided
 * into smaller, parallelised Requests for better performance. These are considered
 * independent requests to S3 but CRT abstracts away the implementation details.
 *
 * S3RequestMetrics are asynchronously collected upon completion of a Request.
 * This however, is independent from the success or even completion of the corresponding
 * S3MetaRequest. When a particular request from customer (a Meta Request) is divided
 * into n-requests on the CRT end, customer receives at minimum n-S3RequestMetrics
 * objects (assuming all requests succeed in the first attempt, more if some fail).
 */
public class S3RequestMetrics {
    // AWS_ERROR_S3_METRIC_DATA_NOT_AVAILABLE = 14358
    private static final int AWS_ERROR_S3_METRIC_DATA_NOT_AVAILABLE = 14358;

    // Required timestamp metrics - always available (default to 0)
    private long s3RequestFirstAttemptStartTimestampNs = 0;
    private long startTimestampNs = 0;
    private long endTimestampNs = 0;
    private long totalDurationNs = 0;

    // Optional timestamp metrics - may not be available (default to -1)
    // These fields will throw CrtRuntimeException(14358) if accessed when unavailable
    private long s3RequestLastAttemptEndTimestampNs = -1;
    private long sendStartTimestampNs = -1;
    private long sendEndTimestampNs = -1;
    private long sendingDurationNs = -1;
    private long receiveStartTimestampNs = -1;
    private long receiveEndTimestampNs = -1;
    private long receivingDurationNs = -1;
    private long signStartTimestampNs = -1;
    private long signEndTimestampNs = -1;
    private long signingDurationNs = -1;
    private long memAcquireStartTimestampNs = -1;
    private long memAcquireEndTimestampNs = -1;
    private long memAcquireDurationNs = -1;
    private long deliverStartTimestampNs = -1;
    private long deliverEndTimestampNs = -1;
    private long deliverDurationNs = -1;
    private long retryDelayStartTimestampNs = -1;
    private long retryDelayEndTimestampNs = -1;
    private long retryDelayDurationNs = -1;
    private long serviceCallDurationNs = -1;

    // Request/Response info metrics
    // Optional: may not be available (defaults to -1 for int, null for String)
    private int responseStatus = -1;
    private String requestId = null;
    private String extendedRequestId = null;
    private String operationName = null;

    // Required: always available (default to null, will be set by native code)
    private String requestPathQuery = null;
    private String hostAddress = null;

    // Required: always available (default to 0)
    private int requestType = 0;

    // CRT info metrics - optional, may not be available (default to null or -1)
    private String ipAddress = null;

    // Request ptr and connection id are internal metrics for crt that directly points to the
    // connection's and S3 request's addresses for this request attempt. This does not need to be exposed,
    // but it might prove useful for logs.
    // Optional: may not be available (default to -1)
    private long connectionId = -1;
    private long requestPtr = -1;
    private long threadId = -1;
    private int streamId = -1;

    // Required: always available (default to 0)
    private int errorCode = 0;
    private int retryAttempt = 0;

    public long getApiCallDurationNs() {
        if (this.s3RequestLastAttemptEndTimestampNs == -1) {
            throw new CrtRuntimeException(AWS_ERROR_S3_METRIC_DATA_NOT_AVAILABLE);
        }
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
        if (this.extendedRequestId == null) {
            throw new CrtRuntimeException(AWS_ERROR_S3_METRIC_DATA_NOT_AVAILABLE);
        }
        return this.extendedRequestId;
    }

    public String getAwsRequestId() {
        if (this.requestId == null) {
            throw new CrtRuntimeException(AWS_ERROR_S3_METRIC_DATA_NOT_AVAILABLE);
        }
        return this.requestId;
    }

    public long getBackoffDelayDurationNs() {
        if (this.retryDelayDurationNs == -1) {
            throw new CrtRuntimeException(AWS_ERROR_S3_METRIC_DATA_NOT_AVAILABLE);
        }
        return this.retryDelayDurationNs;
    }

    public long getServiceCallDurationNs() {
        if (this.serviceCallDurationNs == -1) {
            throw new CrtRuntimeException(AWS_ERROR_S3_METRIC_DATA_NOT_AVAILABLE);
        }
        return this.serviceCallDurationNs;
    }

    public long getSigningDurationNs() {
        if (this.signingDurationNs == -1) {
            throw new CrtRuntimeException(AWS_ERROR_S3_METRIC_DATA_NOT_AVAILABLE);
        }
        return this.signingDurationNs;
    }

    public long getTimeToFirstByte() {
        if (this.receiveStartTimestampNs == -1) {
            throw new CrtRuntimeException(AWS_ERROR_S3_METRIC_DATA_NOT_AVAILABLE);
        }
        return this.receiveStartTimestampNs;
    }

    public long getTimeToLastByte() {
        if (this.receiveEndTimestampNs == -1) {
            throw new CrtRuntimeException(AWS_ERROR_S3_METRIC_DATA_NOT_AVAILABLE);
        }
        return this.receiveEndTimestampNs;
    }

    // Please use CRT.awsIsTransientError() to identify transient errors
    public int getErrorCode() {
        return this.errorCode;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }
}
