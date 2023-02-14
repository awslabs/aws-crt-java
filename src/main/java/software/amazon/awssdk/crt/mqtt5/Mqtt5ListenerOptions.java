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
         */
        public boolean onMessageReceived(Mqtt5Client client, PublishReturn publishReturn);
    }

}
