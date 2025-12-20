package software.amazon.awssdk.crt.test;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class FailFastListener extends RunListener {
    public void testFailure(Failure failure) throws Exception {
        System.err.println("FAILURE: " + failure);
        // Previously we used a negative code but caused CI to pass,
        // for unknown reasons, even when tests failed on Windows.
        System.exit(1);
    }
}
