/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package canary.mqtt5;

import java.io.PrintWriter;
import java.net.SocketOption;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.Log.LogSubject;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.io.EventLoopGroup;
import software.amazon.awssdk.crt.io.HostResolver;
import software.amazon.awssdk.crt.io.SocketOptions;
import software.amazon.awssdk.crt.io.TlsContext;
import software.amazon.awssdk.crt.io.TlsContextOptions;
import software.amazon.awssdk.crt.io.ExponentialBackoffRetryOptions.JitterMode;
import software.amazon.awssdk.crt.mqtt5.*;
import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.Mqtt5ClientOptionsBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.*;

import software.amazon.awssdk.crt.mqtt5.packets.ConnectPacket.ConnectPacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.DisconnectPacket.DisconnectPacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.DisconnectPacket.DisconnectReasonCode;
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket.PublishPacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.PublishPacket.PayloadFormatIndicator;
import software.amazon.awssdk.crt.mqtt5.packets.SubscribePacket.SubscribePacketBuilder;
import software.amazon.awssdk.crt.mqtt5.packets.SubscribePacket.RetainHandlingType;
import software.amazon.awssdk.crt.mqtt5.packets.UnsubscribePacket.UnsubscribePacketBuilder;

public class Mqtt5Canary {

    static String configEndpoint = "localhost";
    static Long configPort = 1883L;
    static String configCaFile = null;
    static String configCertFile = null;
    static String configKeyFile = null;
    static String configClientID = "MQTT5_Sample_Java_" + UUID.randomUUID().toString();
    static boolean configUseWebsockets = false;
    static boolean configUseTls = false;
    static Integer configThreads = 8;
    static Integer configClients = 3;
    static Integer configTps = 6;
    static Long configSeconds = 600L;
    static boolean configShowHelp = false;
    static boolean configLogStdout = true;
    static boolean configLogAWS = false;
    static LogLevel configLogAWSLevel = LogLevel.Debug;
    static String configLogFile;
    static PrintWriter configFilePrinter;

    static List<Mqtt5Client> clients = new ArrayList<Mqtt5Client>();
    static List<ClientsData> clientsData = new ArrayList<ClientsData>();
    static TlsContext clientsContext = null;
    static EventLoopGroup clientsEventLoopGroup;
    static HostResolver clientsHostResolver;
    static ClientBootstrap clientsBootstrap;
    static SocketOptions clientsSocketOptions;
    static Consumer<Mqtt5WebsocketHandshakeTransformArgs> clientsWebsocketTransform = null;
    static CanaryLifecycleEvents clientsLifecycleEvents = new CanaryLifecycleEvents();
    static CanaryPublishEvents clientsPublishEvents = new CanaryPublishEvents();

    static Random random = new Random();
    static java.time.LocalDateTime startDateTime;

    static int operationFutureWaitTime = 30;

    static void printUsage() {
        System.out.println(
            "Usage:\n" +
            "  --help            This message\n"+
            "  --endpoint        MQTT5 endpoint hostname (optional, default=localhost)\n"+
            "  --port            MQTT5 endpoint port to use (optional, default=1883)\n"+
            "  --ca_file         A path to a CA certificate file (optional)\n"+
            "  --cert            A path to a certificate file (optional, will use mTLS if defined)\n" +
            "  --key             A path to a private key file (optional, will use mTLS if defined)\n" +
            "  --clientID        The ClientID to connect with (optional, default=MQTT5_Sample_Java_<UUID>)\n"+
            "  --use_websockets  If defined, websockets will be used (optional)\n"+
            "  --use_tls         If defined, TLS (or mTLS) will be used (optional)\n"+
            "\n"+
            " --threads          The number of EventLoop group threads to use (optional, default=8)\n"+
            " --clients          The number of clients to use (optional, default=3, max=50)\n"+
            " --tps              The number of seconds to wait after performing an operation (optional, default=12)\n"+
            " --seconds          The number of seconds to run the Canary test (optional, default=600)\n"+
            " --log_console      If defined, logging will print to stdout (optional, default=true, type=boolean)\n"+
            " --log_aws          If defined, logging will occur using the AWS Logger (optional, type=boolean) \n"+
            " --log_aws_level    If defined, logging to AWS Logger will use that log level (optional, default=Debug)\n"+
            " --log_file         If defined, logging will be written to this file (optional)"
        );
    }

