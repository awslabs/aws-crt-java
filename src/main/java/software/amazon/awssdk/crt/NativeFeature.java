package software.amazon.awssdk.crt;

import org.graalvm.nativeimage.hosted.Feature;

public class NativeFeature implements Feature {

    @Override
    public void afterImageWrite(AfterImageWriteAccess access) {
      new CRT();
      CRT.extractLibrary(access.getImagePath().getParent().toString());
    }
}