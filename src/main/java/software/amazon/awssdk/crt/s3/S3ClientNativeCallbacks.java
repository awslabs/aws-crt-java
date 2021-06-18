package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.io.StandardRetryOptions;

public interface S3ClientNativeCallbacks {
    default void onSetupStandardRetryOptions(long standardRetryOptionsNativeHandle) { }
}