    static void parseCommandLine(String[] args) {
        for (int idx = 0; idx < args.length; ++idx) {
            switch (args[idx]) {
                case "--help":
                    configShowHelp = true;
                    break;
                case "--endpoint":
                    if (idx + 1 < args.length) {
                        configEndpoint = args[++idx];
                    }
                    break;
                case "--port":
                    if (idx + 1 < args.length) {
                        configPort = Long.parseLong(args[++idx]);
                    }
                    break;
                case "--ca_file":
                    if (idx + 1 < args.length) {
                        configCaFile = args[++idx];
                    }
                    break;
                case "--cert":
                    if (idx + 1 < args.length) {
                        configCertFile = args[++idx];
                    }
                    break;
                case "--key":
                    if (idx + 1 < args.length) {
                        configKeyFile = args[++idx];
                    }
                    break;
                case "--clientID":
                    if (idx + 1 < args.length) {
                        configClientID = args[++idx];
                    }
                    break;
                case "--use_websockets":
                    configUseWebsockets = true;
                    break;
                case "--use_tls":
                    configUseTls = true;
                    break;
                case "--threads":
                    if (idx + 1 < args.length) {
                        configThreads = Integer.parseInt(args[++idx]);
                    }
                    break;
                case "--clients":
                    if (idx + 1 < args.length) {
                        configClients = Integer.parseInt(args[++idx]);
                    }
                    break;
                case "--tps":
                    if (idx + 1 < args.length) {
                        configTps = Integer.parseInt(args[++idx]);
                    }
                    break;
                case "--seconds":
                    if (idx + 1 < args.length) {
                        configSeconds = Long.parseLong(args[++idx]);
                    }
                    break;
                case "--log_console":
                    if (idx + 1 < args.length) {
                        configLogStdout = Boolean.parseBoolean(args[++idx]);
                    }
                    break;
                case "--log_aws":
                    if (idx + 1 < args.length) {
                        configLogAWS = Boolean.parseBoolean(args[++idx]);
                    }
                    break;
                case "--log_aws_level":
                    if (idx + 1 < args.length) {
                        configLogAWSLevel = LogLevel.valueOf(args[++idx]);
                    }
                    break;
                case "--log_file":
                    if (idx + 1 < args.length) {
                        configLogFile = args[++idx];
                    }
                    break;
                default:
                    System.out.println("Unrecognized argument: " + args[idx]);
            }
        }
    }

    static void PrintLog(String message) {
        if (configLogStdout == true) {
            System.out.println("[CANARY] " + message);
        }
        if (configFilePrinter != null) {
            configFilePrinter.println(message);
        }
        if (configLogAWS == true) {
            Log.log(configLogAWSLevel, LogSubject.MqttClient, "[CANARY] " + message);
        }
    }

    static void exitWithError(int errorCode) {
        if (configFilePrinter != null) {
            configFilePrinter.close();
        }
        System.exit(errorCode);
    }

    static final class ClientsData {
        CompletableFuture<Void> connectedFuture = new CompletableFuture<>();
        CompletableFuture<Void> stopFuture = new CompletableFuture<>();
        String sharedTopic = "test/shared_topic";
        String clientId = "";
        boolean subscribedToTopics = false;
        boolean isWaitingForOperation = false;
    }

