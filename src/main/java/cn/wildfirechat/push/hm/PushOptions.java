package cn.wildfirechat.push.hm;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import com.google.gson.Gson;
import com.turo.pushy.apns.PushType;

public class PushOptions {
    public boolean testMessage;
    public Integer ttl;
    public String biTag;
    public String receiptId;
    public Integer collapseKey;
    public Integer backgroundMode;


    public static PushOptions buildPushOptions(PushMessage pushMessage) {
        PushOptions pushOptions = new PushOptions();
        pushOptions.testMessage = false;
        if (pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_INVITE || pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_BYE) {
            pushOptions.ttl = 60;
        } else {
            // 保持默认值，1 天
        }

        return pushOptions;
    }
}
