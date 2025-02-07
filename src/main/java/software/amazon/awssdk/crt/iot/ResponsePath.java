/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

/**
 * A response path is a pair of values - MQTT topic and a JSON path - that describe how a response to
 * an MQTT-based request may arrive.  For a given request type, there may be multiple response paths and each
 * one is associated with a separate JSON schema for the response body.
 */
public class ResponsePath {

    /**
     * Builder type for ResponsePath instances
     */
    public static class ResponsePathBuilder {

        private ResponsePath path = new ResponsePath();

        private ResponsePathBuilder() {}

        /**
         * Fluent setter for the MQTT topic associated with this response path.
         *
         * @param responseTopic MQTT topic associated with this response path
         *
         * @return the builder object
         */
        public ResponsePathBuilder withResponseTopic(String responseTopic) {
            path.responseTopic = responseTopic;
            return this;
        }

        /**
         * Fluent setter for the JSON path for finding correlation tokens within payloads that arrive on this response path's topic.
         *
         * @param correlationTokenJsonPath JSON path for finding correlation tokens within payloads that arrive on this response path's topic
         *
         * @return the builder object
         */
        public ResponsePathBuilder withCorrelationTokenJsonPath(String correlationTokenJsonPath) {
            path.correlationTokenJsonPath = correlationTokenJsonPath;
            return this;
        }

        /**
         * Creates a new ResponsePath instance based on the builder's configuration.
         *
         * @return a new ResponsePath instance based on the builder's configuration
         */
        public ResponsePath build() {
            return new ResponsePath(path);
        }
    }

    private String responseTopic;
    private String correlationTokenJsonPath;

    private ResponsePath() {
    }

    private ResponsePath(ResponsePath path) {
        this.responseTopic = path.responseTopic;
        this.correlationTokenJsonPath = path.correlationTokenJsonPath;
    }

    /**
     * Creates a new builder instance for ResponsePath instances.
     *
     * @return a new builder instance for ResponsePath instances
     */
    public static ResponsePathBuilder builder() {
        return new ResponsePathBuilder();
    }
}
