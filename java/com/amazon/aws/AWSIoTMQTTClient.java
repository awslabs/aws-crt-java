
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
package com.amazon.aws;

import com.amazon.aws.CRT;

public final class AWSIoTMQTTClient implements AutoCloseable {
    private CRT _crt;

    AWSIoTMQTTClient() {
        // This will cause the JNI lib to be loaded the first time a CRT is created
        _crt = new CRT();
        assert (_crt != null);

    }
    
    @Override
    public void close() {
        _crt = null;
    }

    void connect() {

    }
};
