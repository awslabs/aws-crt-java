/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.http.HttpHeader;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;

class S3MetaRequestResponseHandlerNativeAdapter {
    private S3MetaRequestResponseHandler responseHandler;
    // Create a reference queue


    // Background thread to monitor the ReferenceQueue
    static class CleanerThread extends Thread {
        private final ReferenceQueue<ByteBuffer> referenceQueue;
        private long buffer;

        public CleanerThread(ReferenceQueue<ByteBuffer> referenceQueue, long buffer) {
            this.referenceQueue = referenceQueue;
            this.buffer = buffer;
            this.setDaemon(true); // Make it a daemon thread so it doesn't block JVM shutdown
        }

        @Override
        public void run() {
            try {
                while (true) {
                    // Wait for a PhantomReference to be enqueued
                    PhantomReference<? extends ByteBuffer> ref =
                        (PhantomReference<? extends ByteBuffer>) referenceQueue.remove(); // Blocking call

                    // Trigger the callback or cleanup action
                    // freeDirectBuffer(this.buffer);
                    System.out.println("teaewewaet");
                    System.out.println(this.buffer);

                    // Optionally clear the reference to avoid memory leaks
                    ref.clear();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
            }
        }
    }

    S3MetaRequestResponseHandlerNativeAdapter(S3MetaRequestResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
    }

    int onResponseBody(ByteBuffer bodyBytesIn, long buffer, long objectRangeStart, long objectRangeEnd) {
        ReferenceQueue<ByteBuffer> referenceQueue = new ReferenceQueue<>();
        PhantomReference<ByteBuffer> phantomReference = new PhantomReference<>(bodyBytesIn, referenceQueue);
        CleanerThread cleanerThread = new CleanerThread(referenceQueue, buffer);
        cleanerThread.start();
        return this.responseHandler.onResponseBody(bodyBytesIn, objectRangeStart, objectRangeEnd);
    }

    void onFinished(int errorCode, int responseStatus, byte[] errorPayload, String errorOperationName, int checksumAlgorithm, boolean didValidateChecksum, Throwable cause, final ByteBuffer headersBlob) {
        HttpHeader[] errorHeaders = headersBlob == null ? null : HttpHeader.loadHeadersFromMarshalledHeadersBlob(headersBlob);
        S3FinishedResponseContext context = new S3FinishedResponseContext(errorCode, responseStatus, errorPayload, errorOperationName, ChecksumAlgorithm.getEnumValueFromInteger(checksumAlgorithm), didValidateChecksum, cause, errorHeaders);
        this.responseHandler.onFinished(context);
    }

    void onResponseHeaders(final int statusCode, final ByteBuffer headersBlob) {
        responseHandler.onResponseHeaders(statusCode, HttpHeader.loadHeadersFromMarshalledHeadersBlob(headersBlob));
    }

    void onProgress(final S3MetaRequestProgress progress) {
        responseHandler.onProgress(progress);
    }


    private static native void freeDirectBuffer(long bufferId);
}
