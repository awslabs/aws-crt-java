package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.io.StandardRetryOptions;

public class S3ClientNativeCallbacksNativeAdapter {
    private S3ClientNativeCallbacks nativeCallbacks;

    S3ClientNativeCallbacksNativeAdapter(S3ClientNativeCallbacks nativeCallbacks) {
        this.nativeCallbacks = nativeCallbacks;
    }

    void onSetupStandardRetryOptions(long standardRetryOptionsNativeHandle) {
        this.nativeCallbacks.onSetupStandardRetryOptions(standardRetryOptionsNativeHandle);
    }
}
