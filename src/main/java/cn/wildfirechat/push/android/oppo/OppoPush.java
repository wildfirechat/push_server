package cn.wildfirechat.push.android.oppo;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import com.oppo.push.server.Notification;
import com.oppo.push.server.Result;
import com.oppo.push.server.Sender;
import com.oppo.push.server.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

@Component
public class OppoPush {
    private static final Logger LOG = LoggerFactory.getLogger(OppoPush.class);

    @Autowired
    OppoConfig mConfig;

    private Sender mSender;

    @PostConstruct
    private void init() {
        try {
            mSender = new Sender(mConfig.getAppKey(), mConfig.getAppSecret());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void push(PushMessage pushMessage) {
        if (mSender == null) {
            LOG.error("Oppo push message can't sent, because not initial correctly");
        }
        if(pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_RECALLED || pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_DELETED) {
            //Todo not implement
            //撤回或者删除消息，需要更新远程通知，暂未实现
            return;
        }
        Result result = null;
        try {
            Notification notification = getNotification(pushMessage); //创建通知栏消息体

            Target target = Target.build(pushMessage.deviceToken); //创建发送对象

            result = mSender.unicastNotification(notification, target);  //发送单推消息

            result.getStatusCode(); // 获取http请求状态码

            result.getReturnCode(); // 获取平台返回码

            result.getMessageId();  // 获取平台返回的messageId
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("sendSingle error " + e.getMessage());
        }
        if (result != null) {
            LOG.info("Server response: MessageId: " + result.getMessageId()
                    + " ErrorCode: " + result.getReturnCode()
                    + " Reason: " + result.getReason());
        }
    }

    private Notification getNotification(PushMessage pushMessage) {
        if (pushMessage.isHiddenDetail) {
            pushMessage.pushContent = "您收到一条新消息";
        }
        Notification notification = new Notification();


        /**
         * 以下参数必填项
        */
        String title;
        if (pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_FRIEND_REQUEST) {
            if (StringUtils.isEmpty(pushMessage.senderName)) {
                title = "好友请求";
            } else {
                title = pushMessage.senderName + " 请求加您为好友";
            }
        } else {
            if (StringUtils.isEmpty(pushMessage.senderName)) {
                title = "消息";
            } else {
                title = pushMessage.senderName;
            }
        }

        notification.setTitle(title);
        notification.setContent(pushMessage.pushContent);

        /**
         * 以下参数非必填项， 如果需要使用可以参考OPPO push服务端api文档进行设置
        */
        //通知栏样式 1. 标准样式  2. 长文本样式  3. 大图样式 【非必填，默认1-标准样式】
        notification.setStyle(1);

        // App开发者自定义消息Id，OPPO推送平台根据此ID做去重处理，对于广播推送相同appMessageId只会保存一次，对于单推相同appMessageId只会推送一次
        //notification.setAppMessageId(UUID.randomUUID().toString());

        // 应用接收消息到达回执的回调URL，字数限制200以内，中英文均以一个计算
        //notification.setCallBackUrl("http://www.test.com");

        // App开发者自定义回执参数，字数限制50以内，中英文均以一个计算
        //notification.setCallBackParameter("");

        // 点击动作类型0，启动应用；1，打开应用内页（activity的intent action）；2，打开网页；4，打开应用内页（activity）；【非必填，默认值为0】;5,Intent scheme URL
        //notification.setClickActionType(4);

        // 应用内页地址【click_action_type为1或4时必填，长度500】
        //notification.setClickActionActivity("com.coloros.push.demo.component.InternalActivity");

        // 网页地址【click_action_type为2必填，长度500】
        //notification.setClickActionUrl("http://www.test.com");

        // 动作参数，打开应用内页或网页时传递给应用或网页【JSON格式，非必填】，字符数不能超过4K，示例：{"key1":"value1","key2":"value2"}
        //notification.setActionParameters("{\"key1\":\"value1\",\"key2\":\"value2\"}");

        // 展示类型 (0, “即时”),(1, “定时”)
        notification.setShowTimeType(0);

        // 定时展示开始时间（根据time_zone转换成当地时间），时间的毫秒数
        //notification.setShowStartTime(System.currentTimeMillis() + 1000 * 60 * 3);

        // 定时展示结束时间（根据time_zone转换成当地时间），时间的毫秒数
        //notification.setShowEndTime(System.currentTimeMillis() + 1000 * 60 * 5);

        // 是否进离线消息,【非必填，默认为True】
        //notification.setOffLine(true);

        // 离线消息的存活时间(time_to_live) (单位：秒), 【off_line值为true时，必填，最长3天】
        if (pushMessage.pushMessageType != PushMessageType.PUSH_MESSAGE_TYPE_NORMAL) {
            notification.setOffLineTtl(60); // 单位秒
        } else {
            notification.setOffLineTtl(10 * 60);
        }

        // 时区，默认值：（GMT+08:00）北京，香港，新加坡
        //notification.setTimeZone("GMT+08:00");

        // 0：不限联网方式, 1：仅wifi推送
        notification.setNetworkType(0);

        return notification;
    }
}
