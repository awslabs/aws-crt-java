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

import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtResource;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.io.AwsInputStream;

/**
 * Represents a single Client Request to be sent on a HTTP connection
 */
public class HttpRequest2 extends CrtResource {

    private AwsInputStream bodyStream;

    public HttpRequest2() {
        this(null);
    }

    public HttpRequest2(AwsInputStream bodyStream) {
        acquireNativeHandle(httpRequest2New());

        this.bodyStream = bodyStream;
        if (bodyStream != null) {
            httpRequest2SetBodyStream(getNativeHandle(), bodyStream.getNativeHandle());
            addReferenceTo(bodyStream);
        }
    }

    /**
     * Begins the release process of the request's native handle
     */
    @Override
    protected void releaseNativeHandle() {
        if (!isNull()) {
            httpRequest2Destroy(getNativeHandle());
        }
    }

    public String getMethod() {
        return httpRequest2GetMethod(getNativeHandle());
    }

    public void setMethod(String method) {
        httpRequest2SetMethod(getNativeHandle(), method);
    }

    public String getPath() {
        return httpRequest2GetPath(getNativeHandle());
    }

    public void setPath(String path) {
        httpRequest2SetPath(getNativeHandle(), path);
    }


    /**
     * Determines whether a resource releases its dependencies at the same time the native handle is released or if it waits.
     * Resources that wait are responsible for calling releaseReferences() manually.
     */
    @Override
    protected boolean canReleaseReferencesImmediately() { return true; }

    /*******************************************************************************
     * native methods
     ******************************************************************************/
    private static native void httpRequest2Destroy(long nativeHandle);
    private static native long httpRequest2New() throws CrtRuntimeException;

    private static native void httpRequest2SetBodyStream(long nativeHandle, long input_stream);

    private static native String httpRequest2GetMethod(long nativeHandle);
    private static native void httpRequest2SetMethod(long nativeHandle, String method);

    private static native String httpRequest2GetPath(long nativeHandle);
    private static native void httpRequest2SetPath(long nativeHandle, String path);


}
