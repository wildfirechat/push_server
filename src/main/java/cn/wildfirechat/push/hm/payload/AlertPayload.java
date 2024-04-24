package cn.wildfirechat.push.hm.payload;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import cn.wildfirechat.push.Utility;
import cn.wildfirechat.push.hm.payload.internal.ClickAction;
import cn.wildfirechat.push.hm.payload.internal.Notification;
import cn.wildfirechat.push.hm.payload.internal.Payload;
import cn.wildfirechat.push.hm.payload.internal.Target;
import com.google.gson.Gson;
import org.json.simple.JSONObject;

import java.text.MessageFormat;
import java.util.ArrayList;


public class AlertPayload {
    Payload payload;
    Target target;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static AlertPayload buildAlertPayload(PushMessage pushMessage) {
        Notification notification = new Notification();
        String[] titleAndBody = Utility.getPushTitleAndContent(pushMessage);
        notification.title = titleAndBody[0];
        notification.body = titleAndBody[1];

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

        Target target = new Target();
        target.token = new ArrayList<>();
        target.token.add(pushMessage.deviceToken);

        AlertPayload alertPayload = new AlertPayload();
        alertPayload.payload = new Payload();
        alertPayload.payload.notification = notification;
        alertPayload.target = target;

        return alertPayload;
    }
}

