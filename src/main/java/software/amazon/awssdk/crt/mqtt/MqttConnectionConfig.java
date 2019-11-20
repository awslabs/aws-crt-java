
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

import software.amazon.awssdk.crt.http.HttpProxyOptions;
import software.amazon.awssdk.crt.io.ClientTlsContext;
import software.amazon.awssdk.crt.io.SocketOptions;

public final class MqttConnectionConfig {
    public String clientId;
    public String endpoint;
    public int port;
    public boolean useWebsocket = false;
    public boolean cleanSession = true;
    public int keepAliveSecs = 0;
    public SocketOptions socketOptions = new SocketOptions();
    public MqttMessage will = null;
    public String username;
    public String password;
    public ClientTlsContext tlsContext = null;
    public HttpProxyOptions proxyOptions = null;
}
