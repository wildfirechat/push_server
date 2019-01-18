package cn.wildfirechat.push.android;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.android.hms.HMSPush;
import cn.wildfirechat.push.android.meizu.MeiZuPush;
import cn.wildfirechat.push.android.xiaomi.XiaomiPush;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AndroidPushServiceImpl implements AndroidPushService {
    private static final Logger LOG = LoggerFactory.getLogger(AndroidPushServiceImpl.class);
    @Autowired
    private HMSPush hmsPush;

    @Autowired
    private MeiZuPush meiZuPush;

    @Autowired
    private XiaomiPush xiaomiPush;


    @Override
    public Object push(PushMessage pushMessage) {
        LOG.info("Android push {}", new Gson().toJson(pushMessage));
        switch (pushMessage.getPushType()) {
            case AndroidPushType.ANDROID_PUSH_TYPE_XIAOMI:
                xiaomiPush.push(pushMessage);
                break;
            case AndroidPushType.ANDROID_PUSH_TYPE_HUAWEI:
                hmsPush.push(pushMessage);
                break;
            case AndroidPushType.ANDROID_PUSH_TYPE_MEIZU:
                meiZuPush.push(pushMessage);
                break;
            default:
                LOG.info("unknown push type");
                break;
        }
        return "ok";
    }
}
