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
package software.amazon.awssdk.crt.io;

import software.amazon.awssdk.crt.CrtResource;

/**
 * This class wraps a byte array based aws_input_stream native resource.
 */
public class ByteArrayAwsInputStream extends AwsInputStream {

    byte[] data;

    public ByteArrayAwsInputStream(byte[] data) {
        super();
        this.data = data;

        long nativeHandle = awsInputStreamByteArrayNew(this, data);
        acquireNativeHandle(nativeHandle);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long awsInputStreamByteArrayNew(ByteArrayAwsInputStream thisObj, byte[] data);
};
