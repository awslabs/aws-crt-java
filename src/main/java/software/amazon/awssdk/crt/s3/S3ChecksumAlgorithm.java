package software.amazon.awssdk.crt.s3;

import java.util.HashMap;
import java.util.Map;

public enum S3ChecksumAlgorithm {

    AWS_SCA_NONE(0),

    AWS_SCA_CRC32C(1),

    AWS_SCA_CRC32(2),

    AWS_SCA_SHA1(3),

    AWS_SCA_SHA256(4);

    S3ChecksumAlgorithm(int nativeValue) {
        this.nativeValue = nativeValue;
    }

    public int getNativeValue() {
        return nativeValue;
    }

    public static S3ChecksumAlgorithm getEnumValueFromInteger(int value) {
        S3ChecksumAlgorithm enumValue = enumMapping.get(value);
        if (enumValue != null) {
            return enumValue;
        }

        throw new RuntimeException("Invalid S3 Meta Request type");
    }

    private static Map<Integer, S3ChecksumAlgorithm> buildEnumMapping() {
        Map<Integer, S3ChecksumAlgorithm> enumMapping = new HashMap<Integer, S3ChecksumAlgorithm>();
        enumMapping.put(AWS_SCA_NONE.getNativeValue(), AWS_SCA_NONE);
        enumMapping.put(AWS_SCA_CRC32C.getNativeValue(), AWS_SCA_CRC32C);
        enumMapping.put(AWS_SCA_CRC32.getNativeValue(), AWS_SCA_CRC32);
        enumMapping.put(AWS_SCA_SHA1.getNativeValue(), AWS_SCA_SHA1);
        enumMapping.put(AWS_SCA_SHA256.getNativeValue(), AWS_SCA_SHA256);
        return enumMapping;
    }

    private int nativeValue;

    private static Map<Integer, S3ChecksumAlgorithm> enumMapping = buildEnumMapping();
}
