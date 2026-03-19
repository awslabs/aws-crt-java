/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import software.amazon.awssdk.crt.Log;
import software.amazon.awssdk.crt.Log.LogLevel;
import software.amazon.awssdk.crt.Log.LogSubject;
import software.amazon.awssdk.crt.mqtt5.packets.PubAckPacket;

/**
 * The type of data returned after calling Publish on an Mqtt5Client. The data contained varies depending
 * on the publish and its configuration. Use <code>getType()</code> to figure out what type of data is contained and either
 * <code>getData()</code> to get the data and cast it, or call <code>getResult[Type name here]()</code> to get the data already cast.
 */
public class PublishResult {
    private PublishResultType type;

    // The values for the various types of packet (currently just PubAck data)
    private PubAckPacket pubackData;

    /**
     * Returns the type of data that was returned after calling Publish on the Mqtt5Client.
     * You can use this information to determine what type of data is contained and either
     * <code>getData()</code> to get the data and cast it, or call <code>getResult[Type name here]()</code> to
     * get the data already cast.
     * @return The type of data contained in the PublishResult
     */
    public PublishResultType getType() {
        return type;
    }

    /**
     * Returns the data contained in the PubAck result. This is based on the PublishResultType,
     * which is determined by the QoS setting in the published message.
     *
     * Note: To get the data type from this function, you will need to cast. For example, to get
     * the PubAck from result type of PUBACK, you will need to use the following:
     * <code>PubAckPacket packet = (PubAckPacket)getValue()</code>
     *
     * @return The data contained in the PublishResult result
     */
    public Object getValue() {
        if (type == PublishResultType.NONE) {
            return null;
        } else if (type == PublishResultType.PUBACK) {
            return getResultPubAck();
        } else {
            Log.log(LogLevel.Error, LogSubject.MqttClient, "PublishResult: Cannot get value - unknown type of: " + type);
            return null;
        }
    }

    /**
     * Returns the data contained in the PublishResult for a PublishResultType of PUBACK.
     * This occurs for QoS 1 and will return a PubAckPacket.
     * @return the data contained in the PublishResult for a PublishResultType of PUBACK.
     */
    public PubAckPacket getResultPubAck() {
        return pubackData;
    }

    /**
     * This is only called in JNI to make a new PublishResult with QoS 0.
     */
    private PublishResult() {
        this.type = PublishResultType.NONE;
    }

    /**
     * This is only called in JNI to make a new PublishResult with a PubAck packet (QoS 1).
     * @param newPubackPacket The PubAckPacket data for QoS 1 packets. Can be null if result is non QoS 1.
     * @return A newly created PublishResult
     */
    private PublishResult(PubAckPacket newPubackPacket) {
        this.type = PublishResultType.PUBACK;
        this.pubackData = newPubackPacket;
    }


    /**
     * The type of data returned after calling Publish on an MQTT5 client. The type returned from a publish
     * varies based on the QoS settings of the message sent.
     */
    public enum PublishResultType {

        /**
         * No PublishResult result data (QoS 0)
         * This means the PublishResult has no data and getValue will return null.
         */
        NONE(0),

        /**
         * PublishResult result was a publish acknowledgment (PubAck - QoS 1)
         * This means the PublishResult has a PubAck and getValue will return
         * the PubAckPacket associated with the publish.
         */
        PUBACK(1);

        private int result;

        private PublishResultType(int code) {
            result = code;
        }

        /**
         * @return The native enum integer value associated with this Java enum value
         */
        public int getValue() {
            return result;
        }

        /**
         * Creates a Java PublishResultType enum value from a native integer value.
         *
         * @param value native integer value for PublishResultType
         * @return a new PublishResultType value
         */
        public static PublishResultType getEnumValueFromInteger(int value) {
            PublishResultType enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }
            throw new RuntimeException("Illegal PublishResultType");
        }

        private static Map<Integer, PublishResultType> buildEnumMapping() {
            return Stream.of(PublishResultType.values())
                .collect(Collectors.toMap(PublishResultType::getValue, Function.identity()));
        }
        private static Map<Integer, PublishResultType> enumMapping = buildEnumMapping();
    }
}
