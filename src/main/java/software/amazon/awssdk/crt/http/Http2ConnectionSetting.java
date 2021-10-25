package software.amazon.awssdk.crt.http;

public class Http2ConnectionSetting {
    public enum Http2ConnectionSettingID {
        HEADER_TABLE_SIZE(1), ENABLE_PUSH(2), MAX_CONCURRENT_STREAMS(3), INITIAL_WINDOW_SIZE(4), MAX_FRAME_SIZE(5),
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
}
