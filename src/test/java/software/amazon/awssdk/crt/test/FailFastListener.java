package software.amazon.awssdk.crt.test;

import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class FailFastListener extends RunListener {
    public void testFailure(Failure failure) throws Exception {
        System.err.println("FAILURE: " + failure);
        System.exit(-1);
    }
}
