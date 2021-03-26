package com.amazonaws.s3;

import com.amazonaws.s3.model.*;
import software.amazon.awssdk.crt.CRT;
import software.amazon.awssdk.crt.CrtRuntimeException;
import software.amazon.awssdk.crt.auth.credentials.CredentialsProvider;
import software.amazon.awssdk.crt.http.HttpHeader;
import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.http.HttpRequestBodyStream;
import software.amazon.awssdk.crt.io.ClientBootstrap;
import software.amazon.awssdk.crt.s3.*;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class S3NativeClientRequest {

    String customQueryParameters = "";

    LinkedList<HttpHeader> customHeaders = new LinkedList<HttpHeader>();

    public S3NativeClientRequest() {

    }

    protected S3NativeClientRequest(BuilderImpl builder) {
        this.customQueryParameters = builder.customQueryParameters;
        this.customHeaders = builder.customHeaders;
    }

    public String customQueryParameters() {
        return customQueryParameters;
    }

    public LinkedList<HttpHeader> customHeaders() {
        return customHeaders;
    }

    public interface Builder {
        Builder customQueryParameters(String customQueryParameters);

        Builder customHeaders(LinkedList<HttpHeader> customHeaders);
    }

    protected static class BuilderImpl implements Builder {

        String customQueryParameters = "";

        LinkedList<HttpHeader> customHeaders = new LinkedList<HttpHeader>();

        protected BuilderImpl() {
        }

        public BuilderImpl(S3NativeClientRequest request) {
            customQueryParameters(request.customQueryParameters);
            customHeaders(request.customHeaders);
        }

        public final Builder customQueryParameters(String customQueryParameters) {
            this.customQueryParameters = customQueryParameters;
            return this;
        }

        public final Builder customHeaders(LinkedList<HttpHeader> customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }
    }

}
