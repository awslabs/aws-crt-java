package software.amazon.awssdk.crt.graalvm;


import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeJNIAccess;
import software.amazon.awssdk.crt.http.HttpRequestBase;


class JNIRegistrationFeature implements Feature {
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        try {
            RuntimeJNIAccess.register(HttpRequestBase.class);
            RuntimeJNIAccess.register(HttpRequestBase.class.getDeclaredField("bodyStream"));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}