    enum CANARY_OPERATIONS {
        OPERATION_NULL,
        OPERATION_START,
        OPERATION_STOP,
        OPERATION_SUBSCRIBE,
        OPERATION_UNSUBSCRIBE,
        OPERATION_UNSUBSCRIBE_BAD,
        OPERATION_PUBLISH_QOS0,
        OPERATION_PUBLISH_QOS1,
        OPERATION_PUBLISH_TO_SUBSCRIBED_TOPIC_QOS0,
        OPERATION_PUBLISH_TO_SUBSCRIBED_TOPIC_QOS1,
        OPERATION_PUBLISH_TO_SHARED_TOPIC_QOS0,
        OPERATION_PUBLISH_TO_SHARED_TOPIC_QOS1,
    }
    static List<CANARY_OPERATIONS> clientsOperationsList = new ArrayList<CANARY_OPERATIONS>();

    static final class CanaryLifecycleEvents implements Mqtt5ClientOptions.LifecycleEvents {
        @Override
        public void onAttemptingConnect(Mqtt5Client client, OnAttemptingConnectReturn onAttemptingConnectReturn) {}

        @Override
        public void onConnectionSuccess(Mqtt5Client client, OnConnectionSuccessReturn onConnectionSuccessReturn) {
            ConnAckPacket connAckData = onConnectionSuccessReturn.getConnAckPacket();
            NegotiatedSettings negotiatedSettings = onConnectionSuccessReturn.getNegotiatedSettings();
            int clientIdx = clients.indexOf(client);
            PrintLog("[Lifecycle event] Client ID " + clientIdx + " connection success...");
            clientsData.get(clientIdx).clientId = negotiatedSettings.getAssignedClientID();
            clientsData.get(clientIdx).connectedFuture.complete(null);
            clientsData.get(clientIdx).stopFuture = new CompletableFuture<>();
        }

        @Override
        public void onConnectionFailure(Mqtt5Client client, OnConnectionFailureReturn onConnectionFailureReturn) {
            int clientIdx = clients.indexOf(client);
            PrintLog("[Lifecycle event] Client ID " + clientIdx + " connection failed...");
            clientsData.get(clientIdx).connectedFuture.completeExceptionally(new Exception("Connection failure"));
            clientsData.get(clientIdx).subscribedToTopics = false;
        }

        @Override
        public void onDisconnection(Mqtt5Client client, OnDisconnectionReturn onDisconnectionReturn) {
            int clientIdx = clients.indexOf(client);
            PrintLog("[Lifecycle event] Client ID " + clientIdx + " connection disconnected...");
            // Print why we were disconnected
            PrintLog("[Lifecycle event]] Client ID " + clientIdx + " Disconnection error code: " + Integer.toString(onDisconnectionReturn.getErrorCode()) + " - " + CRT.awsErrorString(onDisconnectionReturn.getErrorCode()));
            if (onDisconnectionReturn.getDisconnectPacket() != null) {
                PrintLog("[Lifecycle event] Client ID " + clientIdx + " Disconnect packet reason: " + onDisconnectionReturn.getDisconnectPacket().getReasonString());
            }
            clientsData.get(clientIdx).connectedFuture = new CompletableFuture<>();
            clientsData.get(clientIdx).subscribedToTopics = false;
        }

        @Override
        public void onStopped(Mqtt5Client client, OnStoppedReturn onStoppedReturn) {
            int clientIdx = clients.indexOf(client);
            PrintLog("[Lifecycle event] Client ID " + clientIdx + " connection stopped...");
            clientsData.get(clientIdx).connectedFuture = new CompletableFuture<>();
            clientsData.get(clientIdx).stopFuture.complete(null);
            clientsData.get(clientIdx).subscribedToTopics = false;
        }
    }

    static final class CanaryPublishEvents implements Mqtt5ClientOptions.PublishEvents {
        @Override
        public void onMessageReceived(Mqtt5Client client, PublishReturn publishReturn) {
            PublishPacket publishPacket = publishReturn.getPublishPacket();
            int clientIdx = clients.indexOf(client);
            PrintLog("[Publish event] Client ID " + clientIdx + " message received:\n" +
                    "  Topic: " + publishPacket.getTopic() + "\n" +
                    "  Payload: " + new String(publishPacket.getPayload()));
        }
    }

