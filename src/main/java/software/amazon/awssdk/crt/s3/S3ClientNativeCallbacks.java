package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.io.StandardRetryOptions;

/* A callback interface for listening to miscellaneous events inside the client. */
public interface S3ClientNativeCallbacks {
    default void onSetupStandardRetryOptions(long standardRetryOptionsNativeHandle) {
    }
}
