/**
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0.
 */

package software.amazon.awssdk.crt.http;

import java.util.List;
import java.util.ArrayList;

public class Http2ConnectionSettingListBuilder {

    private List<Http2ConnectionSetting> settings;

    public Http2ConnectionSettingListBuilder() {
        this.settings = new ArrayList<Http2ConnectionSetting>();
    }

    public Http2ConnectionSettingListBuilder addSetting(Http2ConnectionSetting setting) throws Exception {
        settings.add(setting);
        return this;
    }

    public Http2ConnectionSettingListBuilder enablePush(boolean push) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.ENABLE_PUSH, push ? 1 : 0));
        return this;
    }

    public Http2ConnectionSettingListBuilder headerTableSize(long headerTableSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.HEADER_TABLE_SIZE, headerTableSize));
        return this;
    }

    public Http2ConnectionSettingListBuilder maxConcurrentStreams(long maxConcurrentStreams) throws Exception {
        settings.add(
                new Http2ConnectionSetting(Http2ConnectionSetting.ID.MAX_CONCURRENT_STREAMS, maxConcurrentStreams));
        return this;
    }

    public Http2ConnectionSettingListBuilder initialWindowSize(long initialWindowSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.INITIAL_WINDOW_SIZE, initialWindowSize));
        return this;
    }

    public Http2ConnectionSettingListBuilder maxFrameSize(long maxFrameSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.MAX_FRAME_SIZE, maxFrameSize));
        return this;
    }

    public Http2ConnectionSettingListBuilder maxHeaderListSize(long maxHeaderListSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.MAX_HEADER_LIST_SIZE, maxHeaderListSize));
        return this;
    }

    public List<Http2ConnectionSetting> build() {
        return this.settings;
    }
}
