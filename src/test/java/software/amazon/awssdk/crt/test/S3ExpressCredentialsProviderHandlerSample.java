package software.amazon.awssdk.crt.test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;


import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.s3.S3Client;
import software.amazon.awssdk.crt.s3.S3FinishedResponseContext;
import software.amazon.awssdk.crt.s3.S3MetaRequest;
import software.amazon.awssdk.crt.s3.S3MetaRequestOptions;
import software.amazon.awssdk.crt.s3.S3MetaRequestOptions.MetaRequestType;
import software.amazon.awssdk.crt.s3.S3MetaRequestResponseHandler;
import software.amazon.awssdk.crt.s3.S3ExpressCredentialsProperties;
import software.amazon.awssdk.crt.s3.S3ExpressCredentialsProviderHandler;

/**
 * Sample implementation of a s3express credentials provider.
 * Check `testS3PutS3ExpressOverrideSamples` for the usage.
 *
 * This Implementation simply calls create session api every time asked for a credentials, without any caching.
 */
public class S3ExpressCredentialsProviderHandlerSample implements S3ExpressCredentialsProviderHandler {
    private S3Client client;

    public S3ExpressCredentialsProviderHandlerSample(S3Client client) {
        this.client = client;
    }

    public CompletableFuture<Credentials> getS3ExpressCredentials(S3ExpressCredentialsProperties properties, Credentials origCredentials) {
        CompletableFuture<Credentials> future = new CompletableFuture<Credentials>();
        HttpHeader[] headers = { new HttpHeader("Host", properties.getHostValue()), };

        HttpRequest httpRequest = new HttpRequest("GET", "/?session=", headers, null);

        S3MetaRequestResponseHandler responseHandler = new S3MetaRequestResponseHandler() {
            String responseString = new String("");

            @Override
            public int onResponseBody(ByteBuffer bodyBytesIn, long objectRangeStart, long objectRangeEnd) {
                String str = new String(bodyBytesIn.array(), StandardCharsets.UTF_8);
                responseString += str;
                return 0;
            }

            private String getResultString(String startSyntax, String endSyntax){
                String restString = responseString.split(startSyntax)[1];
                return restString.split(endSyntax)[0];
            }

            @Override
            public void onFinished(S3FinishedResponseContext context) {
                try {
                    String sessionToken = getResultString("<SessionToken>", "</SessionToken>");
                    String secretKey = getResultString("<SecretAccessKey>", "</SecretAccessKey>");
                    String accessKeyId = getResultString("<AccessKeyId>", "</AccessKeyId>");
                    Credentials creds = new Credentials(accessKeyId.getBytes(), secretKey.getBytes(), sessionToken.getBytes());
                    future.complete(creds);
                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }
        };

        AwsSigningConfig config = AwsSigningConfig.getDefaultS3SigningConfig(properties.getRegion(), null);
        S3MetaRequestOptions metaRequestOptions = new S3MetaRequestOptions()
                .withMetaRequestType(MetaRequestType.DEFAULT).withHttpRequest(httpRequest)
                .withResponseHandler(responseHandler).withSigningConfig(config);

        S3MetaRequest metaRequest = client.makeMetaRequest(metaRequestOptions);
        future.whenComplete((r,t) -> {
            metaRequest.close();
        });
        return future;

    }

    public CompletableFuture<Void> destroyProvider() {
        CompletableFuture<Void> future = new CompletableFuture<Void>();
        future.complete(null);
        return future;

    }

}
