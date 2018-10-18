/*
 * Copyright 2010-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package software.amazon.awssdk.crt.mqtt;

import software.amazon.awssdk.crt.mqtt.MqttActionListener;
import software.amazon.awssdk.crt.mqtt.MqttMessageResultListener;
import software.amazon.awssdk.crt.mqtt.MqttTaskScheduler;

class MqttConnectionListener implements MqttActionListener {
    protected MqttMessageResultListener listener;
    protected MqttTaskScheduler tasks;

    public MqttConnectionListener(MqttTaskScheduler _tasks, MqttMessageResultListener _listener) {
        tasks = _tasks;
        listener = _listener;
    }

    @Override
    public void onSuccess(MqttToken actionToken) {
        tasks.scheduleTask(new Runnable() {
            @Override
            public void run() {
                listener.onSuccess();
            }
        });
    }

    @Override
    public void onFailure(MqttToken actionToken, Throwable cause) {
        tasks.scheduleTask(new Runnable() {
            @Override
            public void run() {
                listener.onFailure();
            }
        });
    }
};
