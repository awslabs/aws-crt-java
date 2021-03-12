package com.amazonaws.s3;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface RequestDataSupplier extends OperationHandler{
    /**
     * Called to retrieve data from this supplier. The supplier must write the
     * available bytes to the given {@code ByteBuffer}.
     * 
     * Return true if all of the request data has been supplied,
     * 
     * @param buffer byte array to populate with data.
     * @return true if and only if all of the data has been supplied and the transfer can complete 
     */
    boolean getRequestBytes(ByteBuffer buffer);

    /**
     * Called when the processing needs the stream to rewind itself back to its beginning.
     * If the stream does not support rewinding or the rewind fails, false should be returned
     *
     * Signing requires a rewindable stream, but basic http does not.
     *
     * @return True if the stream was successfully rewound, false otherwise.
     */
    default boolean resetPosition() { return false; }
}
