package cn.wildfirechat.push.android.vivo;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import cn.wildfirechat.push.Utility;
import com.vivo.push.sdk.notofication.Message;
import com.vivo.push.sdk.notofication.Result;
import com.vivo.push.sdk.server.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Random;

@Component
public class VivoPush {
    private static final Logger LOG = LoggerFactory.getLogger(VivoPush.class);
    private long tokenExpiredTime;

    @Autowired
    VivoConfig mConfig;

    private volatile String authToken;

    /**
     * 配置变更时清除缓存的 authToken，使下次推送用新的 appId/appKey/appSecret 重新获取，
     * 避免改配置后仍使用旧 token。
     */
    public synchronized void refresh() {
        authToken = null;
        tokenExpiredTime = 0;
        LOG.info("VivoPush token invalidated");
    }

    public void push(PushMessage pushMessage) throws Exception {
        if (StringUtils.isEmpty(mConfig.getAppId()) || "0".equals(mConfig.getAppId())
                || StringUtils.isEmpty(mConfig.getAppKey()) || StringUtils.isEmpty(mConfig.getAppSecret())) {
            LOG.info("VivoPush config is not complete, skip push");
            return;
        }
        String regId = pushMessage.getDeviceToken();
        // 添加regId有效性检查
        if (!isValidVivoRegId(regId)) {
            LOG.error("Invalid vivo regId: {}", regId);
            return;
        }

        LOG.debug("Sending Vivo push with regId: {}", regId);

        // 升级点1：使用新的认证机制
        if (tokenExpiredTime <= System.currentTimeMillis()) {
            refreshToken(); // 内部需要实现getToken逻辑
        }

        String[] arr = Utility.getPushTitleAndContent(pushMessage);
        String title = arr[0];
        String body = arr[1];

        Sender senderMessage = new Sender(mConfig.getAppSecret());
        senderMessage.setAuthToken(authToken);

        Message.Builder builder = new Message.Builder()
                .regId(regId)
                .notifyType(3)
                .title(title)
                .content(body)
                .timeToLive(1000)
                .skipType(1)
                .networkType(-1)
                .requestId(System.currentTimeMillis() + "_" + new Random().nextInt(1000))
                .pushMode(0);

        if (pushMessage.pushMessageType != PushMessageType.PUSH_MESSAGE_TYPE_NORMAL) {
            builder.timeToLive(60);
        } else {
            builder.timeToLive(10 * 60);
        }

        Result resultMessage = senderMessage.sendSingle(builder.build());
        if (resultMessage.getResult() != 0) {
            throw new RuntimeException("Vivo push failed: " + resultMessage.getResult() + " - " + resultMessage.getDesc());
        }
        LOG.info("Vivo push response: [MessageId={}] [ErrorCode={}] [Reason={}]",
                resultMessage.getTaskId(),
                resultMessage.getResult(),
                resultMessage.getDesc());
    }

    // 新增的token刷新方法（需根据示例代码实现）
    private void refreshToken() {
        try {
            Sender tokenSender = new Sender(mConfig.getAppSecret());
            // 升级点9：使用新的getToken接口
            Result tokenResult = tokenSender.getToken(
                    Integer.parseInt(mConfig.getAppId()),       // 需要配置appId
                    mConfig.getAppKey()        // 需要配置appKey
            );

            if (tokenResult.getResult() == 0) {
                authToken = tokenResult.getAuthToken();
                // 假设token有效期为48小时（按vivo标准）
                tokenExpiredTime = System.currentTimeMillis() + 48 * 3600 * 1000;
            } else {
                LOG.error("Refresh token failed: [code={}] [desc={}]",
                        tokenResult.getResult(),
                        tokenResult.getDesc());
            }
        } catch (Exception e) {
            LOG.error("Refresh token error", e);
        }
    }

    // 校验regId格式
    private boolean isValidVivoRegId(String regId) {
        return regId != null && regId.startsWith("v2-") && regId.length() > 50;
    }
}
