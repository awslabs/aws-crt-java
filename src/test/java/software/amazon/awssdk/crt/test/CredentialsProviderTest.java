/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import com.sun.net.httpserver.HttpServer;

import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.auth.credentials.*;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.*;

public class CredentialsProviderTest extends CrtTestFixture {
    static private String ACCESS_KEY_ID = "access_key_id";
    static private String SECRET_ACCESS_KEY = "secret_access_key";
    static private String SESSION_TOKEN = "session_token";

    private static String COGNITO_IDENTITY = System.getenv("AWS_TESTING_COGNITO_IDENTITY");
    private static String TEST_HTTP_PROXY_HOST = System.getenv("AWS_TEST_HTTP_PROXY_HOST");
    private static String TEST_HTTP_PROXY_PORT = System.getenv("AWS_TEST_HTTP_PROXY_PORT");

    private boolean isCIEnvironmentSetUp() {
        if (COGNITO_IDENTITY == null ) {
            return false;
        }

        return true;
    }

    private boolean isProxyEnvironmentSetUp() {
        if (TEST_HTTP_PROXY_HOST == null || TEST_HTTP_PROXY_PORT == null) {
            return false;
        }

        return true;
    }

    public CredentialsProviderTest() {
    }

    @Test
    public void testCreateDestroyStatic() {
        StaticCredentialsProvider.StaticCredentialsProviderBuilder builder = new StaticCredentialsProvider.StaticCredentialsProviderBuilder();
        builder.withAccessKeyId(ACCESS_KEY_ID.getBytes());
        builder.withSecretAccessKey(SECRET_ACCESS_KEY.getBytes());
        builder.withSessionToken(SESSION_TOKEN.getBytes());

        try (StaticCredentialsProvider provider = builder.build()) {
            assertNotNull(provider);
            assertTrue(provider.getNativeHandle() != 0);
        } catch (CrtRuntimeException ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testGetCredentialsStatic() {
        StaticCredentialsProvider.StaticCredentialsProviderBuilder builder = new StaticCredentialsProvider.StaticCredentialsProviderBuilder();
        builder.withAccessKeyId(ACCESS_KEY_ID.getBytes());
        builder.withSecretAccessKey(SECRET_ACCESS_KEY.getBytes());
        builder.withSessionToken(SESSION_TOKEN.getBytes());

        try (StaticCredentialsProvider provider = builder.build()) {
            CompletableFuture<Credentials> future = provider.getCredentials();
            Credentials credentials = future.get();
            assertTrue(Arrays.equals(credentials.getAccessKeyId(), ACCESS_KEY_ID.getBytes()));
            assertTrue(Arrays.equals(credentials.getSecretAccessKey(), SECRET_ACCESS_KEY.getBytes()));
            assertTrue(Arrays.equals(credentials.getSessionToken(), SESSION_TOKEN.getBytes()));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testCreateDestroyDefaultChain() {
        skipIfNetworkUnavailable();
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver)) {
            DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder builder = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder();
            builder.withClientBootstrap(bootstrap);

            try (DefaultChainCredentialsProvider provider = builder.build()) {
                assertNotNull(provider);
                assertTrue(provider.getNativeHandle() != 0);
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetCredentialsDefaultChain() {
        skipIfNetworkUnavailable();
        try (EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver)) {
            DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder builder = new DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder();
            builder.withClientBootstrap(bootstrap);

            try (DefaultChainCredentialsProvider provider = builder.build()) {
                CompletableFuture<Credentials> future = provider.getCredentials();
                /*
                 * This may or may not succeed depending on the test environment and setting up
                 * the environment to force the test to always succeed or fail would mean
                 * modifying the environment from Java, which is gross.
                 */
                Credentials credentials = future.get();
            }
        } catch (Exception e) {
            ;
        }
    }

    @Test
    public void testCacheStatic() {
        StaticCredentialsProvider.StaticCredentialsProviderBuilder builder = new StaticCredentialsProvider.StaticCredentialsProviderBuilder();
        builder.withAccessKeyId(ACCESS_KEY_ID.getBytes());
        builder.withSecretAccessKey(SECRET_ACCESS_KEY.getBytes());
        builder.withSessionToken(SESSION_TOKEN.getBytes());

        try (StaticCredentialsProvider provider = builder.build()) {

            CachedCredentialsProvider.CachedCredentialsProviderBuilder cachedBuilder = new CachedCredentialsProvider.CachedCredentialsProviderBuilder();
            cachedBuilder.withCachingDurationInSeconds(900);
            cachedBuilder.withCachedProvider(provider);

            try (CredentialsProvider cachedProvider = cachedBuilder.build()) {
                CompletableFuture<Credentials> future = cachedProvider.getCredentials();
                Credentials credentials = future.get();
                assertTrue(Arrays.equals(credentials.getAccessKeyId(), ACCESS_KEY_ID.getBytes()));
                assertTrue(Arrays.equals(credentials.getSecretAccessKey(), SECRET_ACCESS_KEY.getBytes()));
                assertTrue(Arrays.equals(credentials.getSessionToken(), SESSION_TOKEN.getBytes()));
            } catch (Exception ex) {
                fail(ex.getMessage());
            }
        }
    }

    @Test
    public void testDelegate() {
        DelegateCredentialsProvider.DelegateCredentialsProviderBuilder builder = new DelegateCredentialsProvider.DelegateCredentialsProviderBuilder();
        DelegateCredentialsHandler credentialsHandler = new DelegateCredentialsHandler() {
            @Override
            public Credentials getCredentials() {
                return new Credentials(ACCESS_KEY_ID.getBytes(), SECRET_ACCESS_KEY.getBytes(),
                        SESSION_TOKEN.getBytes());
            }
        };
        builder.withHandler(credentialsHandler);
        try (DelegateCredentialsProvider provider = builder.build()) {
            CompletableFuture<Credentials> future = provider.getCredentials();
            Credentials credentials = future.get();
            assertTrue(Arrays.equals(credentials.getAccessKeyId(), ACCESS_KEY_ID.getBytes()));
            assertTrue(Arrays.equals(credentials.getSecretAccessKey(), SECRET_ACCESS_KEY.getBytes()));
            assertTrue(Arrays.equals(credentials.getSessionToken(), SESSION_TOKEN.getBytes()));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testDelegateAnonymousCredentials() {
        DelegateCredentialsProvider.DelegateCredentialsProviderBuilder builder = new DelegateCredentialsProvider.DelegateCredentialsProviderBuilder();
        DelegateCredentialsHandler credentialsHandler = new DelegateCredentialsHandler() {
            @Override
            public Credentials getCredentials() {
                return Credentials.createAnonymousCredentials();
            }
        };
        builder.withHandler(credentialsHandler);
        try (DelegateCredentialsProvider provider = builder.build()) {
            CompletableFuture<Credentials> future = provider.getCredentials();
            Credentials credentials = future.get();
            assertTrue(Arrays.equals(credentials.getAccessKeyId(), null));
            assertTrue(Arrays.equals(credentials.getSecretAccessKey(), null));
            assertTrue(Arrays.equals(credentials.getSessionToken(), null));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testDelegateWithoutToken() {
        DelegateCredentialsProvider.DelegateCredentialsProviderBuilder builder = new DelegateCredentialsProvider.DelegateCredentialsProviderBuilder();
        DelegateCredentialsHandler credentialsHandler = new DelegateCredentialsHandler() {
            @Override
            public Credentials getCredentials() {
                return new Credentials(ACCESS_KEY_ID.getBytes(), SECRET_ACCESS_KEY.getBytes(), null);
            }
        };
        builder.withHandler(credentialsHandler);
        try (DelegateCredentialsProvider provider = builder.build()) {
            CompletableFuture<Credentials> future = provider.getCredentials();
            Credentials credentials = future.get();
            assertTrue(Arrays.equals(credentials.getAccessKeyId(), ACCESS_KEY_ID.getBytes()));
            assertTrue(Arrays.equals(credentials.getSecretAccessKey(), SECRET_ACCESS_KEY.getBytes()));
            assertTrue(Arrays.equals(credentials.getSessionToken(), null));
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testDelegateException() {
        DelegateCredentialsProvider.DelegateCredentialsProviderBuilder builder = new DelegateCredentialsProvider.DelegateCredentialsProviderBuilder();
        DelegateCredentialsHandler credentialsHandler = new DelegateCredentialsHandler() {
            @Override
            public Credentials getCredentials() {
                throw new RuntimeException("Some exception. =)");
            }
        };
        boolean failed = false;
        builder.withHandler(credentialsHandler);
        try (DelegateCredentialsProvider provider = builder.build()) {
            CompletableFuture<Credentials> future = provider.getCredentials();
            Credentials credentials = future.get();
            assertTrue(Arrays.equals(credentials.getAccessKeyId(), ACCESS_KEY_ID.getBytes()));
            assertTrue(Arrays.equals(credentials.getSecretAccessKey(), SECRET_ACCESS_KEY.getBytes()));
            assertTrue(Arrays.equals(credentials.getSessionToken(), null));
        } catch (Exception ex) {
            failed = true;
        }
        assertTrue(failed);
    }

    @Test
    public void testCreateDestroyProfile_ValidCreds() throws IOException {
        Path confPath = Files.createTempFile("testCreateDestroyProfile_ValidProfile_conf_", "");
        Path credsPath = Files.createTempFile("testCreateDestroyProfile_ValidProfile_creds_", "");
        Files.write(credsPath, Arrays.asList("[default]", "aws_access_key_id=" + ACCESS_KEY_ID,
                "aws_secret_access_key=" + SECRET_ACCESS_KEY, "aws_session_token=" + SESSION_TOKEN));

        ProfileCredentialsProvider.Builder builder = ProfileCredentialsProvider.builder()
                .withConfigFileNameOverride(confPath.toString()).withCredentialsFileNameOverride(credsPath.toString());

        try (ProfileCredentialsProvider provider = builder.build()) {
            assertNotNull(provider);
            assertTrue(provider.getNativeHandle() != 0);

            Credentials credentials = provider.getCredentials().join();
            assertTrue(Arrays.equals(credentials.getAccessKeyId(), ACCESS_KEY_ID.getBytes()));
            assertTrue(Arrays.equals(credentials.getSecretAccessKey(), SECRET_ACCESS_KEY.getBytes()));
            assertTrue(Arrays.equals(credentials.getSessionToken(), SESSION_TOKEN.getBytes()));
        } finally {
            Files.deleteIfExists(credsPath);
            Files.deleteIfExists(confPath);
        }
    }

    @Test
    public void testCreateDestroyProfile_InvalidProfile() throws IOException {
        Path confPath = Files.createTempFile("testCreateDestroyProfile_ValidProfile_conf_", "");
        Path credsPath = Files.createTempFile("testCreateDestroyProfile_ValidProfile_creds_", "");

        ProfileCredentialsProvider.Builder builder = ProfileCredentialsProvider.builder()
                .withConfigFileNameOverride(confPath.toString()).withCredentialsFileNameOverride(credsPath.toString());

        try (ProfileCredentialsProvider provider = builder.build()) {
            fail("Expected builder.build() call to throw exception due to missing [default] section in profile files.");
        } catch (CrtRuntimeException e) {
            // Got correct exception, nothing to do
        } finally {
            Files.deleteIfExists(confPath);
            Files.deleteIfExists(credsPath);
        }
    }

    @Test
    public void testCreateDestroyProfile_MissingCreds() throws ExecutionException, InterruptedException, IOException {
        Path confPath = Files.createTempFile("testCreateDestroyProfile_ValidProfile_conf_", "");
        Path credsPath = Files.createTempFile("testCreateDestroyProfile_ValidProfile_creds_", "");
        Files.write(credsPath, Arrays.asList("[default]")); // Contains a section header but no actual credentials

        ProfileCredentialsProvider.Builder builder = ProfileCredentialsProvider.builder()
                .withConfigFileNameOverride(confPath.toString()).withCredentialsFileNameOverride(credsPath.toString());

        try (ProfileCredentialsProvider provider = builder.build()) {
            assertNotNull(provider);
            assertTrue(provider.getNativeHandle() != 0);

            try {
                provider.getCredentials().join();
                fail("Expected credential fetching to throw an exception since creds are missing from profile");
            } catch (CompletionException e) {
                assertNotNull(e.getCause());
                Throwable innerException = e.getCause();

                // Check that the right exception type caused the completion error in the future
                assertEquals("Failed to get a valid set of credentials", innerException.getMessage());
                assertEquals(RuntimeException.class, innerException.getClass());
            }
        } finally {
            Files.deleteIfExists(credsPath);
            Files.deleteIfExists(confPath);
        }
    }

    @Ignore // Enable this test if/when https://github.com/awslabs/aws-c-auth/issues/142 has
            // been resolved
    @Test
    public void testCreateDestroyEcs_ValidCreds()
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        skipIfNetworkUnavailable();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", httpExchange -> {
            String response = "{\"AccessKeyId\":\"ACCESS_KEY_ID\"," +
                    "\"SecretAccessKey\":\"SECRET_ACCESS_KEY\"," +
                    "\"Token\":\"TOKEN_TOKEN_TOKEN\"," +
                    "\"Expiration\":\"3000-05-03T04:55:54Z\"}";
            httpExchange.sendResponseHeaders(200, response.length());
            List<String> hv = new ArrayList<String>();
            hv.add("application/json");
            httpExchange.getResponseHeaders().put("Content-Type", hv);
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });
        server.start();

        EcsCredentialsProvider unit = EcsCredentialsProvider.builder()
                .withHost("127.0.0.1")
                .withPathAndQuery("/")
                .build();

        CompletableFuture<Credentials> credentialsFuture = unit.getCredentials();

        Credentials creds = credentialsFuture.get(8, TimeUnit.SECONDS);

        assertNotNull(creds);
        assertNotNull(creds.getAccessKeyId());
        assertEquals("ACCESS_KEY_ID", new String(creds.getAccessKeyId()));
        assertEquals("SECRET_ACCESS_KEY", new String(creds.getSecretAccessKey()));
        assertEquals("TOKEN_TOKEN_TOKEN", new String(creds.getSessionToken()));

        server.stop(0);
        unit.close();
    }

    @Test
    public void testCreateDestroyEcs_MissingCreds() {
        EcsCredentialsProvider.Builder builder = EcsCredentialsProvider.builder();

        try (EcsCredentialsProvider provider = builder.build()) {
            assertNotNull(provider);
            assertTrue(provider.getNativeHandle() != 0);

            try {
                provider.getCredentials().join();
                fail("Expected credential fetching to throw an exception since creds are missing from profile");
            } catch (CompletionException e) {
                assertNotNull(e.getCause());
                Throwable innerException = e.getCause();

                // Check that the right exception type caused the completion error in the future
                assertEquals("Failed to get a valid set of credentials", innerException.getMessage());
                assertEquals(RuntimeException.class, innerException.getClass());
            }
        }
    }

    @Test
    public void testCreateDestroyStsWebIdentity_InvalidEnv() {
        skipIfNetworkUnavailable();
        try (
                TlsContextOptions tlsContextOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsCtx = new TlsContext(tlsContextOptions);
                EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                StsWebIdentityCredentialsProvider unit = StsWebIdentityCredentialsProvider.builder()
                        .withClientBootstrap(bootstrap)
                        .withTlsContext(tlsCtx)
                        .build()) {

            fail("Expected StsWebIdentityCredentialsProvider construction to fail due to missing config state.");
        } catch (CrtRuntimeException e) {
            // Check that the right exception type caused the completion error in the future
            assertTrue(e.getMessage().startsWith("Failed to create STS web identity credentials provider"));
        }
    }

    @Test
    public void testCreateDestroySts_InvalidRole() {
        skipIfNetworkUnavailable();
        try (
                EventLoopGroup eventLoopGroup = new EventLoopGroup(1);
                HostResolver resolver = new HostResolver(eventLoopGroup);
                ClientBootstrap bootstrap = new ClientBootstrap(eventLoopGroup, resolver);
                StaticCredentialsProvider staticCP = new StaticCredentialsProvider.StaticCredentialsProviderBuilder()
                        .withAccessKeyId(ACCESS_KEY_ID.getBytes())
                        .withSecretAccessKey(SECRET_ACCESS_KEY.getBytes())
                        .withSessionToken(SESSION_TOKEN.getBytes())
                        .build();
                TlsContextOptions tlsContextOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsContext = new TlsContext(tlsContextOptions);
                StsCredentialsProvider unit = StsCredentialsProvider.builder()
                        .withCredsProvider(staticCP)
                        .withDurationSeconds(10)
                        .withSessionName("test-session")
                        .withRoleArn("invalid-role-arn")
                        .withTlsContext(tlsContext)
                        .withClientBootstrap(bootstrap)
                        .build()) {
            unit.getCredentials().join();
            fail("Expected builder.build() call to throw exception due to invalid STS configuration.");
        } catch (CompletionException e) {
            assertNotNull(e.getCause());
            Throwable innerException = e.getCause();

            // Check that the right exception type caused the completion error in the future
            assertEquals("Failed to get a valid set of credentials", innerException.getMessage());
            assertEquals(RuntimeException.class, innerException.getClass());
        }
    }

    @Test
    public void testGetCredentialsCognito() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(isCIEnvironmentSetUp());

        try (TlsContextOptions tlsContextOptions = TlsContextOptions.createDefaultClient();
             TlsContext tlsContext = new TlsContext(tlsContextOptions)) {

            CognitoCredentialsProvider.CognitoCredentialsProviderBuilder builder = new CognitoCredentialsProvider.CognitoCredentialsProviderBuilder();
            builder.withEndpoint("cognito-identity.us-east-1.amazonaws.com");
            builder.withIdentity(COGNITO_IDENTITY);
            builder.withTlsContext(tlsContext);

            try (CognitoCredentialsProvider provider = builder.build()) {
                CompletableFuture<Credentials> future = provider.getCredentials();
                Credentials credentials = future.get();

                assertNotNull(credentials.getAccessKeyId());
                assertNotNull(credentials.getSecretAccessKey());
                assertNotNull(credentials.getSessionToken());
            } catch (Exception ex) {
                fail(ex.getMessage());
            }
        }
    }

    @Test
    public void testGetCredentialsCognitoProxy() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(isCIEnvironmentSetUp());
        Assume.assumeTrue(isProxyEnvironmentSetUp());

        try (TlsContextOptions tlsContextOptions = TlsContextOptions.createDefaultClient();
             TlsContext tlsContext = new TlsContext(tlsContextOptions)) {

            HttpProxyOptions proxyOptions = new HttpProxyOptions();
            proxyOptions.setHost(TEST_HTTP_PROXY_HOST);
            proxyOptions.setPort(Integer.parseInt(TEST_HTTP_PROXY_PORT));

            CognitoCredentialsProvider.CognitoCredentialsProviderBuilder builder = new CognitoCredentialsProvider.CognitoCredentialsProviderBuilder();
            builder.withEndpoint("cognito-identity.us-east-1.amazonaws.com");
            builder.withIdentity(COGNITO_IDENTITY);
            builder.withTlsContext(tlsContext);
            builder.withHttpProxyOptions(proxyOptions);

            try (CognitoCredentialsProvider provider = builder.build()) {
                CompletableFuture<Credentials> future = provider.getCredentials();
                Credentials credentials = future.get();

                assertNotNull(credentials.getAccessKeyId());
                assertNotNull(credentials.getSecretAccessKey());
                assertNotNull(credentials.getSessionToken());
            } catch (Exception ex) {
                fail(ex.getMessage());
            }
        }
    }

    @Test
    public void testCreateCognitoMaximal() {
        skipIfNetworkUnavailable();
        Assume.assumeTrue(isCIEnvironmentSetUp());

        try (TlsContextOptions tlsContextOptions = TlsContextOptions.createDefaultClient();
             TlsContext tlsContext = new TlsContext(tlsContextOptions)) {

            CognitoCredentialsProvider.CognitoCredentialsProviderBuilder builder = new CognitoCredentialsProvider.CognitoCredentialsProviderBuilder();
            builder.withEndpoint("cognito-identity.us-east-1.amazonaws.com");
            builder.withIdentity(COGNITO_IDENTITY);
            builder.withTlsContext(tlsContext);
            builder.withLogin(new CognitoCredentialsProvider.CognitoLoginTokenPair("test", "token"));
            builder.withLogin(new CognitoCredentialsProvider.CognitoLoginTokenPair("garbage", "value"));

            try (CognitoCredentialsProvider provider = builder.build()) {
                ;
            } catch (Exception ex) {
                fail(ex.getMessage());
            }
        }
    }
}
