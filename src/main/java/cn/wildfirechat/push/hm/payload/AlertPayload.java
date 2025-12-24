package cn.wildfirechat.push.hm.payload;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import cn.wildfirechat.push.Utility;
import cn.wildfirechat.push.hm.payload.internal.*;
import com.google.gson.Gson;
import org.json.simple.JSONObject;

public class AlertPayload extends Payload{
    Notification notification;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static AlertPayload buildAlertPayload(PushMessage pushMessage) {
        AlertPayload alertPayload = new AlertPayload();
        Notification notification = new Notification();
        String[] titleAndBody = Utility.getPushTitleAndContent(pushMessage);
        notification.title = titleAndBody[0];
        notification.body = titleAndBody[1];
        alertPayload.notification = notification;

        ClickAction clickAction = new ClickAction();

        if (pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_NORMAL) {
            JSONObject data = new JSONObject();
            JSONObject conv = new JSONObject();
            conv.put("type", pushMessage.convType);
            conv.put("target", pushMessage.target);
            conv.put("line", pushMessage.line);
            data.put("conversation", conv);
            clickAction.data = data;
        } else {
            // TODO
        }

        notification.clickAction = clickAction;

        Badge badge = new Badge();
        int badgeNum = pushMessage.getUnReceivedMsg() + pushMessage.getExistBadgeNumber();
        if (badgeNum <= 0) {
            badgeNum = 1;
        }
        badge.setNum = badgeNum;
        notification.badge = badge;

        return alertPayload;
    }
}

