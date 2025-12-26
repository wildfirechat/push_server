package cn.wildfirechat.push.android.honor.internal;

public class AndroidConfig {
    public String ttl = "86400s";
    public String biTag;
    public String data;
    public AndroidNotification notification;

    // 0，普通消息；1，测试消息
    public int targetUserType = 0;
}
