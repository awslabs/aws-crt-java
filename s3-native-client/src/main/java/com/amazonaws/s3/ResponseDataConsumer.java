package com.amazonaws.s3;

import java.nio.ByteBuffer;

/**
 * Produces events for response streaming data to be handled without holding everything in memory
 * @param <T> POJO response type.
 */
public interface ResponseDataConsumer<T> extends OperationHandler {

    /**
     * Called when the unmarshalled response object is ready.
     *
     * @param response The unmarshalled response.
     */
    public void onResponse(T response);
    
    /**
     * Called multiple times in sequence until there are no more response body bytes. 
     * @param bodyBytesIn the next sequence of bytes representing the data requested through the operation
     */
    public void onResponseData(ByteBuffer bodyBytesIn);

    /**
     * Invoked when the transfer has completed normally
     */
    public void onFinished();
}
