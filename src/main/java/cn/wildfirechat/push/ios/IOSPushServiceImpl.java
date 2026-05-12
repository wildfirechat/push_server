package cn.wildfirechat.push.ios;

import cn.wildfirechat.push.admin.StatisticsService;
import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.Utility;
import cn.wildfirechat.push.android.AndroidPushType;
import cn.wildfirechat.push.getui.GetuiPush;
import cn.wildfirechat.push.unipush.UniPush;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
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

    @Autowired
    private UniPush uniPush;

    @Autowired
    private StatisticsService statisticsService;

    private ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() * 100,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    @Override
    public Object push(PushMessage pushMessage) {
        LOG.info("iOS push {}", new Gson().toJson(pushMessage));
        if(Utility.filterPush(pushMessage)) {
            LOG.info("canceled");
            return "Canceled";
        }

        final String platform;
        if(pushMessage.pushType < 3) {
            platform = "apns";
        } else if(pushMessage.pushType == AndroidPushType.ANDROID_PUSH_TYPE_GETUI) {
            platform = "getui_ios";
        } else if (pushMessage.pushType == AndroidPushType.PUSH_TYPE_UNIPUSH_V2) {
            platform = "unipush_ios";
        } else {
            platform = "unknown_ios";
        }
        if (statisticsService != null) {
            statisticsService.recordPush(platform);
        }

        final long start = System.currentTimeMillis();
        executorService.execute(()->{
            long now = System.currentTimeMillis();
            if (now - start > 15000) {
                LOG.error("等待太久，消息抛弃");
                return;
            }
            try {
                if(pushMessage.pushType < 3) {
                    apnsServer.pushMessage(pushMessage);
                } else if(pushMessage.pushType == AndroidPushType.ANDROID_PUSH_TYPE_GETUI) {
                    getuiPush.push(pushMessage, false);
                } else if (pushMessage.pushType == AndroidPushType.PUSH_TYPE_UNIPUSH_V2) {
                    uniPush.push(pushMessage);
                } else {
                    LOG.error("Unknown ios push type: {}", pushMessage.pushType);
                }
                if (statisticsService != null) {
                    statisticsService.recordSuccess(platform);
                }
            } catch (Exception e) {
                LOG.error("iOS push error", e);
                if (statisticsService != null) {
                    statisticsService.recordFail(platform);
                }
            }
        });
        return "OK";
    }

    @Override
    public void testPush(PushMessage pushMessage) throws Exception {
        LOG.info("iOS test push {}", new Gson().toJson(pushMessage));
        if (Utility.filterPush(pushMessage)) {
            throw new Exception("消息被过滤，取消推送");
        }
        final String platform;
        if (pushMessage.pushType < 3) {
            platform = "apns";
        } else if (pushMessage.pushType == AndroidPushType.ANDROID_PUSH_TYPE_GETUI) {
            platform = "getui_ios";
        } else if (pushMessage.pushType == AndroidPushType.PUSH_TYPE_UNIPUSH_V2) {
            platform = "unipush_ios";
        } else {
            throw new Exception("未知的iOS推送类型: " + pushMessage.pushType);
        }
        if (statisticsService != null) {
            statisticsService.recordPush(platform);
        }
        try {
            if (pushMessage.pushType < 3) {
                apnsServer.pushMessage(pushMessage);
            } else if (pushMessage.pushType == AndroidPushType.ANDROID_PUSH_TYPE_GETUI) {
                getuiPush.push(pushMessage, false);
            } else if (pushMessage.pushType == AndroidPushType.PUSH_TYPE_UNIPUSH_V2) {
                uniPush.push(pushMessage);
            }
            if (statisticsService != null) {
                statisticsService.recordSuccess(platform);
            }
        } catch (Exception e) {
            if (statisticsService != null) {
                statisticsService.recordFail(platform);
            }
            throw e;
        }
    }
}
