package cn.wildfirechat.push;

import cn.wildfirechat.push.ios.IOSPushServiceImpl;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class Utility {
    private static final Logger LOG = LoggerFactory.getLogger(Utility.class);
    public static boolean filterPush(PushMessage pushMessage) {
        if(pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_BYE) {
            if(Utility.shouldCancelVoipByePush(pushMessage.sender, pushMessage.convType, pushMessage.userId, pushMessage.pushData)) {
                LOG.info("Voip bye push canceled");
                return true;
            }
        } else if(pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_ANSWER) {
            if(Utility.shouldCancelVoipAnswerPush(pushMessage.sender, pushMessage.userId)) {
                LOG.info("Voip answer push canceled");
                return true;
            }
        }
        return false;
    }
    /*
    说明：1, 当回1对1通话时，发起方肯定在线，如果接收方不在线，发起方主动结束电话或者等待超时时需要推送对方。
         2, 当是群组通话时，不在线的接收方可能回到结束推送，只有所有人都离开的结束原因才需要推送。
     */
    private static boolean shouldCancelVoipByePush(String sender, int conversationType, String recvId, String pushData) {
        if(sender.equals(recvId)) {
            return true;
        }

        int reason = -1;
        if (!StringUtils.isEmpty(pushData)) {
            try {
                JSONObject object = (JSONObject) (new JSONParser().parse(pushData));
                Object oEndReason = object.get("r");
                if (oEndReason != null) {
                    if (oEndReason instanceof Integer) {
                        reason = (Integer) oEndReason;
                    } else if (oEndReason instanceof Long) {
                        long lr = (Long) oEndReason;
                        reason = (int) lr;
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            return false;
        }

        LOG.info("End call reason is {}, convType is {}", reason, conversationType);
        if(reason > 0) {
            if(conversationType == 0) {
                //单人聊天。发起方在线，对方不在线。当对方超时或者当前方挂断，需要通知对方通话已经结束。
                if(reason == 3 || reason == 11) {
                    return false;
                }
            } else if(conversationType == 1) {
                //群组聊天时，只有所有人离开时才需要推送未在线用户
                if(reason == 9) {
                    return false;
                }
            }
        }
        return true;
    }

    //接听推送，只有自己发给自己用来停止掉其它端的振铃。
    private static boolean shouldCancelVoipAnswerPush(String sender, String recvId) {
        return !sender.equals(recvId);
    }
}
