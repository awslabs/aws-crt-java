/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.util.List;

public class Http2ConnectionSetting {
    /**
     * Predefined settings identifiers (RFC-7540 6.5.2).
     */
    public enum ID {
        HEADER_TABLE_SIZE(1), ENABLE_PUSH(2), MAX_CONCURRENT_STREAMS(3), INITIAL_WINDOW_SIZE(4), MAX_FRAME_SIZE(5),
        MAX_HEADER_LIST_SIZE(6);

        private int settingID;

        ID(int value) {
            settingID = value;
        }

        public int getValue() {
            return settingID;
        }
    }

    private ID id;
    private long value;

    public long getValue() {
        return value;
    }

    public ID getId() {
        return id;
    }

    /**
     * HTTP/2 connection settings.
     *
     * value is limited from 0 to UINT32_MAX (RFC-7540 6.5.1)
     */
    public Http2ConnectionSetting(ID id, long value) {
        if (value > 4294967296L || value < 0) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.value = value;
    }

    /**
     * @exclude Marshals a list of settings into an array for Jni to deal with.
     *
     * @param settings list of headers to write to the headers block
     * @return a long[] that with the [id, value, id, value, *]
     */
    public static long[] marshallSettingsForJNI(List<Http2ConnectionSetting> settings) {
        /* Each setting is two long */
        int totalLength = settings.size();

        long marshalledSettings[] = new long[totalLength * 2];

        for (int i = 0; i < totalLength; i++) {
            marshalledSettings[i * 2] = settings.get(i).id.getValue();
            marshalledSettings[i * 2 + 1] = settings.get(i).value;
        }

        return marshalledSettings;
    }

}
