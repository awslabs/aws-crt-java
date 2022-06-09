/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.AsyncCallback;
import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.concurrent.CompletableFuture;
import java.util.List;

/**
 * This class wraps aws-c-http to provide the basic HTTP/2 request/response
 * functionality via the AWS Common Runtime.
 *
 * Http2ClientConnection represents a single connection to a HTTP/2 service
 * endpoint.
 *
 * This class is not thread safe and should not be called from different
 * threads.
 */
public class Http2ClientConnection extends HttpClientConnection {

    /*
     * Error codes that may be present in HTTP/2 RST_STREAM and GOAWAY frames
     * (RFC-7540 7).
     */
    public enum Http2ErrorCode {
        PROTOCOL_ERROR(1), INTERNAL_ERROR(2), FLOW_CONTROL_ERROR(3), SETTINGS_TIMEOUT(4), STREAM_CLOSED(5),
        FRAME_SIZE_ERROR(6), REFUSED_STREAM(7), CANCEL(8), COMPRESSION_ERROR(9), CONNECT_ERROR(10),
        ENHANCE_YOUR_CALM(11), INADEQUATE_SECURITY(12), HTTP_1_1_REQUIRED(13);

        private int errorCode;

        Http2ErrorCode(int value) {
            errorCode = value;
        }

        public int getValue() {
            return errorCode;
        }
    }

    public Http2ClientConnection(long connectionBinding) {
        super(connectionBinding);
    }

