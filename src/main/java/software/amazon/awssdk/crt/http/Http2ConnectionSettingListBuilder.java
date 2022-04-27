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

    /**
     * The SETTINGS_HEADER_TABLE_SIZE. Allows the sender to inform the remote
     * endpoint of the maximum size of the header compression table used to decode
     * header blocks, in octets.
     *
     * @param headerTableSize the maximum size of the header compression table used
     *                        (in octets)
     * @return {@link Http2ConnectionSettingListBuilder}
     */
    public Http2ConnectionSettingListBuilder headerTableSize(long headerTableSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.HEADER_TABLE_SIZE, headerTableSize));
        return this;
    }

    /**
     * The SETTINGS_ENABLE_PUSH. This setting can be used to disable server push
     *
     * @param push enable server push or not.
     * @return {@link Http2ConnectionSettingListBuilder}
     */
    public Http2ConnectionSettingListBuilder enablePush(boolean push) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.ENABLE_PUSH, push ? 1 : 0));
        return this;
    }

    /**
     * The SETTINGS_MAX_CONCURRENT_STREAMS. Indicates the maximum number of
     * concurrent streams that the sender will allow.
     *
     * @param maxConcurrentStreams The maximum number of concurrent streams
     * @return {@link Http2ConnectionSettingListBuilder}
     */
    public Http2ConnectionSettingListBuilder maxConcurrentStreams(long maxConcurrentStreams) throws Exception {
        settings.add(
                new Http2ConnectionSetting(Http2ConnectionSetting.ID.MAX_CONCURRENT_STREAMS, maxConcurrentStreams));
        return this;
    }

    /**
     * The SETTINGS_INITIAL_WINDOW_SIZE. Indicates the sender's initial window size
     * (in octets) for stream-level flow control. The initial value is 2^16-1
     * (65,535) octets.
     *
     * @param initialWindowSize initial window size (in octets)
     * @return {@link Http2ConnectionSettingListBuilder}
     */
    public Http2ConnectionSettingListBuilder initialWindowSize(long initialWindowSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.INITIAL_WINDOW_SIZE, initialWindowSize));
        return this;
    }

    /**
     * The SETTINGS_MAX_FRAME_SIZE. Indicates the size of the largest frame payload
     * that the sender is willing to receive, in octets.
     *
     * @param maxFrameSize the size of the largest frame payload (in octets)
     * @return {@link Http2ConnectionSettingListBuilder}
     */
    public Http2ConnectionSettingListBuilder maxFrameSize(long maxFrameSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.MAX_FRAME_SIZE, maxFrameSize));
        return this;
    }

    /**
     * The SETTINGS_MAX_HEADER_LIST_SIZE. This advisory setting informs a peer of
     * the maximum size of header list that the sender is prepared to accept, in
     * octets. The value is based on the uncompressed size of header fields,
     * including the length of the name and value in octets plus an overhead of 32
     * octets for each header field.
     *
     * @param maxHeaderListSize the maximum size of header list (in octets)
     * @return {@link Http2ConnectionSettingListBuilder}
     */
    public Http2ConnectionSettingListBuilder maxHeaderListSize(long maxHeaderListSize) throws Exception {
        settings.add(new Http2ConnectionSetting(Http2ConnectionSetting.ID.MAX_HEADER_LIST_SIZE, maxHeaderListSize));
        return this;
    }

    public List<Http2ConnectionSetting> build() {
        return this.settings;
    }
}
