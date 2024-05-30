/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogSubject;
import software.amazon.awssdk.crt.CrtPlatform;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.auth.credentials.Credentials;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Assume;

import java.util.Optional;

public class CrtTestFixture {

    private CrtTestContext context;

    public final CrtTestContext getContext() {
        return context;
    }

    private static void SetPropertyFromEnv(String name) {
        String propertyValue = System.getenv(name);
        if (propertyValue != null) {
            System.setProperty(name, propertyValue);
        }
    }

    // Setup System properties from environment variables set by builder for use by
    // unit tests.
    private static void SetupTestProperties() {
        SetPropertyFromEnv("AWS_GRAAL_VM_CI");
        SetPropertyFromEnv("AWS_TEST_IS_CI");
        SetPropertyFromEnv("AWS_TEST_MQTT311_ROOT_CA");
        SetPropertyFromEnv("ENDPOINT");
        SetPropertyFromEnv("REGION");

        // Cognito
        SetPropertyFromEnv("AWS_TEST_MQTT311_COGNITO_ENDPOINT");
        SetPropertyFromEnv("AWS_TEST_MQTT311_COGNITO_IDENTITY");

        // Proxy
        SetPropertyFromEnv("AWS_TEST_HTTP_PROXY_HOST");
        SetPropertyFromEnv("AWS_TEST_HTTP_PROXY_PORT");
        SetPropertyFromEnv("AWS_TEST_HTTPS_PROXY_HOST");
        SetPropertyFromEnv("AWS_TEST_HTTPS_PROXY_PORT");
        SetPropertyFromEnv("AWS_TEST_HTTP_PROXY_BASIC_HOST");
        SetPropertyFromEnv("AWS_TEST_HTTP_PROXY_BASIC_PORT");

        // Static credential related
        SetPropertyFromEnv("AWS_TEST_MQTT311_ROLE_CREDENTIAL_ACCESS_KEY");
        SetPropertyFromEnv("AWS_TEST_MQTT311_ROLE_CREDENTIAL_SECRET_ACCESS_KEY");
        SetPropertyFromEnv("AWS_TEST_MQTT311_ROLE_CREDENTIAL_SESSION_TOKEN");

        // Custom Key Ops
        SetPropertyFromEnv("AWS_TEST_MQTT311_CUSTOM_KEY_OPS_KEY");
        SetPropertyFromEnv("AWS_TEST_MQTT311_CUSTOM_KEY_OPS_CERT");

        // MQTT311 Codebuild/Direct connections data
        SetPropertyFromEnv("AWS_TEST_MQTT311_DIRECT_MQTT_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT311_DIRECT_MQTT_PORT");
        SetPropertyFromEnv("AWS_TEST_MQTT311_DIRECT_MQTT_BASIC_AUTH_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT311_DIRECT_MQTT_BASIC_AUTH_PORT");
        SetPropertyFromEnv("AWS_TEST_MQTT311_DIRECT_MQTT_TLS_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT311_DIRECT_MQTT_TLS_PORT");

        // MQTT311 Codebuild/Websocket connections data
        SetPropertyFromEnv("AWS_TEST_MQTT311_WS_MQTT_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT311_WS_MQTT_PORT");
        SetPropertyFromEnv("AWS_TEST_MQTT311_WS_MQTT_BASIC_AUTH_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT311_WS_MQTT_BASIC_AUTH_PORT");
        SetPropertyFromEnv("AWS_TEST_MQTT311_WS_MQTT_TLS_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT311_WS_MQTT_TLS_PORT");

        // MQTT311 Codebuild misc connections data
        SetPropertyFromEnv("AWS_TEST_MQTT311_BASIC_AUTH_USERNAME");
        SetPropertyFromEnv("AWS_TEST_MQTT311_BASIC_AUTH_PASSWORD");
        SetPropertyFromEnv("AWS_TEST_MQTT311_CERTIFICATE_FILE");
        SetPropertyFromEnv("AWS_TEST_MQTT311_KEY_FILE");

        // MQTT311 IoT Endpoint, Key, Cert
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_RSA_CERT");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_RSA_KEY");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_ECC_CERT");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_ECC_KEY");

        // MQTT311 Proxy
        SetPropertyFromEnv("AWS_TEST_MQTT311_PROXY_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT311_PROXY_PORT");

        // MQTT311 Keystore
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_FORMAT");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_FILE");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_PASSWORD");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_CERT_ALIAS");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_CERT_PASSWORD");

        // MQTT311 PKCS12
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_PKCS12_KEY");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_PKCS12_KEY_PASSWORD");

        // PKCS11
        SetPropertyFromEnv("AWS_TEST_PKCS11_LIB");
        SetPropertyFromEnv("AWS_TEST_PKCS11_TOKEN_LABEL");
        SetPropertyFromEnv("AWS_TEST_PKCS11_PIN");
        SetPropertyFromEnv("AWS_TEST_PKCS11_PKEY_LABEL");
        SetPropertyFromEnv("AWS_TEST_PKCS11_CERT_FILE");
        SetPropertyFromEnv("AWS_TEST_PKCS11_CA_FILE");

        // MQTT311 X509
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_X509_CERT");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_X509_KEY");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_X509_ENDPOINT");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_X509_ROLE_ALIAS");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_X509_THING_NAME");

        // MQTT311 Windows Cert Store
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS");
        SetPropertyFromEnv("AWS_TEST_MQTT311_IOT_CORE_WINDOWS_CERT_STORE");

        // MQTT5 Codebuild/Direct connections data
        SetPropertyFromEnv("AWS_TEST_MQTT5_DIRECT_MQTT_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT5_DIRECT_MQTT_PORT");
        SetPropertyFromEnv("AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT5_DIRECT_MQTT_BASIC_AUTH_PORT");
        SetPropertyFromEnv("AWS_TEST_MQTT5_DIRECT_MQTT_TLS_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT5_DIRECT_MQTT_TLS_PORT");

        // MQTT5 Codebuild/Websocket connections data
        SetPropertyFromEnv("AWS_TEST_MQTT5_WS_MQTT_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT5_WS_MQTT_PORT");
        SetPropertyFromEnv("AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT5_WS_MQTT_BASIC_AUTH_PORT");
        SetPropertyFromEnv("AWS_TEST_MQTT5_WS_MQTT_TLS_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT5_WS_MQTT_TLS_PORT");

        // MQTT5 Codebuild misc connections data
        SetPropertyFromEnv("AWS_TEST_MQTT5_BASIC_AUTH_USERNAME");
        SetPropertyFromEnv("AWS_TEST_MQTT5_BASIC_AUTH_PASSWORD");
        SetPropertyFromEnv("AWS_TEST_MQTT5_CERTIFICATE_FILE");
        SetPropertyFromEnv("AWS_TEST_MQTT5_KEY_FILE");

        // MQTT5 Proxy
        SetPropertyFromEnv("AWS_TEST_MQTT5_PROXY_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT5_PROXY_PORT");

        // MQTT5 Endpoint/Host credential
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_HOST");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_REGION");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_RSA_CERT");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_RSA_KEY");

        // MQTT5 Static credential related
        SetPropertyFromEnv("AWS_TEST_MQTT5_ROLE_CREDENTIAL_ACCESS_KEY");
        SetPropertyFromEnv("AWS_TEST_MQTT5_ROLE_CREDENTIAL_SECRET_ACCESS_KEY");
        SetPropertyFromEnv("AWS_TEST_MQTT5_ROLE_CREDENTIAL_SESSION_TOKEN");

        // MQTT5 Cognito
        SetPropertyFromEnv("AWS_TEST_MQTT5_COGNITO_ENDPOINT");
        SetPropertyFromEnv("AWS_TEST_MQTT5_COGNITO_IDENTITY");

        // MQTT5 Keystore
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FORMAT");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_FILE");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_PASSWORD");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_ALIAS");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_KEYSTORE_CERT_PASSWORD");

        // MQTT5 PKCS12
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_PKCS12_KEY_PASSWORD");

        // MQTT5 X509
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_X509_CERT");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_X509_KEY");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_X509_ENDPOINT");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_X509_ROLE_ALIAS");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_X509_THING_NAME");

        // MQTT5 Windows Cert Store
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS");
        SetPropertyFromEnv("AWS_TEST_MQTT5_IOT_CORE_WINDOWS_CERT_STORE");

        // MQTT5 Custom Key Ops (so we don't have to make a new file just for a single
        // test)
        SetPropertyFromEnv("AWS_TEST_MQTT5_CUSTOM_KEY_OPS_CERT");
        SetPropertyFromEnv("AWS_TEST_MQTT5_CUSTOM_KEY_OPS_KEY");

        SetPropertyFromEnv("AWS_TEST_BASIC_AUTH_USERNAME");
        SetPropertyFromEnv("AWS_TEST_BASIC_AUTH_PASSWORD");
    }

    /* The function will be run once before any of the test methods in the class */
    @BeforeClass
    public static void setupOnce() {
        // We only want to see the CRT logs if the test fails.
        // Surefire has a redirectTestOutputToFile option, but that doesn't
        // capture what the CRT logger writes to stdout or stderr.
        // Our workaround is to have the CRT logger write to log.txt.
        // We clear the file for each new test by restarting the logger.
        // We stop all tests when one fails (see FailFastListener) so that
        // a valuable log.txt isn't overwritten.
        if (System.getProperty("aws.crt.aws_trace_log_per_test") != null) {
            Log.initLoggingToFile(Log.LogLevel.Trace, "log.txt");
        }
        CrtPlatform platform = CRT.getPlatformImpl();
        if (platform != null) {
            platform.setupOnce();
        }else {
            SetupTestProperties();
        }
    }

    /* The setup function will be run before every test */
    @Before
    public void setup() {
        Log.log(Log.LogLevel.Debug, LogSubject.JavaCrtGeneral, "CrtTestFixture setup begin");

        // TODO this CrtTestContext should be removed as we are using System Properties
        // for tests now.
        context = new CrtTestContext();
    }

    @After
    public void tearDown() {
        Log.log(Log.LogLevel.Debug, LogSubject.JavaCrtGeneral, "CrtTestFixture tearDown begin");
        CrtPlatform platform = CRT.getPlatformImpl();
        if (platform != null) {
            platform.testTearDown(context);
        }

        context = null;

        CrtResource.waitForNoResources();

        if (CRT.getOSIdentifier() != "android") {
            try {
                Runtime.getRuntime().gc();
                CrtMemoryLeakDetector.nativeMemoryLeakCheck();
            } catch (Exception e) {
                throw new RuntimeException("Memory leak from native resource detected!");
            }
        }
        Log.log(Log.LogLevel.Debug, LogSubject.JavaCrtGeneral, "CrtTestFixture tearDown end");
    }

    protected TlsContext createTlsContextOptions(byte[] trustStore) {
        try (TlsContextOptions tlsOpts = configureTlsContextOptions(TlsContextOptions.createDefaultClient(),
                trustStore)) {
            return new TlsContext(tlsOpts);
        }
    }

    protected TlsContextOptions configureTlsContextOptions(TlsContextOptions tlsOpts, byte[] trustStore) {
        if (trustStore != null) {
            tlsOpts.withCertificateAuthority(new String(trustStore));
        }
        return tlsOpts;
    }

    private Optional<Credentials> credentials = null;

    protected boolean hasAwsCredentials() {
        if (credentials == null) {
            try {
                try (EventLoopGroup elg = new EventLoopGroup(1);
                        HostResolver hostResolver = new HostResolver(elg);
                        ClientBootstrap clientBootstrap = new ClientBootstrap(elg, hostResolver)) {

                    try (DefaultChainCredentialsProvider provider = ((new DefaultChainCredentialsProviderBuilder())
                            .withClientBootstrap(clientBootstrap)).build()) {
                        credentials = Optional.of(provider.getCredentials().get());
                    }
                }
            } catch (Exception ex) {
                credentials = Optional.empty();
            }
        }
        return credentials.isPresent();
    }

    protected void skipIfNetworkUnavailable() {
        Assume.assumeTrue(System.getProperty("NETWORK_TESTS_DISABLED") == null);
    }

    protected void skipIfLocalhostUnavailable() {
        Assume.assumeTrue(System.getProperty("aws.crt.localhost") != null);
    }

    protected void skipIfAndroid() {
        CrtPlatform platform = CRT.getPlatformImpl();
        if (platform != null) {
            Assume.assumeFalse(platform.getOSIdentifier().contains("android"));
        }
    }
}
