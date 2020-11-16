package software.amazon.awssdk.crt.test;

import org.junit.Test;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.ServerBootstrap;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;

public class ServerBootstrapTest extends CrtTestFixture {
    public ServerBootstrapTest() {}

    @Test
    public void testCreateDestroy() throws ExecutionException, InterruptedException {
        EventLoopGroup elg = new EventLoopGroup(1);
        ServerBootstrap bootstrap = new ServerBootstrap(elg);

        assertNotNull(bootstrap);
        bootstrap.close();
        elg.close();
    }
}
