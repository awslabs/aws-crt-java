/*
 * Copyright 2010-2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.amazon.awssdk.crt.http;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HttpHeader {
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private byte[] name;  /* Not final, Native will manually set name after calling empty Constructor. */
    private byte[] value; /* Not final, Native will manually set value after calling empty Constructor. */

    /** Called by Native to create a new HttpHeader. This is so that Native doesn't have to worry about UTF8
     * encoding/decoding issues. The user thread will deal with them when they call getName() or getValue() **/
    private HttpHeader() {}

    public HttpHeader(String name, String value){
        this.name = name.getBytes(UTF8);
        this.value = value.getBytes(UTF8);
    }

    public String getName() {
        if (name == null) {
            return "";
        }
        return new String(name, UTF8);
    }

    public String getValue() {
        if (value == null) {
            return "";
        }
        return new String(value, UTF8);
    }

    @Override
    public String toString() {
        return getName() + ":" + getValue();
    }
}