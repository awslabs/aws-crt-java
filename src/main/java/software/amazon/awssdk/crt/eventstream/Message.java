package software.amazon.awssdk.crt.eventstream;

import software.amazon.awssdk.crt.CrtResource;

import java.nio.ByteBuffer;
import java.util.List;

public class Message extends CrtResource {
    public Message(List<Header> headers, byte[] payload) {
        acquireNativeHandle(messageNew(Header.marshallHeadersForJNI(headers), payload));
    }

    public ByteBuffer getMessageBuffer() {
        return messageBuffer(getNativeHandle());
    }

    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            messageDelete(getNativeHandle());
        }
    }

    @Override
    protected boolean canReleaseReferencesImmediately() {
        return true;
    }

    private static native long messageNew(byte[] serializedHeaders, byte[] payload);
    private static native void messageDelete(long messageHandle);
    private static native ByteBuffer messageBuffer(long messageHandle);
}
