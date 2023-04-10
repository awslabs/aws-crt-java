/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.test;

import org.junit.Test;

import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.auth.credentials.CognitoCredentialsProvider.CognitoCredentialsProviderBuilder;
import software.amazon.awssdk.crt.auth.credentials.DefaultChainCredentialsProvider.DefaultChainCredentialsProviderBuilder;
import software.amazon.awssdk.crt.auth.credentials.StaticCredentialsProvider.StaticCredentialsProviderBuilder;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;


public class MqttClientConnectionMethodTest extends MqttClientConnectionFixture {
    public MqttClientConnectionMethodTest() {}

    /**
     * Conn_WS_MQTT311_UC1 - static credentials connect
    */
    @Test
    public void testWebsocketCredentialsStaticConnect()
    {
        skipIfNetworkUnavailable();
        skipIfCredentialsMissingStatic();
        StaticCredentialsProviderBuilder builder = new StaticCredentialsProviderBuilder();
        builder.withAccessKeyId(AWS_TEST_ACCESS_KEY.getBytes());
        builder.withSecretAccessKey(AWS_TEST_SECRET_ACCESS_KEY.getBytes());
        builder.withSessionToken(AWS_TEST_SESSION_TOKEN.getBytes());
        try (CredentialsProvider provider = builder.build();) {
            connectWebsocketsWithCredentialsProvider(provider);
            disconnect();
            close();
        }
    }

    /**
     * Conn_WS_MQTT311_UC2 - default credentials connect
     */
    @Test
    public void testWebsocketCredentialsDefault()
    {
        skipIfNetworkUnavailable();
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);)
        {
            DefaultChainCredentialsProviderBuilder builder = new DefaultChainCredentialsProviderBuilder();
            builder.withClientBootstrap(bootstrap);
            try (CredentialsProvider provider = builder.build();) {
                connectWebsocketsWithCredentialsProvider(provider);
                disconnect();
                close();
            }
        }
    }

    /**
     * Conn_WS_MQTT311_UC3 - Cognito Identity credentials connect
    * TODO: Make another test that supports logins
    */
    @Test
    public void testWebsocketCredentialsCognito()
    {
        skipIfNetworkUnavailable();
        try (EventLoopGroup elg = new EventLoopGroup(1);
            HostResolver hr = new HostResolver(elg);
            ClientBootstrap bootstrap = new ClientBootstrap(elg, hr);
            TlsContextOptions contextOptions = TlsContextOptions.createDefaultClient();
            TlsContext context = new TlsContext(contextOptions);)
        {
            CognitoCredentialsProviderBuilder builder = new CognitoCredentialsProviderBuilder();
            builder.withClientBootstrap(bootstrap);
            builder.withTlsContext(context);
            builder.withEndpoint(AWS_TEST_COGNITO_ENDPOINT);
            builder.withIdentity(AWS_TEST_COGNITO_IDENTITY);
            try (CredentialsProvider provider = builder.build();) {
                connectWebsocketsWithCredentialsProvider(provider);
                disconnect();
                close();
            }
        }
    }
};
