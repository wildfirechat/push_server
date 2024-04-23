package cn.wildfirechat.push.hm.payload;

import cn.wildfirechat.push.hm.payload.internal.ClickAction;
import cn.wildfirechat.push.hm.payload.internal.Notification;
import cn.wildfirechat.push.hm.payload.internal.Payload;
import cn.wildfirechat.push.hm.payload.internal.Target;
import com.google.gson.Gson;

import java.util.ArrayList;


public class AlertPayload {
    Payload payload;
    Target target;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static AlertPayload buildAlertPayload(String token, String title, String body) {
        Notification notification = new Notification();
        notification.title = title;
        notification.body = body;

        ClickAction clickAction = new ClickAction();
        notification.clickAction = clickAction;

        Target target = new Target();
        target.token = new ArrayList<>();
        target.token.add(token);

        AlertPayload alertPayload = new AlertPayload();
        alertPayload.payload = new Payload();
        alertPayload.payload.notification = notification;
        alertPayload.target = target;

        return alertPayload;
    }
}

