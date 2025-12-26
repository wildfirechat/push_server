package cn.wildfirechat.push.android.honor.internal;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.Utility;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class RequestBody {

    //  荣耀推送长度限制：title最大40字符，body最大256字符
    private static final int HONOR_PUSH_MAX_TITLE = 40;
    private static final int HONOR_PUSH_MAX_BODY = 256;

    // 自定义消息负载
    public String data;
    public Notification notification;
    public AndroidConfig android;
    public List<String> token;


    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static RequestBody buildRequestBody(PushMessage pushMessage, String appId) {
        RequestBody requestBody = new RequestBody();

        requestBody.notification = new Notification();
        String[] titleAndBody = Utility.getPushTitleAndContent(pushMessage);
        String title = titleAndBody[0];
        String body = titleAndBody[1];

        // 处理华为推送的长度限制
        if (title != null && title.length() > HONOR_PUSH_MAX_TITLE) {
            title = title.substring(0, HONOR_PUSH_MAX_TITLE - 3) + "...";
        }

        if (body != null && body.length() > HONOR_PUSH_MAX_BODY) {
            body = body.substring(0, HONOR_PUSH_MAX_BODY - 3) + "...";
        }

        requestBody.notification.title = title;
        requestBody.notification.body = body;

        List<String> tokens = new ArrayList<>();
        tokens.add(pushMessage.deviceToken);
        requestBody.token = tokens;

        requestBody.android = new AndroidConfig();
        requestBody.android.notification = new AndroidNotification();
        requestBody.android.notification.title = title;
        requestBody.android.notification.body = body;
        requestBody.android.notification.importance = "NORMAL";

        requestBody.android.notification.clickAction = new ClickAction();
        requestBody.android.notification.clickAction.type = 3;
        requestBody.android.notification.badge = new BadgeNotification();

        int badgeNum = pushMessage.getUnReceivedMsg() + pushMessage.getExistBadgeNumber();
        if (badgeNum <= 0) {
            badgeNum = 1;
        }
        requestBody.android.notification.badge.setNum = badgeNum;
        return requestBody;
    }
}
