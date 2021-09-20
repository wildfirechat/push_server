package cn.wildfirechat.push.android.xiaomi;


import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import com.google.gson.Gson;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.json.simple.parser.ParseException;


import java.io.IOException;

import static com.xiaomi.xmpush.server.Message.NOTIFY_TYPE_ALL;

@Component
public class XiaomiPush {
    private static final Logger LOG = LoggerFactory.getLogger(XiaomiPush.class);
    @Autowired
    private XiaomiConfig mConfig;


    public void push(PushMessage pushMessage) {
        Constants.useOfficial();
        Sender sender = new Sender(mConfig.getAppSecret());

        Message message;
        String token = pushMessage.getDeviceToken();
        pushMessage.deviceToken = null;
        if(pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_INVITE || pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_BYE || pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_ANSWER) {
            //voip
            long timeToLive = 60 * 1000; // 1 min
            message = new Message.Builder()
                    .payload(new Gson().toJson(pushMessage))
                    .restrictedPackageName(pushMessage.getPackageName())
                    .passThrough(1)  //透传
                    .timeToLive(timeToLive)
                    .enableFlowControl(false)
                    .build();
        } else if(pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_RECALLED || pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_DELETED) {
            //Todo not implement
            //撤回或者删除消息，需要更新远程通知，暂未实现
            return;
        } else {  //normal or friend
            long timeToLive = 600 * 1000;//10 min
            message = new Message.Builder()
                    .payload(new Gson().toJson(pushMessage))
                    .title("新消息提醒")
                    .description(pushMessage.pushContent)
                    .notifyType(NOTIFY_TYPE_ALL)
                    .restrictedPackageName(pushMessage.getPackageName())
                    .passThrough(0)
                    .timeToLive(timeToLive)
                    .enableFlowControl(true)
                    .build();
        }

        Result result = null;
        try {
            result = sender.send(message, token, 3);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        LOG.info("Server response: MessageId: " + result.getMessageId()
            + " ErrorCode: " + result.getErrorCode().toString()
            + " Reason: " + result.getReason());
    }
}
