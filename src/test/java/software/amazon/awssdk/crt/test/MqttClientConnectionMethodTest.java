/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

import org.junit.Assume;

import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.CognitoCredentialsProvider.CognitoCredentialsProviderBuilder;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder;
import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider.StaticCredentialsProviderBuilder;
import software.amazon.awssdk.crt.auth.credentials.X509CredentialsProvider.X509CredentialsProviderBuilder;
import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.http.HttpProxyOptions.HttpProxyConnectionType;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.Pkcs11Lib;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.io.TlsContextPkcs11Options;


/* For environment variable setup, see SetupCrossCICrtEnvironment in the CRT builder */
public class MqttClientConnectionMethodTest extends MqttClientConnectionFixture {
    public MqttClientConnectionMethodTest() {}

    /**
     * ============================================================
     * MQTT311 DIRECT IoT Core CONNECTION TEST CASES
     * ============================================================
     */

    /* MQTT311 ConnDC_Cred_UC1 - MQTT311 connect with Java Keystore */
    @Test
    public void ConnDC_Cred_UC1()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_FORMAT,
            AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_FILE, AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_PASSWORD,
            AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_CERT_ALIAS, AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_CERT_PASSWORD);
        try {
            java.security.KeyStore keyStore;
            keyStore = java.security.KeyStore.getInstance(AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_FORMAT);
            java.io.FileInputStream fileInputStream = new java.io.FileInputStream(AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_FILE);
            keyStore.load(fileInputStream, AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_PASSWORD.toCharArray());
            fileInputStream.close();

            try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsJavaKeystore(
                    keyStore,
                    AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_CERT_ALIAS,
                    AWS_TEST_MQTT311_IOT_CORE_KEYSTORE_CERT_PASSWORD);
                    TlsContext context = new TlsContext(contextOptions);)
                {
                    connectDirectWithConfig(
                        context,
                        AWS_TEST_MQTT311_IOT_CORE_HOST,
                        8883,
                        null,
                        null,
                        null);
                    disconnect();
                    close();
                }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            assertTrue("Exception ocurred running Java Keystore test!", ex == null);
        }
    }

    /* MQTT311 ConnDC_Cred_UC2 - MQTT311 connect with PKCS12 Key */
    @Test
    public void ConnDC_Cred_UC2()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_PKCS12_KEY,
            AWS_TEST_MQTT311_IOT_CORE_PKCS12_KEY_PASSWORD);
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsPkcs12(
                AWS_TEST_MQTT311_IOT_CORE_PKCS12_KEY,
                AWS_TEST_MQTT311_IOT_CORE_PKCS12_KEY_PASSWORD);
                TlsContext context = new TlsContext(contextOptions);)
            {
                connectDirectWithConfig(
                    context,
                    AWS_TEST_MQTT311_IOT_CORE_HOST,
                    8883,
                    null,
                    null,
                    null);
                disconnect();
                close();
            }
    }

    /* MQTT311 ConnDC_Cred_UC3 - MQTT311 connect with Windows Cert Store */
    @Test
    public void ConnDC_Cred_UC3()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_WINDOWS_PFX_CERT_NO_PASS,
            AWS_TEST_MQTT311_IOT_CORE_WINDOWS_CERT_STORE);
        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsWindowsCertStorePath(
            AWS_TEST_MQTT311_IOT_CORE_WINDOWS_CERT_STORE);
                TlsContext context = new TlsContext(contextOptions);)
            {
                connectDirectWithConfig(
                    context,
                    AWS_TEST_MQTT311_IOT_CORE_HOST,
                    8883,
                    null,
                    null,
                    null);
                disconnect();
                close();
            }
    }

    /* MQTT311 ConnDC_Cred_UC4 - MQTT311 connect with PKCS11 */
    @Test
    public void ConnDC_Cred_UC4()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_PKCS11_LIB,
            AWS_TEST_MQTT311_IOT_CORE_PKCS11_TOKEN_LABEL, AWS_TEST_MQTT311_IOT_CORE_PKCS11_PIN,
            AWS_TEST_MQTT311_IOT_CORE_PKCS11_PKEY_LABEL, AWS_TEST_MQTT311_IOT_CORE_PKCS11_CERT_FILE);
        try (
            Pkcs11Lib pkcs11Lib = new Pkcs11Lib(AWS_TEST_MQTT311_IOT_CORE_PKCS11_LIB);
            TlsContextPkcs11Options pkcs11Options = new TlsContextPkcs11Options(pkcs11Lib);
        )
        {
            pkcs11Options.withTokenLabel(AWS_TEST_MQTT311_IOT_CORE_PKCS11_TOKEN_LABEL);
            pkcs11Options.withUserPin(AWS_TEST_MQTT311_IOT_CORE_PKCS11_PIN);
            pkcs11Options.withPrivateKeyObjectLabel(AWS_TEST_MQTT311_IOT_CORE_PKCS11_PKEY_LABEL);
            pkcs11Options.withCertificateFilePath(AWS_TEST_MQTT311_IOT_CORE_PKCS11_CERT_FILE);

            try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsPkcs11(pkcs11Options);
                    TlsContext context = new TlsContext(contextOptions);)
                {
                    connectDirectWithConfig(
                        context,
                        AWS_TEST_MQTT311_IOT_CORE_HOST,
                        8883,
                        null,
                        null,
                        null);
                    disconnect();
                    close();
                }
        }
    }

    /**
     * ============================================================
     * MQTT311 WEBSOCKET IoT Core CONNECTION TEST CASES
     * ============================================================
     */

    /* MQTT311 ConnWS_Cred_UC1 - static credentials connect */
    @Test
    public void ConnWS_Cred_UC1()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_ROLE_CREDENTIAL_ACCESS_KEY,
            AWS_TEST_MQTT311_ROLE_CREDENTIAL_SECRET_ACCESS_KEY, AWS_TEST_MQTT311_ROLE_CREDENTIAL_SESSION_TOKEN);

        StaticCredentialsProviderBuilder builder = new StaticCredentialsProviderBuilder();
        builder.withAccessKeyId(AWS_TEST_MQTT311_ROLE_CREDENTIAL_ACCESS_KEY.getBytes());
        builder.withSecretAccessKey(AWS_TEST_MQTT311_ROLE_CREDENTIAL_SECRET_ACCESS_KEY.getBytes());
        builder.withSessionToken(AWS_TEST_MQTT311_ROLE_CREDENTIAL_SESSION_TOKEN.getBytes());

        try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
            TlsContext tlsContext = new TlsContext(tlsOptions);
            CredentialsProvider provider = builder.build();) {
            connectWebsocketsWithCredentialsProvider(provider, AWS_TEST_MQTT311_IOT_CORE_HOST, 443, tlsContext, null, null, null);
            disconnect();
            close();
        }
    }

    /* MQTT311 ConnWS_Cred_UC2 - default credentials connect */
    @Test
    public void ConnWS_Cred_UC2()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_IOT_CORE_HOST);
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);)
        {
            DefaultChainCredentialsProviderBuilder builder = new DefaultChainCredentialsProviderBuilder();
            builder.withClientBootstrap(bootstrap);
            try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsContext = new TlsContext(tlsOptions);
                CredentialsProvider provider = builder.build();) {
                connectWebsocketsWithCredentialsProvider(provider, AWS_TEST_MQTT311_IOT_CORE_HOST, 443, tlsContext, null, null, null);
                disconnect();
                close();
            }
        }
    }

    /**
     * MQTT311 ConnWS_Cred_UC3 - Cognito Identity credentials connect
     */
    @Test
    public void ConnWS_Cred_UC3()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_COGNITO_ENDPOINT, AWS_TEST_MQTT311_COGNITO_IDENTITY);
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            TlsContextOptions contextOptions = TlsContextOptions.createDefaultClient();
            TlsContext context = new TlsContext(contextOptions);)
        {
            CognitoCredentialsProviderBuilder builder = new CognitoCredentialsProviderBuilder();
            builder.withClientBootstrap(bootstrap);
            builder.withTlsContext(context);
            builder.withEndpoint(AWS_TEST_MQTT311_COGNITO_ENDPOINT);
            builder.withIdentity(AWS_TEST_MQTT311_COGNITO_IDENTITY);
            try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsContext = new TlsContext(tlsOptions);
                CredentialsProvider provider = builder.build();) {
                connectWebsocketsWithCredentialsProvider(provider, AWS_TEST_MQTT311_IOT_CORE_HOST, 443, tlsContext, null, null, null);
                disconnect();
                close();
            }
        }
    }

    /* MQTT311 ConnWS_Cred_UC4 - X509 credentials connect */
    @Test
    public void ConnWS_Cred_UC4()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_X509_CERT,
            AWS_TEST_MQTT311_IOT_CORE_X509_KEY, AWS_TEST_MQTT311_IOT_CORE_X509_ENDPOINT,
            AWS_TEST_MQTT311_IOT_CORE_X509_ROLE_ALIAS, AWS_TEST_MQTT311_IOT_CORE_X509_THING_NAME);
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            TlsContextOptions x509TlsOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT311_IOT_CORE_X509_CERT,
                AWS_TEST_MQTT311_IOT_CORE_X509_KEY);
            TlsContext x509TlsContext = new TlsContext(x509TlsOptions);)
        {
            X509CredentialsProviderBuilder builder = new X509CredentialsProviderBuilder();
            builder.withTlsContext(x509TlsContext);
            builder.withEndpoint(AWS_TEST_MQTT311_IOT_CORE_X509_ENDPOINT);
            builder.withRoleAlias(AWS_TEST_MQTT311_IOT_CORE_X509_ROLE_ALIAS);
            builder.withThingName(AWS_TEST_MQTT311_IOT_CORE_X509_THING_NAME);

            try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient();
                TlsContext tlsContext = new TlsContext(tlsOptions);
                CredentialsProvider provider = builder.build();) {
                connectWebsocketsWithCredentialsProvider(provider, AWS_TEST_MQTT311_IOT_CORE_HOST, 443, tlsContext, null, null, null);
                disconnect();
                close();
            }
        }
    }

    /**
     * ============================================================
     * MQTT311 DIRECT CONNECTION TEST CASES
     * ============================================================
     */

    /* MQTT311 ConnDC_UC1 - MQTT311 connect without authentication */
    @Test
    public void ConnDC_UC1()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_DIRECT_MQTT_HOST, AWS_TEST_MQTT311_DIRECT_MQTT_PORT);
        connectDirectWithConfig(
            null,
            AWS_TEST_MQTT311_DIRECT_MQTT_HOST,
            Integer.parseInt(AWS_TEST_MQTT311_DIRECT_MQTT_PORT),
            null,
            null,
            null);
        disconnect();
        close();
    }

    /* MQTT311 ConnDC_UC2 - MQTT311 connect with basic authentication */
    @Test
    public void ConnDC_UC2()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_DIRECT_MQTT_BASIC_AUTH_HOST, AWS_TEST_MQTT311_DIRECT_MQTT_BASIC_AUTH_PORT,
            AWS_TEST_MQTT311_BASIC_AUTH_USERNAME, AWS_TEST_MQTT311_BASIC_AUTH_PASSWORD);
        connectDirectWithConfig(
            null,
            AWS_TEST_MQTT311_DIRECT_MQTT_BASIC_AUTH_HOST,
            Integer.parseInt(AWS_TEST_MQTT311_DIRECT_MQTT_BASIC_AUTH_PORT),
            AWS_TEST_MQTT311_BASIC_AUTH_USERNAME,
            AWS_TEST_MQTT311_BASIC_AUTH_PASSWORD,
            null);
        disconnect();
        close();
    }

    /* MQTT311 ConnDC_UC3 - MQTT311 connect with TLS */
    @Test
    public void ConnDC_UC3()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_DIRECT_MQTT_TLS_HOST, AWS_TEST_MQTT311_DIRECT_MQTT_TLS_PORT);
        try (TlsContextOptions contextOptions = TlsContextOptions.createDefaultClient();)
            {
                contextOptions.verifyPeer = false;
                try (TlsContext context = new TlsContext(contextOptions);)
                {
                    connectDirectWithConfig(
                        context,
                        AWS_TEST_MQTT311_DIRECT_MQTT_TLS_HOST,
                        Integer.parseInt(AWS_TEST_MQTT311_DIRECT_MQTT_TLS_PORT),
                        null,
                        null,
                        null);
                    disconnect();
                    close();
                }
            }
    }

    /* MQTT311 ConnDC_UC4 - MQTT311 connect with mTLS */
    @Test
    public void ConnDC_UC4()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_IOT_CORE_HOST, AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
            AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);
        int port = 8883;

        try (TlsContextOptions contextOptions = TlsContextOptions.createWithMtlsFromPath(
                AWS_TEST_MQTT311_IOT_CORE_RSA_CERT,
                AWS_TEST_MQTT311_IOT_CORE_RSA_KEY);)
            {
                if (TlsContextOptions.isAlpnSupported()) {
                    contextOptions.withAlpnList("x-amzn-mqtt-ca");
                    port = TEST_PORT_ALPN;
                }
                try (TlsContext context = new TlsContext(contextOptions);)
                {
                    connectDirectWithConfig(
                        context,
                        AWS_TEST_MQTT311_IOT_CORE_HOST,
                        port,
                        null,
                        null,
                        null);
                    disconnect();
                    close();
                }
            }
    }

    /* MQTT311 ConnDC_UC5 - MQTT311 connect with proxy */
    @Test
    public void ConnDC_UC5()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_DIRECT_MQTT_TLS_HOST, AWS_TEST_MQTT311_DIRECT_MQTT_TLS_PORT,
            AWS_TEST_MQTT311_PROXY_HOST, AWS_TEST_MQTT311_PROXY_PORT);
        HttpProxyOptions proxyOptions = new HttpProxyOptions();
        proxyOptions.setHost(AWS_TEST_MQTT311_PROXY_HOST);
        proxyOptions.setPort(Integer.parseInt(AWS_TEST_MQTT311_PROXY_PORT));
        proxyOptions.setConnectionType(HttpProxyConnectionType.Tunneling);

        try (TlsContextOptions contextOptions = TlsContextOptions.createDefaultClient();)
            {
                contextOptions.verifyPeer = false;
                try (TlsContext context = new TlsContext(contextOptions);)
                {
                    connectDirectWithConfig(
                        context,
                        AWS_TEST_MQTT311_DIRECT_MQTT_TLS_HOST,
                        Integer.parseInt(AWS_TEST_MQTT311_DIRECT_MQTT_TLS_PORT),
                        null,
                        null,
                        proxyOptions);
                    disconnect();
                    close();
                }
            }

    }

    /**
     * ============================================================
     * MQTT311 WEBSOCKET CONNECT TEST CASES
     * ============================================================
     */

    /* MQTT311 ConnWS_UC1 - MQTT311 websocket minimal connect */
    @Test
    public void ConnWS_UC1()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(AWS_TEST_MQTT311_WS_MQTT_HOST, AWS_TEST_MQTT311_WS_MQTT_PORT);
        connectWebsocketsWithCredentialsProvider(
            null,
            AWS_TEST_MQTT311_WS_MQTT_HOST,
            Integer.parseInt(AWS_TEST_MQTT311_WS_MQTT_PORT),
            null,
            null,
            null,
            null);
        disconnect();
        close();
    }

    /* MQTT311 ConnWS_UC2 - MQTT311 with basic auth */
    @Test
    public void ConnWS_UC2()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_WS_MQTT_BASIC_AUTH_HOST, AWS_TEST_MQTT311_WS_MQTT_BASIC_AUTH_PORT,
            AWS_TEST_MQTT311_BASIC_AUTH_USERNAME, AWS_TEST_MQTT311_BASIC_AUTH_PASSWORD);
        connectWebsocketsWithCredentialsProvider(
            null,
            AWS_TEST_MQTT311_WS_MQTT_BASIC_AUTH_HOST,
            Integer.parseInt(AWS_TEST_MQTT311_WS_MQTT_BASIC_AUTH_PORT),
            null,
            AWS_TEST_MQTT311_BASIC_AUTH_USERNAME,
            AWS_TEST_MQTT311_BASIC_AUTH_PASSWORD,
            null);
        disconnect();
        close();
    }

    /* MQTT311 ConnWS_UC3 - MQTT311 with TLS */
    @Test
    public void ConnWS_UC3()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_WS_MQTT_TLS_HOST, AWS_TEST_MQTT311_WS_MQTT_TLS_PORT);
        try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient()) {
            tlsOptions.withVerifyPeer(false);
            try (TlsContext tlsContext = new TlsContext(tlsOptions);)
            {
                connectWebsocketsWithCredentialsProvider(
                    null,
                    AWS_TEST_MQTT311_WS_MQTT_TLS_HOST,
                    Integer.parseInt(AWS_TEST_MQTT311_WS_MQTT_TLS_PORT),
                    tlsContext,
                    null,
                    null,
                    null);
                disconnect();
                close();
            }
        }
    }

    /* MQTT311 ConnWS_UC4 - MQTT311 with proxy */
    @Test
    public void ConnWS_UC4()
    {
        skipIfNetworkUnavailable();
        Assume.assumeNotNull(
            AWS_TEST_MQTT311_WS_MQTT_TLS_HOST, AWS_TEST_MQTT311_WS_MQTT_TLS_PORT,
            AWS_TEST_MQTT311_PROXY_HOST, AWS_TEST_MQTT311_PROXY_PORT);
        HttpProxyOptions proxyOptions = new HttpProxyOptions();
        proxyOptions.setHost(AWS_TEST_MQTT311_PROXY_HOST);
        proxyOptions.setPort(Integer.parseInt(AWS_TEST_MQTT311_PROXY_PORT));
        proxyOptions.setConnectionType(HttpProxyConnectionType.Tunneling);

        try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient()) {
            tlsOptions.withVerifyPeer(false);
            try (TlsContext tlsContext = new TlsContext(tlsOptions);)
            {
                connectWebsocketsWithCredentialsProvider(
                    null,
                    AWS_TEST_MQTT311_WS_MQTT_TLS_HOST,
                    Integer.parseInt(AWS_TEST_MQTT311_WS_MQTT_TLS_PORT),
                    tlsContext,
                    null,
                    null,
                    proxyOptions);
                disconnect();
                close();
            }
        }
    }
};
