package cn.wildfirechat.push.android.meizu;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import com.meizu.push.sdk.server.IFlymePush;
import com.meizu.push.sdk.server.constant.ResultPack;
import com.meizu.push.sdk.server.model.push.PushResult;
import com.meizu.push.sdk.server.model.push.VarnishedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MeiZuPush {
    private static final Logger LOG = LoggerFactory.getLogger(MeiZuPush.class);
    private IFlymePush flymePush;

    @PostConstruct
    public void init() {
        this.flymePush = new IFlymePush(mConfig.getAppSecret());
    }

    @Autowired
    private MeiZuConfig mConfig;

    public void push(PushMessage pushMessage) {
        if(pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_RECALLED || pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_DELETED) {
            //Todo not implement
            //撤回或者删除消息，需要更新远程通知，暂未实现
            return;
        }

        //组装透传消息
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

        VarnishedMessage message = new VarnishedMessage.Builder()
            .appId(mConfig.getAppId())
            .title(title)
            .content(pushMessage.pushContent)
            .validTime(1)
            .build();

        //目标用户
        List<String> pushIds = new ArrayList<String>();
        pushIds.add(pushMessage.getDeviceToken());

        try {
            // 1 调用推送服务
            ResultPack<PushResult> result = flymePush.pushMessage(message, pushIds);
            if (result.isSucceed()) {
                // 2 调用推送服务成功 （其中map为设备的具体推送结果，一般业务针对超速的code类型做处理）
                PushResult pushResult = result.value();
                String msgId = pushResult.getMsgId();//推送消息ID，用于推送流程明细排查
                Map<String, List<String>> targetResultMap = pushResult.getRespTarget();//推送结果，全部推送成功，则map为empty
                LOG.info("push result:" + pushResult);
                if (targetResultMap != null && !targetResultMap.isEmpty()) {
                    System.err.println("push fail token:" + targetResultMap);
                }
            } else {
                // 调用推送接口服务异常 eg: appId、appKey非法、推送消息非法.....
                // result.code(); //服务异常码
                // result.comment();//服务异常描述
                LOG.info(String.format("pushMessage error code:%s comment:%s", result.code(), result.comment()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
