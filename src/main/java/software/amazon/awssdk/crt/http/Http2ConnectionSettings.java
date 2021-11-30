/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.util.List;
import java.util.ArrayList;

public class Http2ConnectionSettings {

    private List<Http2ConnectionSetting> settings;

    public Http2ConnectionSettings(List<Http2ConnectionSetting> settings) {
        this.settings = new ArrayList<Http2ConnectionSetting>(settings);
    }

    public Http2ConnectionSettings() {
        this.settings = new ArrayList<Http2ConnectionSetting>();
    }

    public void addSetting(Http2ConnectionSetting setting) throws Exception {
        settings.add(setting);
    }

    public void enablePush(boolean push) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.ENABLE_PUSH, push ? 1 : 0));
    }

    public void headerTableSize(long headerTableSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.HEADER_TABLE_SIZE, headerTableSize));
    }

    public void maxConcurrentStreams(long maxConcurrentStreams) throws Exception {
        settings.add(
                new Http2ConnectionSetting(Http2ConnectionSetting.ID.MAX_CONCURRENT_STREAMS, maxConcurrentStreams));
    }

    public void initialWindowSize(long initialWindowSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.INITIAL_WINDOW_SIZE, initialWindowSize));
    }

    public void maxFrameSize(long maxFrameSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.MAX_FRAME_SIZE, maxFrameSize));
    }

    public void maxHeaderListSize(long maxHeaderListSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.MAX_HEADER_LIST_SIZE, maxHeaderListSize));
    }

    /**
     * @exclude Marshals a list of settings into an array for Jni to deal with.
     *
     * @param settings list of headers to write to the headers block
     * @return a long[] that with the [id, value, id, value, *]
     */
    public static long[] marshallSettingsForJNI() {
        /* Each setting is two long */
        int totalLength = this.settings.size();

        long marshalledSettings[] = new long[totalLength * 2];

        for (int i = 0; i < totalLength; i++) {
            marshalledSettings[i * 2] = settings.get(i).id.getValue();
            marshalledSettings[i * 2 + 1] = settings.get(i).value;
        }

        return marshalledSettings;
    }
}
