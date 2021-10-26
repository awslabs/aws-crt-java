/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.util.List;

public class Http2ConnectionSetting {
    public enum Http2ConnectionSettingID {
        HEADER_TABLE_SIZE(1),
        ENABLE_PUSH(2),
        MAX_CONCURRENT_STREAMS(3),
        INITIAL_WINDOW_SIZE(4),
        MAX_FRAME_SIZE(5),
        MAX_HEADER_LIST_SIZE(6);

        private int settingID;

        Http2ConnectionSettingID(int value) {
            settingID = value;
        }

        public int getValue() {
            return settingID;
        }
    }

    public Http2ConnectionSettingID id;
    public long value;

    public Http2ConnectionSetting(Http2ConnectionSettingID id, long value) {
        this.id = id;
        this.value = value;
    }

    /**
     * Turn the setting toa list of two long, which makes it much easier for Jni to
     * deal with.
     *
     * @return a long[] that with the [id, value]
     */
    public long[] marshalForJni() {
        long[] marshalled = new long[2];
        marshalled[0] = (long) id.getValue();
        marshalled[1] = value;
        return marshalled;
    }

    /**
     * Marshals a list of settings into a list for Jni to deal with.
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
