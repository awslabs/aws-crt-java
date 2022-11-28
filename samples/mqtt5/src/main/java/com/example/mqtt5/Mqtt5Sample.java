/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package com.example.mqtt5;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.mqtt5.*;
import software.amazon.awssdk.crt.mqtt5.packets.*;

import software.amazon.awssdk.crt.mqtt5.packets.ConnectPacket.ConnectPacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.DisconnectPacket.DisconnectPacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.DisconnectPacket.DisconnectReasonCode;
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket.PublishPacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket.PayloadFormatIndicator;
import software.amazon.awssdk.crt.mqtt5.packets.SubscribePacket.SubscribePacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.SubscribePacket.RetainHandlingType;
import software.amazon.awssdk.crt.mqtt5.packets.UnsubscribePacket.UnsubscribePacketBuilder;

public class Mqtt5Sample {

    static String endpoint = "localhost";
    static Long port = 1883L;
    static String clientID = "MQTT5_Sample_Java_" + UUID.randomUUID().toString();
    static boolean useWebsockets = false;
    static boolean useTls = false;
    static boolean showHelp = false;

    static void printUsage() {
        System.out.println(
            "Usage:\n" +
            "  --help            This message\n"+
            "  --endpoint        MQTT5 endpoint hostname (optional, default=localhost)\n"+
            "  --port            MQTT5 endpoint port to use (optional, default=1883)\n"+
            "  --clientID        The ClientID to connect with (optional, default=MQTT5_Sample_Java_<UUID>)\n"+
            "  --use_websockets  If defined, websockets will be used (optional)\n"+
            "  --use_tls         If defined, TLS will be used (optional)"
        );
    }

    static void parseCommandLine(String[] args) {
        for (int idx = 0; idx < args.length; ++idx) {
            switch (args[idx]) {
                case "--help":
                    showHelp = true;
                    break;
                case "--endpoint":
                    if (idx + 1 < args.length) {
                        endpoint = args[++idx];
                    }
                    break;
                case "--port":
                    if (idx + 1 < args.length) {
                        port = Long.parseLong(args[++idx]);
                    }
                    break;
                case "--clientID":
                    if (idx + 1 < args.length) {
                        clientID = args[++idx];
                    }
                    break;
                case "--use_websockets":
                    useWebsockets = true;
                    break;
                case "--use_tls":
                    useTls = true;
                    break;
                default:
                    System.out.println("Unrecognized argument: " + args[idx]);
            }
        }
    }


    static final class SampleLifecycleEvents implements Mqtt5ClientOptions.LifecycleEvents {
        CompletableFuture<Void> connectedFuture = new CompletableFuture<>();
        CompletableFuture<Void> stopFuture = new CompletableFuture<>();

        @Override
        public void onAttemptingConnect(Mqtt5Client client, OnAttemptingConnectReturn onAttemptingConnectReturn) {
            System.out.println("[Lifecycle event] Client attempting connection...");
        }

        @Override
        public void onConnectionSuccess(Mqtt5Client client, ConnAckPacket connAckData, NegotiatedSettings negotiatedSettings) {
            System.out.println("[Lifecycle event] Client connection success...");
            connectedFuture.complete(null);
        }

        @Override
        public void onConnectionFailure(Mqtt5Client client, int failureCode, ConnAckPacket connAckData) {
            System.out.println("[Lifecycle event] Client connection failed...");
            connectedFuture.completeExceptionally(new Exception("Connection failure"));
        }

        @Override
        public void onDisconnection(Mqtt5Client client, int failureCode, DisconnectPacket disconnectData) {
            System.out.println("[Lifecycle event] Client disconnected...");
        }

        @Override
        public void onStopped(Mqtt5Client client, OnStoppedReturn onStoppedReturn) {
            System.out.println("[Lifecycle event] Client stopped...");
            stopFuture.complete(null);
        }
    }

