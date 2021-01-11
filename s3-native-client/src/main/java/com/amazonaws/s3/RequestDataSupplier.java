package com.amazonaws.s3;

import java.util.function.Function;

public interface RequestDataSupplier extends Function<byte[], Boolean> {
    default Boolean apply(byte[] buffer) {
        return getRequestBytes(buffer);
    }
    
    /**
     * Gives the supplier implement an array that must be fully populated with sequences of bytes, representing
     * the data to be sent with the operation's request
     * 
     * Return true if all of the request data has been supplied
     * 
     * @param buffer byte array to populate with data.
     * @return true if and only if all of the data has been supplied and the transfer can complete 
     */
    boolean getRequestBytes(byte[] buffer);

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
