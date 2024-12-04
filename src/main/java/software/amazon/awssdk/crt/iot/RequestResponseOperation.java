/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.iot;

import java.util.ArrayList;

/**
 * Configuration options for an MQTT-based request-response operation.
 */
public class RequestResponseOperation {

    /**
     * Builder class for RequestResponseOperation instances
     */
    public static class RequestResponseOperationBuilder {

        private RequestResponseOperation request = new RequestResponseOperation();

        private RequestResponseOperationBuilder() {}

        /**
         * Adds a response path to the set of all possible response paths associated with this request.
         *
         * @param path response paths to associate with this request
         *
         * @return the builder object
         */
        public RequestResponseOperationBuilder withResponsePath(ResponsePath path) {
            request.responsePaths.add(path);
            return this;
        }

        /**
         * Adds a topic filter to the set of topic filters that should be subscribed to in order to cover all possible
         * response paths.  Sometimes using wildcards can cut down on the subscriptions needed; other times that isn't
         * possible to do correctly.
         *
         * @param topicFilter topic filter to subscribe to in order to cover one or more response paths
         *
         * @return the builder object
         */
        public RequestResponseOperationBuilder withSubscription(String topicFilter) {
            request.subscriptions.add(topicFilter);
            return this;
        }

        /**
         * Sets the topic to publish the request to once response subscriptions have been established.
         *
         * @param publishTopic topic to publish the request to once response subscriptions have been established
         *
         * @return the builder object
         */
        public RequestResponseOperationBuilder withPublishTopic(String publishTopic) {
            request.publishTopic = publishTopic;
            return this;
        }

        /**
         * Sets the payload to publish to 'publishTopic' in order to initiate the request.
         *
         * @param payload payload to publish to 'publishTopic' in order to initiate the request
         *
         * @return the builder object
         */
        public RequestResponseOperationBuilder withPayload(byte[] payload) {
            request.payload = payload;
            return this;
        }

        /**
         * Sets the correlation token embedded in the request that must be found in a response message.
         *
         * This can be null to support certain services which don't use correlation tokens.  In that case, the client
         * only allows one token-less request at a time.
         *
         * @param correlationToken the correlation token embedded in the request that must be found in a response message
         *
         * @return the builder object
         */
        public RequestResponseOperationBuilder withCorrelationToken(String correlationToken) {
            request.correlationToken = correlationToken;
            return this;
        }

        /**
         * Creates a new RequestResponseOperation instance based on the builder's configuration.
         *
         * @return a new RequestResponseOperation instance based on the builder's configuration
         */
        public RequestResponseOperation build() {
            return new RequestResponseOperation(request);
        }
    }

    private ArrayList<ResponsePath> responsePaths = new ArrayList<>();
    private ArrayList<String> subscriptions = new ArrayList<>();
    private String publishTopic;
    private String correlationToken;
    private byte[] payload;

    private RequestResponseOperation() {
    }

    private RequestResponseOperation(RequestResponseOperation request) {
        this.responsePaths.addAll(request.responsePaths);
        this.subscriptions.addAll(request.subscriptions);
        this.publishTopic = request.publishTopic;
        this.correlationToken = request.correlationToken;
        this.payload = request.payload;
    }

    /**
     * Creates a new builder for RequestResponseOperations objects
     *
     * @return a new builder instance for RequestResponseOperations objects
     */
    public RequestResponseOperationBuilder builder() {
        return new RequestResponseOperationBuilder();
    }

}
