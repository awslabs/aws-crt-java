package com.amazonaws.s3;

import static org.junit.Assert.assertNotNull;

import com.amazonaws.s3.model.GetObjectOutput;
import com.amazonaws.s3.model.GetObjectRequest;
import com.amazonaws.s3.model.PutObjectRequest;
import com.amazonaws.test.AwsClientTestFixture;
import java.nio.ByteBuffer;
import org.junit.BeforeClass;
import org.junit.Test;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import org.junit.Assume;

public class S3NativeClientTest extends AwsClientTestFixture {
    private static final String BUCKET = System.getProperty("crt.test_s3_bucket", "<bucket>>");
    private static final String REGION = System.getProperty("crt.test_s3_region", "us-east-1");
    private static final String GET_OBJECT_KEY = System.getProperty("crt.test_s3_get_object_key", "file.download");
    private static final String PUT_OBJECT_KEY = System.getProperty("crt.test_s3_put_object_key", "file.upload");
    
    @BeforeClass
    public static void haveAwsCredentials() {
        Assume.assumeTrue(areAwsCredentialsAvailable());
    }
    
    @Test
    public void testGetObject() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        try (final EventLoopGroup elGroup = new EventLoopGroup(9);
             final HostResolver resolver = new HostResolver(elGroup, 128);
             final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
             final CredentialsProvider provider = getTestCredentialsProvider()) {
            final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider,
                    64_000_000l, 100.);
            final long length[] = { 0 };
            nativeClient.getObject(GetObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(GET_OBJECT_KEY)
                    .build(), new ResponseDataConsumer<GetObjectOutput>() {

                @Override
                public void onResponse(GetObjectOutput response) {
                    assertNotNull(response);
                }

                @Override
                public void onResponseData(ByteBuffer bodyBytesIn) {
                    length[0] += bodyBytesIn.remaining();
                }

                @Override
                public void onFinished() { }

                @Override
                public void onException(final CrtRuntimeException e) { }
            }).join();
        }
    }
    
    @Test
    public void testPutObject() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);

        try (final EventLoopGroup elGroup = new EventLoopGroup(9);
             final HostResolver resolver = new HostResolver(elGroup, 128);
             final ClientBootstrap clientBootstrap = new ClientBootstrap(elGroup, resolver);
             final CredentialsProvider provider = getTestCredentialsProvider()) {
            final S3NativeClient nativeClient = new S3NativeClient(REGION, clientBootstrap, provider,
                    64_000_000l, 100.);
            final long contentLength = 1024l;
            final long lengthWritten[] = { 0 };
            nativeClient.putObject(PutObjectRequest.builder()
                    .bucket(BUCKET)
                    .key(PUT_OBJECT_KEY)
                    .contentLength(contentLength)
                    .build(), buffer -> {
                        for (int index = 0; index < buffer.length; ++index) {
                            buffer[index] = 42;
                        }
                        lengthWritten[0] += buffer.length;
                        return lengthWritten[0] == contentLength;
                    }).join();

        }
    }
}
