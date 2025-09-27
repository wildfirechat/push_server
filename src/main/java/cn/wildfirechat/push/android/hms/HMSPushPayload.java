package cn.wildfirechat.push.android.hms;

import com.google.gson.Gson;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import cn.wildfirechat.push.Utility;
import cn.wildfirechat.push.android.hms.internal.HMSMessageNotification;
import cn.wildfirechat.push.android.hms.internal.HMSPushAndroidInfo;
import cn.wildfirechat.push.android.hms.internal.HMSPushClickAction;
import cn.wildfirechat.push.android.hms.internal.HMSPushMessage;
import cn.wildfirechat.push.android.hms.internal.HMSPushNotification;
import cn.wildfirechat.push.hm.payload.internal.ClickAction;
import cn.wildfirechat.push.hm.payload.internal.Notification;
import cn.wildfirechat.push.hm.payload.internal.Payload;
import cn.wildfirechat.push.hm.payload.internal.Target;

// https://developer.huawei.com/consumer/cn/doc/HMSCore-Guides/rest-sample-code-0000001050040242
public class HMSPushPayload {
    // 华为推送长度限制：title最大40字符，body最大256字符
    private static final int HMS_PUSH_MAX_TITLE = 40;
    private static final int HMS_PUSH_MAX_BODY = 256;

    public boolean validate_only = false;
    public HMSPushMessage message;


    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static HMSPushPayload buildAlertPayload(PushMessage pushMessage, String appId) {
        HMSPushMessage hmsMessage = new HMSPushMessage();

        hmsMessage.notification = new HMSPushNotification();
        String[] titleAndBody = Utility.getPushTitleAndContent(pushMessage);
        String title = titleAndBody[0];
        String body = titleAndBody[1];

        // 处理华为推送的长度限制
        if (title != null && title.length() > HMS_PUSH_MAX_TITLE) {
            title = title.substring(0, HMS_PUSH_MAX_TITLE - 3) + "...";
        }

        if (body != null && body.length() > HMS_PUSH_MAX_BODY) {
            body = body.substring(0, HMS_PUSH_MAX_BODY - 3) + "...";
        }

        hmsMessage.notification.title = title;
        hmsMessage.notification.body = body;

        List<String> tokens = new ArrayList<>();
        tokens.add(pushMessage.deviceToken);
        hmsMessage.token = tokens;

        hmsMessage.android = new HMSPushAndroidInfo();
        hmsMessage.android.notification = new HMSMessageNotification();
        hmsMessage.android.notification.channel_id = "channel_offline_" + appId;

        hmsMessage.android.notification.clickAction = new HMSPushClickAction();
        hmsMessage.android.notification.clickAction.type = 3;

        HMSPushPayload alertPayload = new HMSPushPayload();
        alertPayload.validate_only = false;
        alertPayload.message = hmsMessage;

        return alertPayload;
    }
}

