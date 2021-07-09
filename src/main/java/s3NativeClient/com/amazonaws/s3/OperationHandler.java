package com.amazonaws.s3;

import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.http.HttpHeader;

public interface OperationHandler {
    
    /**
     * HttpHeaders seen in the response can be handled via looking for them here.
     * 
     * @param statusCode int 
     * @param headers HttpHeaders 
     */
    default void onResponseHeaders(final int statusCode, final HttpHeader[] headers) { }
    
    /**
     * Invoked when there is an exception (IO or processing) sending or receiving data for
     * the operation. Exceptions indicate the operation is finished.
     */
    default void onException(final CrtRuntimeException e) { }

    /**
     * Invoked when the transfer has completed normally
     */
    default void onFinished() { }
}