    // ================================================================================
    // SETUP FUNCTIONS
    // ================================================================================

    public static void setupClients() {
        // Create the builder
        Mqtt5ClientOptionsBuilder clientOptionsBuilder = new Mqtt5ClientOptionsBuilder(configEndpoint, configPort);

        clientsEventLoopGroup = new EventLoopGroup(configThreads);
        clientsHostResolver = new HostResolver(clientsEventLoopGroup);
        clientsBootstrap = new ClientBootstrap(clientsEventLoopGroup, clientsHostResolver);
        clientOptionsBuilder.withBootstrap(clientsBootstrap);

        clientsSocketOptions = new SocketOptions();
        clientsSocketOptions.type = SocketOptions.SocketType.STREAM;
        clientsSocketOptions.connectTimeoutMs = 60000;
        clientsSocketOptions.keepAliveTimeoutSecs = 0;
        clientsSocketOptions.keepAliveIntervalSecs = 0;
        clientOptionsBuilder.withSocketOptions(clientsSocketOptions);

        clientOptionsBuilder.withSessionBehavior(Mqtt5ClientOptions.ClientSessionBehavior.CLEAN);
        clientOptionsBuilder.withLifecycleEvents(clientsLifecycleEvents);
        clientOptionsBuilder.withRetryJitterMode(JitterMode.None);
        clientOptionsBuilder.withMinReconnectDelayMs(60000L);
        clientOptionsBuilder.withMaxReconnectDelayMs(120000L);
        clientOptionsBuilder.withMinConnectedTimeToResetReconnectDelayMs(30000L);
        clientOptionsBuilder.withPingTimeoutMs(20000L);
        clientOptionsBuilder.withPublishEvents(clientsPublishEvents);

        if (configUseTls == true || configCertFile != null || configKeyFile != null) {
            TlsContextOptions tlsContextOptions = TlsContextOptions.createDefaultClient();
            if (configCertFile != null || configKeyFile != null) {
                tlsContextOptions.withMtlsFromPath(configCertFile, configKeyFile);
            } else {
                tlsContextOptions.withVerifyPeer(false);
            }
            clientsContext = new TlsContext(tlsContextOptions);
            clientOptionsBuilder.withTlsContext(clientsContext);
        }

        if (configUseWebsockets == true) {
            clientsWebsocketTransform = new Consumer<Mqtt5WebsocketHandshakeTransformArgs>() {
                @Override
                public void accept(Mqtt5WebsocketHandshakeTransformArgs t) {
                    t.complete(t.getHttpRequest());
                }
            };
            clientOptionsBuilder.withWebsocketHandshakeTransform(clientsWebsocketTransform);
        }

        ConnectPacketBuilder connectPacketBuilder = new ConnectPacketBuilder();
        connectPacketBuilder.withKeepAliveIntervalSeconds(30L);
        connectPacketBuilder.withMaximumPacketSizeBytes(128L * 1024L);
        connectPacketBuilder.withReceiveMaximum(9L);
        clientOptionsBuilder.withConnectOptions(connectPacketBuilder.build());

        for (int i = 0; i < configClients; i++) {
            connectPacketBuilder.withClientId(configClientID + "_" + Integer.toString(i));
            clientOptionsBuilder.withConnectOptions(connectPacketBuilder.build());
            Mqtt5Client newClient = new Mqtt5Client(clientOptionsBuilder.build());
            clients.add(newClient);
            ClientsData newData = new ClientsData();
            clientsData.add(newData);
        }
    }