    static final class SamplePublishEvents implements Mqtt5ClientOptions.PublishEvents {
        @Override
        public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn) {
            PublishPacket publishPacket = publishResult.getPublishPacket();
            System.out.println(
                "Message received:\n"+
                "  Topic: " + publishPacket.getTopic() + "\n" +
                "  Payload: " + new String(publishPacket.getPayload())
            );
            if (publishPacket.getUserProperties() != null) {
                List<UserProperty> userProperties = publishPacket.getUserProperties();
                for (int i = 0; i < userProperties.size(); i++) {
                    System.out.println("  Property: " + userProperties.get(i).key + " - " + userProperties.get(i).value);
                }
            }
        }
    }

    public static void main(String[] args) {

        parseCommandLine(args);
        if (showHelp) {
            printUsage();
            return;
        }

        SampleLifecycleEvents sampleLifecycleEvents = new SampleLifecycleEvents();
        SamplePublishEvents samplePublishEvents = new SamplePublishEvents();
        Consumer<Mqtt5WebsocketHandshakeTransformArgs> websocketTransform = null;
        TlsContext tlsContext = null;

        try {
            Mqtt5ClientOptions.Mqtt5ClientOptionsBuilder optionsBuilder = new Mqtt5ClientOptions.Mqtt5ClientOptionsBuilder(endpoint, port);

            optionsBuilder.withLifecycleEvents(sampleLifecycleEvents);
            optionsBuilder.withPublishEvents(samplePublishEvents);

            if (useWebsockets == true) {
                websocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                    @Override
                    public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                        t.complete(t.getHttpRequest());
                    }
                };
                optionsBuilder.withWebsocketHandshakeTransform(websocketTransform);
            }
            if (useTls == true) {
                try (TlsContextOptions tlsOptions = TlsContextOptions.createDefaultClient()) {
                    tlsOptions.withVerifyPeer(false);
                    tlsContext = new TlsContext(tlsOptions);
                    optionsBuilder.withTlsContext(tlsContext);
                }
            }

            ConnectPacketBuilder connectBuilder = new ConnectPacketBuilder();
            connectBuilder.withClientId(clientID);
            // Add a will
            PublishPacketBuilder willBuilder = new PublishPacketBuilder();
            willBuilder.withTopic("test/topic/will");
            willBuilder.withPayload("Goodbye".getBytes());
            willBuilder.withQOS(QOS.AT_MOST_ONCE);
            connectBuilder.withWill(willBuilder.build());
            // Add the connection options
            optionsBuilder.withConnectOptions(connectBuilder.build());

            try (Mqtt5Client client = new Mqtt5Client(optionsBuilder.build())) {
                // Connect and make sure it is successful
                try {
                    client.start();
                    sampleLifecycleEvents.connectedFuture.get(60, TimeUnit.SECONDS);
                    System.out.println("Connection status: " + client.getIsConnected());
                } catch (Exception ex) {
                    System.out.println("Could not connect! Exception: " + ex.toString());
                    System.exit(1);
                    return;
                }

                // Subscribe
                SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
                subscribePacketBuilder.withSubscription("test/topic", QOS.AT_LEAST_ONCE);
                // Make sure it is successful
                try {
                    SubAckPacket subAckPacket = client.subscribe(subscribePacketBuilder.build()).get(60, TimeUnit.SECONDS);
                    if (subAckPacket.getReasonCodes().get(0) != SubAckPacket.SubAckReasonCode.GRANTED_QOS_1) {
                        System.out.println("Could not subscribe! Error code: " + subAckPacket.getReasonCodes().get(0).toString());
                        System.exit(1);
                        return;
                    }
                } catch (Exception ex) {
                    System.out.println("Could not subscribe! Exception: " + ex.toString());
                    System.exit(1);
                    return;
                }

                // Publish
                PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
                publishPacketBuilder.withPayload("Hello World!".getBytes());
                publishPacketBuilder.withQOS(QOS.AT_LEAST_ONCE);
                publishPacketBuilder.withTopic("test/topic");
                // Add user properties
                List<UserProperty> publishProperties = new ArrayList<UserProperty>();
                publishProperties.add(new UserProperty("Red", "Blue"));
                publishProperties.add(new UserProperty("key", "value"));
                publishPacketBuilder.withUserProperties(publishProperties);
                // Publish 10 times and make sure they are successful
                try {
                    for (int i = 0; i < 10; i++) {
                        client.publish(publishPacketBuilder.build()).get(60, TimeUnit.SECONDS);
                        Thread.sleep(100);
                    }
                } catch (Exception ex) {
                    System.out.println("Could not publish! Exception: " + ex.toString());
                    System.exit(1);
                    return;
                }

                // Unsubscribe
                UnsubscribePacketBuilder unsubscribePacketBuilder = new UnsubscribePacketBuilder();
                unsubscribePacketBuilder.withSubscription("test/topic");
                // Make sure it is successful
                try {
                    client.unsubscribe(unsubscribePacketBuilder.build()).get(60, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    System.out.println("Could not unsubscribe! Exception: " + ex.toString());
                    System.exit(1);
                    return;
                }

                // Disconnect
                DisconnectPacketBuilder disconnectPacketBuilder = new DisconnectPacketBuilder();
                disconnectPacketBuilder.withReasonCode(DisconnectReasonCode.NORMAL_DISCONNECTION);
                // Make sure it is successful
                try {
                    client.stop(disconnectPacketBuilder.build());
                    sampleLifecycleEvents.stopFuture.get(60, TimeUnit.SECONDS);
                    System.out.println("Connection status: " + client.getIsConnected());
                } catch (Exception ex) {
                    System.out.println("Could not stop client! Exception: " + ex.toString());
                    System.exit(1);
                    return;
                }
            }
        } finally {}

        if (tlsContext != null) {
            tlsContext.close();
        }

        System.out.println("Waiting for no resources...");
        CrtResource.waitForNoResources();

        System.out.println("Finished sample!");
        System.exit(0);
    }
}
