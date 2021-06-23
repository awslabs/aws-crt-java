/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.junit.Assume;
import org.junit.Test;
import static org.junit.Assert.*;
import software.amazon.awssdk.crt.*;
import software.amazon.awssdk.crt.auth.credentials.CachedCredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.DelegateCredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.DelegateCredentialsHandler;
import software.amazon.awssdk.crt.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;

public class CredentialsProviderTest extends CrtTestFixture {
    static private String ACCESS_KEY_ID = "access_key_id";
    static private String SECRET_ACCESS_KEY = "secret_access_key";
    static private String SESSION_TOKEN = "session_token";

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
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
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
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
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
    public void testCreateDestroyProfile_ValidCreds() throws IOException {
        Path confPath = Files.createTempFile("testCreateDestroyProfile_ValidProfile_conf_", "");
        Path credsPath = Files.createTempFile("testCreateDestroyProfile_ValidProfile_creds_", "");
        Files.write(credsPath, Arrays.asList(
                "[default]",
                "aws_access_key_id=" + ACCESS_KEY_ID,
                "aws_secret_access_key=" + SECRET_ACCESS_KEY,
                "aws_session_token=" + SESSION_TOKEN
        ));

        ProfileCredentialsProvider.Builder builder = ProfileCredentialsProvider
                .builder()
                .withConfigFileNameOverride(confPath.toString())
                .withCredentialsFileNameOverride(credsPath.toString());

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

        ProfileCredentialsProvider.Builder builder = ProfileCredentialsProvider
                .builder()
                .withConfigFileNameOverride(confPath.toString())
                .withCredentialsFileNameOverride(credsPath.toString());

        try {
            builder.build();
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

        ProfileCredentialsProvider.Builder builder = ProfileCredentialsProvider
                .builder()
                .withConfigFileNameOverride(confPath.toString())
                .withCredentialsFileNameOverride(credsPath.toString());

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
};
