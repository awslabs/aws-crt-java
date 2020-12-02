package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.s3.S3Client;
import software.amazon.awssdk.crt.s3.S3ClientOptions;
import software.amazon.awssdk.crt.s3.S3MetaRequest;
import software.amazon.awssdk.crt.s3.S3MetaRequestOptions;
import software.amazon.awssdk.crt.s3.S3MetaRequestResponseHandler;
import software.amazon.awssdk.crt.s3.S3MetaRequestOptions.MetaRequestType;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.Log;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Rule;
import org.junit.rules.Timeout;

import java.util.concurrent.CompletableFuture;
import java.util.function.*;

public class S3ClientTest extends CrtTestFixture {

    public S3ClientTest() {
    }

    @Test
    public void testS3ClientCreateDestroy() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        EventLoopGroup elg = new EventLoopGroup(1);
        HostResolver hostResolver = new HostResolver(elg);
        ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver);
        assertNotNull(clientBootstrap);

        DefaultChainCredentialsProvider credentialsProvider = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                .withClientBootstrap(clientBootstrap).build();

        CompletableFuture<Void> shutdownComplete = null;

        String endpoint = "aws-crt-test-stuff-us-west-2.s3.us-west-2.amazonaws.com";

        S3ClientOptions clientOptions = new S3ClientOptions().withRegion("us-west-2").withEndpoint(endpoint)
                .withClientBootstrap(clientBootstrap).withCredentialsProvider(credentialsProvider);

        S3Client client = new S3Client(clientOptions);

        client.close();
        client.getShutdownCompleteFuture().get();

        credentialsProvider.close();
        credentialsProvider.getShutdownCompleteFuture().get();

        clientBootstrap.close();
        clientBootstrap.getShutdownCompleteFuture().get();

        hostResolver.close();
        elg.close();

        CrtResource.waitForNoResources();
    }

    @Test
    public void testS3Get() throws Exception {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        Log.initLoggingToStdout(Log.LogLevel.Debug);

        EventLoopGroup elg = new EventLoopGroup(1);
        HostResolver hostResolver = new HostResolver(elg);
        ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver);
        assertNotNull(clientBootstrap);

        DefaultChainCredentialsProvider credentialsProvider = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                .withClientBootstrap(clientBootstrap).build();

        CompletableFuture<Void> shutdownComplete = null;

        String endpoint = "aws-crt-test-stuff-us-west-2.s3.us-west-2.amazonaws.com";

        S3ClientOptions clientOptions = new S3ClientOptions().withRegion("us-west-2").withEndpoint(endpoint)
                .withClientBootstrap(clientBootstrap).withCredentialsProvider(credentialsProvider);

        S3Client client = new S3Client(clientOptions);

        CompletableFuture<Void> onFinishedFuture = new CompletableFuture<Void>();

        S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

            @Override
            public int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                Log.log(Log.LogLevel.Info, Log.LogSubject.S3Client, "Body Response: " + bodyBytesIn.toString());
                return 0;
            }

            @Override
            public void onFinished(int errorCode) {
                Log.log(Log.LogLevel.Info, Log.LogSubject.S3Client,
                        "Meta request finished with error code " + errorCode);
                onFinishedFuture.complete(null);
            }
        };

        HttpHeader[] headers = { new HttpHeader("Host", endpoint) };
        HttpRequest httpRequest = new HttpRequest("GET", "/get_object_test_1MB.txt", headers, null);

        S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                .withMetaRequestType(MetaRequestType.GET_OBJECT).withHttpRequest(httpRequest)
                .withResponseHandler(responseHandler);

        S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions);

        onFinishedFuture.get();

        metaRequest.close();
        metaRequest.getShutdownCompleteFuture().get();

        client.close();
        client.getShutdownCompleteFuture().get();

        credentialsProvider.close();
        credentialsProvider.getShutdownCompleteFuture().get();

        clientBootstrap.close();
        clientBootstrap.getShutdownCompleteFuture().get();

        hostResolver.close();
        elg.close();

        CrtResource.waitForNoResources();
    }
}
