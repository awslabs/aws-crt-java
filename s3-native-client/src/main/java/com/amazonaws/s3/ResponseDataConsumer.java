package com.amazonaws.s3;

import software.amazon.awssdk.crt.CrtRuntimeException;

import java.util.function.Consumer;

/**
 * Produces events for response streaming data to be handled without holding everything in memory
 */
public interface ResponseDataConsumer extends Consumer<byte[]> {
    default void accept(byte[] bodyBytesIn) {
        onResponseData(bodyBytesIn);
    }
    
    /**
     * Called multiple times in sequence until there are no more response body bytes. 
     * @param bodyBytesIn the next sequence of bytes representing the data requested through the operation
     */
    public void onResponseData(byte[] bodyBytesIn);

    /**
     * Invoked when the transfer has completed normally
     */
    public void onFinished();

    /**
     * Invoked when there is an exception (IO or processing) recieving response bytes
     * 
     * Implies the transfer is terminated as well (onFinished will not also be called)
     */
    public void onException(CrtRuntimeException e);
}
