/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */
package software.amazon.awssdk.crt.mqtt5;

import software.amazon.awssdk.crt.mqtt5.Mqtt5ClientOptions.LifecycleEvents;

/**
 * Configuration for the creation of Mqtt5Listener
 *
 * MQTT5 support is currently in <b>developer preview</b>.  We encourage feedback at all times, but feedback during the
 * preview window is especially valuable in shaping the final product.  During the preview period we may make
 * backwards-incompatible changes to the public API, but in general, this is something we will try our best to avoid.
 */
public class Mqtt5ListenerOptions {

    private LifecycleEvents lifecycleEvents;
    private ListenerPublishEvents listenerPublishEvents;

    /**
     * Returns the LifecycleEvents interface that will be called when the client gets a LifecycleEvent.
     *
     * @return The LifecycleEvents interface that will be called when the client gets a LifecycleEvent
     */
    public LifecycleEvents getLifecycleEvents() {
        return this.lifecycleEvents;
    }

    /**
     * Returns the PublishEvents interface that will be called when the client gets a message.
     *
     * @return PublishEvents interface that will be called when the client gets a message.
     */
    public ListenerPublishEvents getListenerPublishEvents() {
        return this.listenerPublishEvents;
    }


    public Mqtt5ListenerOptions(Mqtt5ListenerOptionsBuilder builder)
    {
        this.lifecycleEvents = builder.lifecycleEvents;
        this.listenerPublishEvents = builder.listenerPublishEvents;
    }

    /*******************************************************************************
     * callback methods
     ******************************************************************************/

    /**
     * An interface that defines all of the publish functions the Mqtt5Client will call when it receives a publish packet.
     */
    public interface ListenerPublishEvents {
        /**
         * Called when an MQTT PUBLISH packet is received by the client
         *
         * @param client The client that has received the message
         * @param publishReturn All of the data that was received from the server
         *
         * @return return true if the message get processed, otherwise false
         *         If the message get processed, it will not passed down anymore. The client
         *         and other listener would not get the publishEvent anymore.
         */
        public boolean onMessageReceived(Mqtt5Client client, PublishReturn publishReturn);
    }

    /*******************************************************************************
     * Builder
     ******************************************************************************/

    /**
     * All of the options for a Mqtt5Listener. This includes the settings to make a connection, as well as the
     * event callbacks, publish callbacks, and more.
     */
    static final public class Mqtt5ListenerOptionsBuilder {
        private LifecycleEvents lifecycleEvents;
        private ListenerPublishEvents listenerPublishEvents;

        /**
         * Sets the Lifecycle Events interface that will be called when the client gets a LifecycleEvent.
         *
         * @param lifecycleEvents The LifecycleEvents interface that will be called
         * @return The Mqtt5ListenerOptionsBuilder after setting the Lifecycle Events interface
         */
        public Mqtt5ListenerOptionsBuilder withLifecycleEvents(LifecycleEvents lifecycleEvents) {
            this.lifecycleEvents = lifecycleEvents;
            return this;
        }


        /**
         * Sets the ListenerPublishEvents interface that will be called when the client gets a message.
         *
         * @param publishEvents The ListenerPublishEvents interface that will be called when the client gets a message.
         * @return The Mqtt5ListenerOptionsBuilder after setting the PublishEvents interface
         */
        public Mqtt5ListenerOptionsBuilder withListenerPublishEvents(ListenerPublishEvents publishEvents) {
            this.listenerPublishEvents = publishEvents;
            return this;
        }


        /**
         * Returns a Mqtt5ListenerOptions class configured with all of the options set in the Mqtt5ListenerOptions.
         * This can then be used to make a new Mqtt5Client.
         *
         * @return A configured Mqtt5ListenerOptions
         */
        public Mqtt5ListenerOptions build()
        {
            return new Mqtt5ListenerOptions(this);
        }

    }

}


