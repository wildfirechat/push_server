package cn.wildfirechat.push.hm.payload.internal;

import java.util.List;

// https://developer.huawei.com/consumer/cn/doc/harmonyos-references/push-scenariozed-api-request-param#section17371529101117
public class Notification {
    public String category = "IM";
    public String title;
    public String body;
    public String image;
    public int style;
    public String bigTitle;
    public String bigBody;
    public Integer notifyId;
    public String appMessageId;
    public String profileId;
    public List<String> inboxContent;
    public ClickAction clickAction;
    public Badge badge;
    public String sound;
    public Integer soundDuration;
    public boolean foregroundShow;

}
