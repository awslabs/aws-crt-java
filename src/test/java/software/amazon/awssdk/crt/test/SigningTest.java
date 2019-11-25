/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.crt.test;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.crt.auth.signing.AwsSigner;
import software.amazon.awssdk.crt.auth.signing.AwsSigningConfig;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import static software.amazon.awssdk.crt.utils.ByteBufferUtils.transferData;

public class SigningTest {

    public static String METHOD = "POST";

    public SigningTest() {}

    private HttpRequest createSampleRequest(String endpoint, String method, String path, String body) throws Exception {
        URI uri = new URI(endpoint);

        HttpHeader[] requestHeaders =
                new HttpHeader[]{
                    new HttpHeader("Host", uri.getHost()),
                    new HttpHeader("Content-Length", Integer.toString(body.getBytes(StandardCharsets.UTF_8).length))
                };

        final ByteBuffer bodyBytesIn = ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8));
        HttpRequestBodyStream bodyStream = new HttpRequestBodyStream() {
            @Override
            public boolean sendRequestBody(ByteBuffer bodyBytesOut) {
                transferData(bodyBytesIn, bodyBytesOut);

                return bodyBytesIn.remaining() == 0;
            }

            @Override
            public boolean resetPosition() {
                bodyBytesIn.position(0);

                return true;
            }
        };

        return new HttpRequest(method, path, requestHeaders, bodyStream);
    }

    @Test
    public void testSigningProcess() throws Exception {
        try (StaticCredentialsProvider provider = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
            .withAccessKeyId("AKIDEXAMPLE".getBytes())
            .withSecretAccessKey("wJalrXUtnFEMI/K7MDENG+bPxRfiCYEXAMPLEKEY".getBytes())
            .build();) {

            HttpRequest request = createSampleRequest("https://www.example.com", "POST", "/derp", "<body>Hello</body>");

            Predicate<String> filterParam = param -> !param.equals("bad-param");

            AwsSigningConfig config = new AwsSigningConfig();
            config.setSigningAlgorithm(AwsSigningConfig.AwsSigningAlgorithm.SIGV4_HEADER);
            config.setRegion("us-east-1");
            config.setService("dummy");
            config.setTime(Instant.now());
            config.setCredentialsProvider(provider);
            config.setShouldSignParameter(filterParam);
            config.setUseDoubleUriEncode(true);
            config.setShouldNormalizeUriPath(true);
            config.setSignBody(false);

            CompletableFuture<HttpRequest> result = AwsSigner.signRequest(request, config);
            HttpRequest signedRequest = result.get();
            assertNotNull(signedRequest);
            assertTrue(signedRequest.getMethod().equals(request.getMethod()));

            System.out.println(String.format("%s %s", signedRequest.getMethod(), signedRequest.getEncodedPath()));
            for (HttpHeader header : signedRequest.getHeaders()) {
                System.out.println(String.format("%s:%s", header.getName(), header.getValue()));
            }
        }

        CrtResource.waitForNoResources();
    }



};
