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
import software.amazon.awssdk.crt.utils.ByteBufferUtils;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.CrtRuntimeException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class S3ClientTest extends CrtTestFixture {

    static final String ENDPOINT = "aws-crt-test-stuff-us-west-2.s3.us-west-2.amazonaws.com";
    static final String REGION = "us-west-2";

    public S3ClientTest() {
    }

    private S3Client createS3Client(S3ClientOptions options) {
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hostResolver = new HostResolver(elg);
            ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver)) {
            Assert.assertNotNull(clientBootstrap);

            try (DefaultChainCredentialsProvider credentialsProvider = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder()
                    .withClientBootstrap(clientBootstrap).build()) {
                options.withRegion(REGION)
                    .withClientBootstrap(clientBootstrap)
                    .withCredentialsProvider(credentialsProvider);
                return new S3Client(options);
            }
        }
    }

    @Test
    public void testS3ClientCreateDestroy() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT);
        try (S3Client client = createS3Client(clientOptions)) {

        }
    }

    @Test
    public void testS3Get() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Body Response: " + bodyBytesIn.toString());
                    return 0;
                }

                @Override
                public void onFinished(int errorCode) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + errorCode);
                    if (errorCode != 0) {
                        onFinishedFuture.completeExceptionally(new CrtRuntimeException(errorCode));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(errorCode));
                }
            };

            HttpHeader[] headers = { new HttpHeader("Host", ENDPOINT) };
            HttpRequest httpRequest = new HttpRequest("GET", "/get_object_test_1MB.txt", headers, null);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.GET_OBJECT)
                    .withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    private byte[] createTestPayload() {
        String msg = "This is an S3 Java CRT Client Test";
        ByteBuffer payload = ByteBuffer.allocate(1024 * 1024);
        while (true) {
            try {
                payload.put(msg.getBytes());
            }
            catch (BufferOverflowException ex1) {
                while (true) {
                    try {
                        payload.put("#".getBytes());
                    } catch (BufferOverflowException ex2) {
                        break;
                    }
                }
                break;
            }
        }
        return payload.array();
    }

    @Test
    public void testS3Put() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
        Assume.assumeTrue(hasAwsCredentials());

        S3ClientOptions clientOptions = new S3ClientOptions().withEndpoint(ENDPOINT);
        try (S3Client client = createS3Client(clientOptions)) {
            CompletableFuture<Integer> onFinishedFuture = new CompletableFuture();
            S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {

                @Override
                public int onResponseBody(byte[] bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3, "Body Response: " + bodyBytesIn.toString());
                    return 0;
                }

                @Override
                public void onFinished(int errorCode) {
                    Log.log(Log.LogLevel.Info, Log.LogSubject.JavaCrtS3,
                            "Meta request finished with error code " + errorCode);
                    if (errorCode != 0) {
                        onFinishedFuture.completeExceptionally(new CrtRuntimeException(errorCode));
                        return;
                    }
                    onFinishedFuture.complete(Integer.valueOf(errorCode));
                }
            };

            final ByteBuffer payload = ByteBuffer.wrap(createTestPayload());
            HttpRequestBodyStream payloadStream = new HttpRequestBodyStream() {
                @Override
                public boolean sendRequestBody(ByteBuffer outBuffer) {
                    ByteBufferUtils.transferData(payload, outBuffer);
                    return payload.remaining() == 0;
                }

                @Override
                public boolean resetPosition() {
                    return true;
                }

                @Override
                public long getLength() {
                    return payload.capacity();
                }
            };

            HttpHeader[] headers = {
                new HttpHeader("Host", ENDPOINT),
                    new HttpHeader("Content-Length", Integer.valueOf(payload.capacity()).toString()),
            };
            HttpRequest httpRequest = new HttpRequest("PUT", "/put_object_test_1MB.txt", headers, payloadStream);

            S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                    .withMetaRequestType(MetaRequestType.PUT_OBJECT)
                    .withHttpRequest(httpRequest)
                    .withResponseHandler(responseHandler);

            try (S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions)) {
                Assert.assertEquals(Integer.valueOf(0), onFinishedFuture.get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
