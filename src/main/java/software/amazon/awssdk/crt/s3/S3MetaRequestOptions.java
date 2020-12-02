package software.amazon.awssdk.crt.s3;

import software.amazon.awssdk.crt.http.HttpRequest;
import software.amazon.awssdk.crt.s3.S3MetaRequestResponseHandler;

import java.util.HashMap;
import java.util.Map;

public class S3MetaRequestOptions {

    public enum MetaRequestType {
        GET_OBJECT(0),
        PUT_OBJECT(1);

        MetaRequestType(int nativeValue) {
            this.nativeValue = nativeValue;
        }

        public int getNativeValue() { return nativeValue; }

        public static MetaRequestType getEnumValueFromInteger(int value) {
            MetaRequestType enumValue = enumMapping.get(value);
            if (enumValue != null) {
                return enumValue;
            }

            throw new RuntimeException("Illegal signing algorithm value in signing configuration");
        }

        private static Map<Integer, MetaRequestType> buildEnumMapping() {
            Map<Integer, MetaRequestType> enumMapping = new HashMap<Integer, MetaRequestType>();
            enumMapping.put(GET_OBJECT.getNativeValue(), GET_OBJECT);
            enumMapping.put(PUT_OBJECT.getNativeValue(), PUT_OBJECT);
            return enumMapping;
        }

        private int nativeValue;

        private static Map<Integer, MetaRequestType> enumMapping = buildEnumMapping();
    }

    private MetaRequestType metaRequestType;
    private HttpRequest httpRequest;
    private S3MetaRequestResponseHandler responseHandler;

    public S3MetaRequestOptions withMetaRequestType(MetaRequestType metaRequestType) {
        this.metaRequestType = metaRequestType;
        return this;
    }

    public MetaRequestType getMetaRequestType() {
        return metaRequestType;
    }

    public S3MetaRequestOptions withHttpRequest(HttpRequest httpRequest) {
        this.httpRequest = httpRequest;
        return this;
    }

    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public S3MetaRequestOptions withResponseHandler(S3MetaRequestResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    public S3MetaRequestResponseHandler getResponseHandler() {
        return responseHandler;
    }
}