    public static void setupOperations() {
        // For now have everything evenly distributed
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_NULL);
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_START);
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_STOP);
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_SUBSCRIBE);
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_UNSUBSCRIBE);
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_UNSUBSCRIBE_BAD);
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_PUBLISH_QOS0);
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_PUBLISH_QOS1);
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_PUBLISH_TO_SUBSCRIBED_TOPIC_QOS0);
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_PUBLISH_TO_SUBSCRIBED_TOPIC_QOS1);
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_PUBLISH_TO_SHARED_TOPIC_QOS0);
        clientsOperationsList.add(CANARY_OPERATIONS.OPERATION_PUBLISH_TO_SHARED_TOPIC_QOS1);
    }

    // ================================================================================
    // OPERATIONS
    // ================================================================================

    public static void OperationNull(int clientIdx) {
        // Do nothing!
        PrintLog("[OP] Null called for client ID " + clientIdx);
        return;
    }

    public static void OperationStart(int clientIdx) {
        Mqtt5Client client = clients.get(clientIdx);
        if (clientsData.get(clientIdx).isWaitingForOperation == true) {
            PrintLog("[OP] Start called for client ID " + clientIdx + " but already has operation...");
            return;
        }
        if (client.getIsConnected() == true) {
            PrintLog("[OP] Start called for client ID " + clientIdx + " but is already connected/started!");
            return;
        }

        clientsData.get(clientIdx).isWaitingForOperation = true;
        PrintLog("[OP] About to start client ID " + clientIdx);
        client.start();
        try {
            clientsData.get(clientIdx).connectedFuture.get(operationFutureWaitTime, TimeUnit.SECONDS);
        } catch (Exception ex) {
            PrintLog("[OP] Start had an exception! Exception: " + ex);
            ex.printStackTrace();
            if (configFilePrinter != null) {
                ex.printStackTrace(configFilePrinter);
            }
            exitWithError(1);
        }
        PrintLog("[OP] Started client ID " + clientIdx);
        clientsData.get(clientIdx).isWaitingForOperation = false;
    }

    public static void OperationStop(int clientIdx) {
        Mqtt5Client client = clients.get(clientIdx);
        if (clientsData.get(clientIdx).isWaitingForOperation == true) {
            PrintLog("[OP] Stop called for client ID " + clientIdx + " but already has operation...");
            return;
        }
        if (client.getIsConnected() == false) {
            PrintLog("[OP] Stop called for client ID " + clientIdx + " but is already disconnected/stopped!");
            return;
        }

        clientsData.get(clientIdx).isWaitingForOperation = true;
        PrintLog("[OP] About to stop client ID " + clientIdx);
        client.stop(new DisconnectPacketBuilder().build());
        try {
            clientsData.get(clientIdx).stopFuture.get(operationFutureWaitTime, TimeUnit.SECONDS);
        } catch (Exception ex) {
            PrintLog("[OP] Stop had an exception! Exception: " + ex);
            ex.printStackTrace();
            if (configFilePrinter != null) {
                ex.printStackTrace(configFilePrinter);
            }
            exitWithError(1);
        }
        PrintLog("[OP] Stopped client ID " + clientIdx);
        clientsData.get(clientIdx).isWaitingForOperation = false;
    }

    public static void OperationSubscribe(int clientIdx) {
        Mqtt5Client client = clients.get(clientIdx);
        if (clientsData.get(clientIdx).isWaitingForOperation == true) {
            PrintLog("[OP] Subscribe called for client ID " + clientIdx + " but already has operation...");
            return;
        }
        if (client.getIsConnected() == false) {
            OperationStart(clientIdx);
            return;
        }
        if (clientsData.get(clientIdx).subscribedToTopics == true) {
            return;
        }

        clientsData.get(clientIdx).isWaitingForOperation = true;
        PrintLog("[OP] About to subscribe client ID " + clientIdx);
        SubscribePacketBuilder subscribePacketBuilder = new SubscribePacketBuilder();
        subscribePacketBuilder.withSubscription(clientsData.get(clientIdx).clientId, QOS.AT_LEAST_ONCE);
        subscribePacketBuilder.withSubscription(clientsData.get(clientIdx).sharedTopic, QOS.AT_LEAST_ONCE);
        try {
            client.subscribe(subscribePacketBuilder.build()).get(operationFutureWaitTime, TimeUnit.SECONDS);
        } catch (Exception ex) {
            PrintLog("[OP] Subscribe had an exception! Exception: " + ex);
            ex.printStackTrace();
            if (configFilePrinter != null) {
                ex.printStackTrace(configFilePrinter);
            }
            exitWithError(1);
        }
        clientsData.get(clientIdx).subscribedToTopics = true;
        PrintLog("[OP] Subscribed client ID " + clientIdx);
        clientsData.get(clientIdx).isWaitingForOperation = false;
    }

    public static void OperationUnsubscribe(int clientIdx) {
        Mqtt5Client client = clients.get(clientIdx);
        if (clientsData.get(clientIdx).isWaitingForOperation == true) {
            PrintLog("[OP] Unsubscribe called for client ID " + clientIdx + " but already has operation...");
            return;
        }
        if (client.getIsConnected() == false) {
            OperationStart(clientIdx);
            return;
        }
        if (clientsData.get(clientIdx).subscribedToTopics == false) {
            return;
        }

        clientsData.get(clientIdx).isWaitingForOperation = true;
        PrintLog("[OP] About to unsubscribe client ID " + clientIdx);
        UnsubscribePacketBuilder unsubscribePacketBuilder = new UnsubscribePacketBuilder();
        unsubscribePacketBuilder.withSubscription(clientsData.get(clientIdx).clientId);
        unsubscribePacketBuilder.withSubscription(clientsData.get(clientIdx).sharedTopic);
        try {
            client.unsubscribe(unsubscribePacketBuilder.build()).get(operationFutureWaitTime, TimeUnit.SECONDS);
        } catch (Exception ex) {
            PrintLog("[OP] Unsubscribe had an exception! Exception: " + ex);
            ex.printStackTrace();
            if (configFilePrinter != null) {
                ex.printStackTrace(configFilePrinter);
            }
            exitWithError(1);
        }
        clientsData.get(clientIdx).subscribedToTopics = false;
        PrintLog("[OP] Unsubscribed client ID " + clientIdx);
        clientsData.get(clientIdx).isWaitingForOperation = false;
    }

    public static void OperationUnsubscribeBad(int clientIdx) {
        Mqtt5Client client = clients.get(clientIdx);
        if (clientsData.get(clientIdx).isWaitingForOperation == true) {
            PrintLog("[OP] Unsubscribe bad called for client ID " + clientIdx + " but already has operation...");
            return;
        }
        if (client.getIsConnected() == false) {
            OperationStart(clientIdx);
            return;
        }

        clientsData.get(clientIdx).isWaitingForOperation = true;
        PrintLog("[OP] About to unsubscribe (bad) client ID " + clientIdx);
        UnsubscribePacketBuilder unsubscribePacketBuilder = new UnsubscribePacketBuilder();
        unsubscribePacketBuilder.withSubscription("Non_existent_topic_here");
        try {
            client.unsubscribe(unsubscribePacketBuilder.build()).get(operationFutureWaitTime, TimeUnit.SECONDS);
        } catch (Exception ex) {
            PrintLog("[OP] Unsubscribe (bad) had an exception! Exception: " + ex);
            ex.printStackTrace();
            if (configFilePrinter != null) {
                ex.printStackTrace(configFilePrinter);
            }
            exitWithError(1);
        }
        PrintLog("[OP] Unsubscribed (bad) client ID " + clientIdx);
        clientsData.get(clientIdx).isWaitingForOperation = false;
    }

    // Note: Handles QoS 0, QoS 1, and topic filter based on passed-in input
    public static void OperationPublish(int clientIdx, QOS qos, String topic) {
        Mqtt5Client client = clients.get(clientIdx);
        if (clientsData.get(clientIdx).isWaitingForOperation == true) {
            PrintLog("[OP] Publish called for client ID " + clientIdx + " with QoS" + qos + " with topic " + topic + " - but already has operation...");
            return;
        }
        if (client.getIsConnected() == false) {
            OperationStart(clientIdx);
            return;
        }

        clientsData.get(clientIdx).isWaitingForOperation = true;
        PrintLog("[OP] About to publish client ID " + clientIdx + " with QoS " + qos + " with topic " + topic);
        PublishPacketBuilder publishPacketBuilder = new PublishPacketBuilder();
        publishPacketBuilder.withQOS(qos);
        publishPacketBuilder.withPayload("Hello World".getBytes());
        publishPacketBuilder.withTopic(topic);

        // Add user properties!
        List<UserProperty> propertyList = new ArrayList<UserProperty>();
        propertyList.add(new UserProperty("key", "value"));
        propertyList.add(new UserProperty("cat", "dog"));
        propertyList.add(new UserProperty("red", "blue"));
        publishPacketBuilder.withUserProperties(propertyList);

        try {
            client.publish(publishPacketBuilder.build()).get(operationFutureWaitTime, TimeUnit.SECONDS);
        } catch (Exception ex) {
            PrintLog("[OP] Publish with QoS " + qos + " with topic " + topic + " had an exception! Exception: " + ex);
            ex.printStackTrace();
            if (configFilePrinter != null) {
                ex.printStackTrace(configFilePrinter);
            }
            exitWithError(1);
        }
        PrintLog("[OP] Published client ID " + clientIdx + " with QoS " + qos + " with topic " + topic);
        clientsData.get(clientIdx).isWaitingForOperation = false;
    }

    public static void OperationPublishQoS0(int clientIdx) {
        OperationPublish(clientIdx, QOS.AT_MOST_ONCE, "topic1");
    }

    public static void OperationPublishQoS1(int clientIdx) {
        OperationPublish(clientIdx, QOS.AT_LEAST_ONCE, "topic1");
    }

    public static void OperationPublishToSubscribedTopicQoS0(int clientIdx) {
        OperationPublish(clientIdx, QOS.AT_MOST_ONCE, clientsData.get(clientIdx).clientId);
    }

    public static void OperationPublishToSubscribedTopicQoS1(int clientIdx) {
        OperationPublish(clientIdx, QOS.AT_LEAST_ONCE, clientsData.get(clientIdx).clientId);
    }

    public static void OperationPublishToSharedTopicQoS0(int clientIdx) {
        OperationPublish(clientIdx, QOS.AT_MOST_ONCE, clientsData.get(clientIdx).sharedTopic);
    }

    public static void OperationPublishToSharedTopicQoS1(int clientIdx) {
        OperationPublish(clientIdx, QOS.AT_LEAST_ONCE, clientsData.get(clientIdx).sharedTopic);
    }

    // ================================================================================
    //  MAIN FUNCTIONS
    // ================================================================================

    public static void PerformRandomOperation() {
        int randomIdx = random.nextInt(clientsOperationsList.size());
        for (int i = 0; i < clients.size(); i++) {
            PerformOperation(clientsOperationsList.get(randomIdx), i);
            randomIdx = random.nextInt(clientsOperationsList.size());
        }
    }

    public static void PerformOperation(CANARY_OPERATIONS operation, int clientIdx) {
        switch (operation) {
            case OPERATION_NULL:
                OperationNull(clientIdx);
                break;
            case OPERATION_START:
                OperationStart(clientIdx);
                break;
            case OPERATION_STOP:
                OperationStop(clientIdx);
                break;
            case OPERATION_SUBSCRIBE:
                OperationSubscribe(clientIdx);
                break;
            case OPERATION_UNSUBSCRIBE:
                OperationUnsubscribe(clientIdx);
                break;
            case OPERATION_UNSUBSCRIBE_BAD:
                OperationUnsubscribeBad(clientIdx);
                break;
            case OPERATION_PUBLISH_QOS0:
                OperationPublishQoS0(clientIdx);
                break;
            case OPERATION_PUBLISH_QOS1:
                OperationPublishQoS1(clientIdx);
                break;
            case OPERATION_PUBLISH_TO_SUBSCRIBED_TOPIC_QOS0:
                OperationPublishToSubscribedTopicQoS0(clientIdx);
                break;
            case OPERATION_PUBLISH_TO_SUBSCRIBED_TOPIC_QOS1:
                OperationPublishToSubscribedTopicQoS1(clientIdx);
                break;
            case OPERATION_PUBLISH_TO_SHARED_TOPIC_QOS0:
                OperationPublishToSharedTopicQoS0(clientIdx);
                break;
            case OPERATION_PUBLISH_TO_SHARED_TOPIC_QOS1:
                OperationPublishToSharedTopicQoS1(clientIdx);
                break;
            default:
                PrintLog("Client ID " + clientIdx + " ERROR - Unknown operation! Performing null");
                OperationNull(clientIdx);
                break;
        }
    }

    public static void main(String[] args) {

        System.out.println("Setting up Canary...");

        // Setup
        // ====================
        parseCommandLine(args);
        if (configShowHelp == true) {
            printUsage();
            return;
        }

        // Some option validation
        if (configCertFile != null || configKeyFile != null) {
            if (configCertFile == null) {
                System.out.println("Private key file defined but not the certificate!");
                exitWithError(1);
            } else if (configKeyFile == null) {
                System.out.println("Certificate file defined but not the private key!");
                exitWithError(1);
            }
        }

        // Make all the clients
        setupClients();

        // Make all the operations
        setupOperations();

        // Capture the current time
        startDateTime = java.time.LocalDateTime.now();

        if (configLogAWS == true) {
            if (configLogFile == null) {
                Log.initLoggingToStdout(configLogAWSLevel);
            } else {
                Log.initLoggingToFile(configLogAWSLevel, configLogFile);
            }
        }
        if (configLogFile != null && configLogAWS == false) {
            try {
                configFilePrinter = new PrintWriter(configLogFile, "UTF-8");
            } catch (Exception ex) {
                System.out.println("Could not create non-AWS log file!");
                exitWithError(1);
            }
        }

        // Test loop
        // ====================
        PrintLog("Starting canary test loop...");

        boolean done = false;
        java.time.LocalDateTime nowDateTime = java.time.LocalDateTime.now();
        long secondsDifference = 0;
        long operationsExecuted = 0;
        while (!done) {
            try {
                nowDateTime = java.time.LocalDateTime.now();
                secondsDifference = startDateTime.until(java.time.LocalDateTime.now(), ChronoUnit.SECONDS);
                if (secondsDifference >= configSeconds) {
                    done = true;
                }
            } catch (ArithmeticException ex) {
                // Time overflow - exit with an error!
                exitWithError(1);
            }

            operationsExecuted += 1;
            PerformRandomOperation();

            try {
                Thread.sleep(configTps * 1000);
            } catch (Exception ex) {
                PrintLog("[OP] Could not sleep for " + (configTps*1000) + " seconds due to exception! Exception: " + ex);
                exitWithError(1);
            }
        }

        PrintLog("Test loop operations complete: Total=" + (operationsExecuted * configClients) + " Cycles=" + operationsExecuted);

        // Stop all the clients and close them to clean their memory
        for (int i = 0; i < clients.size(); i++) {
            OperationStop(i);
            clients.get(i).close();
        }

        // Cleanup
        // ====================
        PrintLog("Cleaning up canary...");

        if (clientsContext != null) {
            clientsContext.close();
        }
        if (clientsEventLoopGroup != null) {
            clientsEventLoopGroup.close();
        }
        if (clientsHostResolver != null) {
            clientsHostResolver.close();
        }
        if (clientsBootstrap != null) {
            clientsBootstrap.close();
        }
        if (clientsSocketOptions != null) {
            clientsSocketOptions.close();
        }

        PrintLog("Waiting for no resources...");
        CrtResource.waitForNoResources();

        PrintLog("Finished canary with no errors");
        exitWithError(0); // exit with no error
    }
}
