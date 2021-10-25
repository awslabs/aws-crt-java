/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.concurrent.CompletableFuture;

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
        PROTOCOL_ERROR(1),
        INTERNAL_ERROR(2),
        FLOW_CONTROL_ERROR(3),
        SETTINGS_TIMEOUT(4),
        STREAM_CLOSED(5),
        FRAME_SIZE_ERROR(6),
        REFUSED_STREAM(7),
        CANCEL(8),
        COMPRESSION_ERROR(9),
        CONNECT_ERROR(10),
        ENHANCE_YOUR_CALM(11),
        INADEQUATE_SECURITY(12),
        HTTP_1_1_REQUIRED(13);

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
    public CompletableFuture<Void> changeSettings(final Http2ConnectionSetting settings[]) {
        throw new CrtRuntimeException("Unimplemented");
    }

    /**
     * Send a PING frame Round-trip-time is calculated when PING ACK is received
     * from peer.
     *
     * @param pingData Optional 8 Bytes data with the PING frame
     *
     * @return When this future completes without exception, the peer has
     *         acknowledged the PING and future will be completed with the round
     *         trip time in nano seconds for the connection.
     */
    public CompletableFuture<Long> sendPing(final byte[] pingData) {
        CompletableFuture<Long> completionFuture = new CompletableFuture<>();
        http2ClientConnectionSendPing(getNativeHandle(), completionFuture, pingData);
        return completionFuture;
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
     *                         16KB.
     */
    public void sendGoAway(final Http2ErrorCode Http2ErrorCode, final boolean allowMoreStreams,
            final byte[] debugData) {
        throw new CrtRuntimeException("Unimplemented");
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
     * @param increment_size The size to increment for the connection's flow control
     *                       window
     */
    public void updateConnectionWindow(long incrementSize) {
        throw new CrtRuntimeException("Unimplemented");
    }

    /**
     * @TODO: bindings for getters of local/remote setting and goaway.
     */
    /*******************************************************************************
     * Native methods
     ******************************************************************************/
    /*
     * @TODO: Do we need the change settings? We should have the initial settings
     * expose from connection manager... Any thing other than marshall the list to
     * byte for the settings array.
     */
    // private static native void http2ClientConnectionChangeSettings(long
    // connectionBinding,
    // CompletableFuture<Void> future /* settings */ ) throws CrtRuntimeException;

    private static native void http2ClientConnectionSendPing(long connectionBinding, CompletableFuture<Long> future,
            byte[] pingData) throws CrtRuntimeException;

    private static native void http2ClientConnectionSendGoAway(long connectionBinding, long h2ErrorCode,
            boolean allowMoreStreams, byte[] debugData) throws CrtRuntimeException;

    private static native void http2ClientConnectionUpdateConnectionWindow(long connectionBinding, long incrementSize)
            throws CrtRuntimeException;
}
