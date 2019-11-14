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


/**
 * This class is a factory for a byte array based aws_input_stream native resource.
 */
public class ByteArrayAwsInputStream implements IAwsInputStream {

    byte[] data;

    public ByteArrayAwsInputStream(byte[] data) {
        super();
        this.data = data;
    }

    public long createNativeStreamHandle() {
        return awsInputStreamByteArrayNew(data);
    }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native long awsInputStreamByteArrayNew(byte[] data);
};