    /**
     * Send a SETTINGS frame. SETTINGS will be applied locally when SETTINGS ACK is
     * received from peer.
     *
     * @param settings The array of settings to change. Note: each setting has its
     *                 boundary.
     *
     * @return When this future completes without exception, the peer has
     *         acknowledged the settings and the change has been applied.
     */
    public CompletableFuture<Void> updateSettings(final List<Http2ConnectionSetting> settings) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        if (isNull()) {
            future.completeExceptionally(
                    new IllegalStateException("Http2ClientConnection has been closed, can't change settings on it."));
            return future;
        }
        AsyncCallback updateSettingsCompleted = AsyncCallback.wrapFuture(future, null);
        try {
            http2ClientConnectionUpdateSettings(getNativeHandle(), updateSettingsCompleted,
                    Http2ConnectionSetting.marshallSettingsForJNI(settings));
        } catch (CrtRuntimeException ex) {
            future.completeExceptionally(ex);
        }
        return future;
    }

    /**
     * Send a PING frame. Round-trip-time is calculated when PING ACK is received
     * from peer.
     *
     * @param pingData 8 Bytes data with the PING frame or null for not include data
     *                 in ping
     *
     * @return When this future completes without exception, the peer has
     *         acknowledged the PING and future will be completed with the round
     *         trip time in nano seconds for the connection.
     */
    public CompletableFuture<Long> sendPing(final byte[] pingData) {
        CompletableFuture<Long> completionFuture = new CompletableFuture<>();
        if (isNull()) {
            completionFuture.completeExceptionally(
                    new IllegalStateException("Http2ClientConnection has been closed, can't send ping on it."));
            return completionFuture;
        }
        AsyncCallback pingCompleted = AsyncCallback.wrapFuture(completionFuture, 0L);
        try {
            http2ClientConnectionSendPing(getNativeHandle(), pingCompleted, pingData);
        } catch (CrtRuntimeException ex) {
            completionFuture.completeExceptionally(ex);
        }
        return completionFuture;
    }

    public CompletableFuture<Long> sendPing() {
        return this.sendPing(null);
    }

    /**
     * Send a custom GOAWAY frame.
     *
     * Note that the connection automatically attempts to send a GOAWAY during
     * shutdown (unless a GOAWAY with a valid Last-Stream-ID has already been sent).
     *
     * This call can be used to gracefully warn the peer of an impending shutdown
     * (http2_error=0, allow_more_streams=true), or to customize the final GOAWAY
     * frame that is sent by this connection.
     *
     * The other end may not receive the goaway, if the connection already closed.
     *
     * @param Http2ErrorCode   The HTTP/2 error code (RFC-7540 section 7) to send.
     *                         `enum Http2ErrorCode` lists official codes.
     * @param allowMoreStreams If true, new peer-initiated streams will continue to
     *                         be acknowledged and the GOAWAY's Last-Stream-ID will
     *                         be set to a max value. If false, new peer-initiated
     *                         streams will be ignored and the GOAWAY's
     *                         Last-Stream-ID will be set to the latest acknowledged
     *                         stream.
     * @param debugData        Optional debug data to send. Size must not exceed
     *                         16KB. null is acceptable to not include debug data.
     */
    public void sendGoAway(final Http2ErrorCode Http2ErrorCode, final boolean allowMoreStreams,
            final byte[] debugData) {
        http2ClientConnectionSendGoAway(getNativeHandle(), (long) Http2ErrorCode.getValue(), allowMoreStreams,
                debugData);
    }

    public void sendGoAway(final Http2ErrorCode Http2ErrorCode, final boolean allowMoreStreams) {
        this.sendGoAway(Http2ErrorCode, allowMoreStreams, null);
    }

    /**
     * Increment the connection's flow-control window to keep data flowing.
     *
     * If the connection was created with `manualWindowManagement` set true, the
     * flow-control window of the connection will shrink as body data is received
     * for all the streams created on it. (headers, padding, and other metadata do
     * not affect the window). The initial connection flow-control window is 65,535.
     * Once the connection's flow-control window reaches to 0, all the streams on
     * the connection stop receiving any further data.
     *
     * If `manualWindowManagement` is false, this call will have no effect. The
     * connection maintains its flow-control windows such that no back-pressure is
     * applied and data arrives as fast as possible.
     *
     * If you are not connected, this call will have no effect.
     *
     * Crashes when the connection is not http2 connection. The limit of the Maximum
     * Size is 2**31 - 1. If the increment size cause the connection flow window
     * exceeds the Maximum size, this call will result in the connection lost.
     *
     * @param incrementSize The size to increment for the connection's flow control
     *                      window
     */
    public void updateConnectionWindow(long incrementSize) {
        if (incrementSize > 4294967296L || incrementSize < 0) {
            throw new IllegalArgumentException("increment size cannot exceed 4294967296");
        }
        http2ClientConnectionUpdateConnectionWindow(getNativeHandle(), incrementSize);
    }

    /**
     * Schedules an HttpRequest on the Native EventLoop for this
     * HttpClientConnection. The HTTP/1.1 request will be transformed to HTTP/2
     * request under the hood.
     *
     * @param request       The Request to make to the Server.
     * @param streamHandler The Stream Handler to be called from the Native
     *                      EventLoop
     * @throws CrtRuntimeException if stream creation fails
     * @return The Http2Stream that represents this Request/Response Pair. It can be
     *         closed at any time during the request/response, but must be closed by
     *         the user thread making this request when it's done.
     */
    @Override
    public Http2Stream makeRequest(HttpRequestBase request, HttpStreamBaseResponseHandler streamHandler)
            throws CrtRuntimeException {
        if (isNull()) {
            throw new IllegalStateException("Http2ClientConnection has been closed, can't make requests on it.");
        }

        Http2Stream stream = http2ClientConnectionMakeRequest(getNativeHandle(), request.marshalForJni(),
                request.getBodyStream(), new HttpStreamResponseHandlerNativeAdapter(streamHandler));
        return stream;
    }

    /**
     * @TODO: bindings for getters of local/remote setting and goaway.
     */
    /*******************************************************************************
     * Native methods
     ******************************************************************************/

    private static native Http2Stream http2ClientConnectionMakeRequest(long connectionBinding, byte[] marshalledRequest,
            HttpRequestBodyStream bodyStream, HttpStreamResponseHandlerNativeAdapter responseHandler)
            throws CrtRuntimeException;

    private static native void http2ClientConnectionUpdateSettings(long connectionBinding,
            AsyncCallback completedCallback, long[] marshalledSettings) throws CrtRuntimeException;

    private static native void http2ClientConnectionSendPing(long connectionBinding, AsyncCallback completedCallback,
            byte[] pingData) throws CrtRuntimeException;

    private static native void http2ClientConnectionSendGoAway(long connectionBinding, long h2ErrorCode,
            boolean allowMoreStreams, byte[] debugData) throws CrtRuntimeException;

    private static native void http2ClientConnectionUpdateConnectionWindow(long connectionBinding, long incrementSize)
            throws CrtRuntimeException;
}
