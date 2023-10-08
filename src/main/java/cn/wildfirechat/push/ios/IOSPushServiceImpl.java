package cn.wildfirechat.push.ios;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import cn.wildfirechat.push.Utility;
import cn.wildfirechat.push.android.AndroidPushType;
import cn.wildfirechat.push.android.getui.GetuiPush;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class IOSPushServiceImpl implements IOSPushService {
    private static final Logger LOG = LoggerFactory.getLogger(IOSPushServiceImpl.class);
    @Autowired
    public ApnsServer apnsServer;

    @Autowired
    private GetuiPush getuiPush;

    private ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() * 100,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    @Override
    public Object push(PushMessage pushMessage) {
        LOG.info("iOS push {}", new Gson().toJson(pushMessage));
        if(Utility.filterPush(pushMessage)) {
            LOG.info("canceled");
            return "Canceled";
        }
        final long start = System.currentTimeMillis();
        executorService.execute(()->{
            long now = System.currentTimeMillis();
            if (now - start > 15000) {
                LOG.error("等待太久，消息抛弃");
                return;
            }
            if(pushMessage.pushType < 3) {
                apnsServer.pushMessage(pushMessage);
            } else if(pushMessage.pushType == AndroidPushType.ANDROID_PUSH_TYPE_GETUI) {
                getuiPush.push(pushMessage, false);
            } else {
                LOG.error("Unknown ios push type: {}", pushMessage.pushType);
            }

        });
        return "OK";
    }
}
