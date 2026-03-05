/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.CRT;

import static org.junit.Assert.*;

/**
 * Test suite for static methods in CRT class.
 */
public class CrtStaticTest extends CrtTestFixture {

    public CrtStaticTest() {
    }

    /**
     * Test that AWS_CRT_SUCCESS (0) is not considered a transient error.
     */
    @Test
    public void testAwsIsTransientErrorWithSuccess() {
        assertFalse("Success code should not be transient", CRT.awsIsTransientError(CRT.AWS_CRT_SUCCESS));
    }

    /**
     * Test that the method returns a valid boolean for various error codes.
     */
    @Test
    public void testAwsIsTransientErrorWithVariousErrorCodes() {
        // Test with a range of error codes to ensure the method doesn't crash
        int[] testErrorCodes = {0, 1, 1024, 2048, 3072, 4096};

        for (int errorCode : testErrorCodes) {
            // The method should return a valid boolean without throwing an exception
            boolean result = CRT.awsIsTransientError(errorCode);
            // Just verify the call completes successfully - the actual result depends on native implementation
            assertNotNull("Should return a valid boolean", Boolean.valueOf(result));
        }
    }

    /**
     * Test that the method handles invalid error codes gracefully.
     */
    @Test
    public void testAwsIsTransientErrorWithInvalidCodes() {
        // Test with invalid error codes
        assertFalse("Negative error code should not be transient", CRT.awsIsTransientError(-1));
        assertFalse("Large error code should not be transient", CRT.awsIsTransientError(999999));
    }

    /**
     * Test awsLastError returns a valid error code.
     */
    @Test
    public void testAwsLastError() {
        int lastError = CRT.awsLastError();
        // The error code should be non-negative
        assertTrue("Last error should be non-negative", lastError >= 0);
    }

    /**
     * Test awsErrorString returns a non-null string for valid error codes.
     */
    @Test
    public void testAwsErrorString() {
        String errorString = CRT.awsErrorString(0);
        assertNotNull("Error string should not be null", errorString);

        // Test with a non-zero error code
        errorString = CRT.awsErrorString(1);
        assertNotNull("Error string should not be null", errorString);
    }

    /**
     * Test awsErrorName returns a non-null string for valid error codes.
     */
    @Test
    public void testAwsErrorName() {
        String errorName = CRT.awsErrorName(0);
        assertNotNull("Error name should not be null", errorName);

        // Test with a non-zero error code
        errorName = CRT.awsErrorName(1);
        assertNotNull("Error name should not be null", errorName);
    }
}