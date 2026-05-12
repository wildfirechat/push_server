package cn.wildfirechat.push.android;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.Utility;
import cn.wildfirechat.push.android.fcm.FCMPush;
import cn.wildfirechat.push.getui.GetuiPush;
import cn.wildfirechat.push.android.hms.HMSPush;
import cn.wildfirechat.push.android.honor.HonorPush;
import cn.wildfirechat.push.android.oppo.OppoPush;
import cn.wildfirechat.push.android.vivo.VivoPush;
import cn.wildfirechat.push.admin.PushRecordService;
import cn.wildfirechat.push.admin.StatisticsService;
import cn.wildfirechat.push.android.xiaomi.XiaomiPush;
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
public class AndroidPushServiceImpl implements AndroidPushService {
    private static final Logger LOG = LoggerFactory.getLogger(AndroidPushServiceImpl.class);
    @Autowired
    private HMSPush hmsPush;

    @Autowired
    private XiaomiPush xiaomiPush;

    @Autowired
    private VivoPush vivoPush;

    @Autowired
    private OppoPush oppoPush;

    @Autowired
    private FCMPush fcmPush;

    @Autowired
    private GetuiPush getuiPush;

    @Autowired
    private HonorPush honorPush;

    @Autowired
    private UniPush uniPush;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private PushRecordService pushRecordService;

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
        LOG.info("Android push {}", new Gson().toJson(pushMessage));
        if (Utility.filterPush(pushMessage)) {
            LOG.info("canceled");
            return "Canceled";
        }
        if (pushMessage.line == 1) {
            LOG.info("ignore moments messages");
            return "Canceled";
        }

        final String platform = getPlatformName(pushMessage.getPushType());
        if (statisticsService != null) {
            statisticsService.recordPush(platform);
        }

        final long start = System.currentTimeMillis();
        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            if (now - start > 15000) {
                LOG.error("等待太久，消息抛弃");
                return;
            }

            try {
                switch (pushMessage.getPushType()) {
                    case AndroidPushType.ANDROID_PUSH_TYPE_XIAOMI:
                        xiaomiPush.push(pushMessage);
                        break;
                    case AndroidPushType.ANDROID_PUSH_TYPE_HUAWEI:
                        hmsPush.push(pushMessage);
                        break;
                    case AndroidPushType.ANDROID_PUSH_TYPE_VIVO:
                        vivoPush.push(pushMessage);
                        break;
                    case AndroidPushType.ANDROID_PUSH_TYPE_OPPO:
                        oppoPush.push(pushMessage);
                        break;
                    case AndroidPushType.ANDROID_PUSH_TYPE_FCM:
                        fcmPush.push(pushMessage);
                        break;
                    case AndroidPushType.ANDROID_PUSH_TYPE_GETUI:
                        getuiPush.push(pushMessage, true);
                        break;
                    case AndroidPushType.ANDROID_PUSH_TYPE_HONOR:
                        honorPush.push(pushMessage);
                        break;
                    case AndroidPushType.PUSH_TYPE_UNIPUSH_V2:
                        uniPush.push(pushMessage);
                        break;
                    default:
                        LOG.info("unknown push type");
                        break;
                }
                if (statisticsService != null) {
                    statisticsService.recordSuccess(platform);
                }
                if (pushRecordService != null) {
                    pushRecordService.saveRecord(pushMessage, platform, true, null);
                }
            } catch (Exception e) {
                LOG.error("Push error", e);
                if (statisticsService != null) {
                    statisticsService.recordFail(platform);
                }
                if (pushRecordService != null) {
                    pushRecordService.saveRecord(pushMessage, platform, false, e.getMessage());
                }
            }
        });
        return "ok";
    }

    @Override
    public void testPush(PushMessage pushMessage) throws Exception {
        LOG.info("Android test push {}", new Gson().toJson(pushMessage));
        if (Utility.filterPush(pushMessage)) {
            throw new Exception("消息被过滤，取消推送");
        }
        final String platform = getPlatformName(pushMessage.getPushType());
        if (statisticsService != null) {
            statisticsService.recordPush(platform);
        }
        try {
            switch (pushMessage.getPushType()) {
                case AndroidPushType.ANDROID_PUSH_TYPE_XIAOMI:
                    xiaomiPush.push(pushMessage);
                    break;
                case AndroidPushType.ANDROID_PUSH_TYPE_HUAWEI:
                    hmsPush.push(pushMessage);
                    break;
                case AndroidPushType.ANDROID_PUSH_TYPE_VIVO:
                    vivoPush.push(pushMessage);
                    break;
                case AndroidPushType.ANDROID_PUSH_TYPE_OPPO:
                    oppoPush.push(pushMessage);
                    break;
                case AndroidPushType.ANDROID_PUSH_TYPE_FCM:
                    fcmPush.push(pushMessage);
                    break;
                case AndroidPushType.ANDROID_PUSH_TYPE_GETUI:
                    getuiPush.push(pushMessage, true);
                    break;
                case AndroidPushType.ANDROID_PUSH_TYPE_HONOR:
                    honorPush.push(pushMessage);
                    break;
                case AndroidPushType.PUSH_TYPE_UNIPUSH_V2:
                    uniPush.push(pushMessage);
                    break;
                default:
                    throw new Exception("未知的推送类型: " + pushMessage.getPushType());
            }
            if (statisticsService != null) {
                statisticsService.recordSuccess(platform);
            }
            if (pushRecordService != null) {
                pushRecordService.saveRecord(pushMessage, platform, true, null);
            }
        } catch (Exception e) {
            if (statisticsService != null) {
                statisticsService.recordFail(platform);
            }
            if (pushRecordService != null) {
                pushRecordService.saveRecord(pushMessage, platform, false, e.getMessage());
            }
            throw e;
        }
    }

    private String getPlatformName(int pushType) {
        switch (pushType) {
            case AndroidPushType.ANDROID_PUSH_TYPE_XIAOMI:
                return "xiaomi";
            case AndroidPushType.ANDROID_PUSH_TYPE_HUAWEI:
                return "hms";
            case AndroidPushType.ANDROID_PUSH_TYPE_VIVO:
                return "vivo";
            case AndroidPushType.ANDROID_PUSH_TYPE_OPPO:
                return "oppo";
            case AndroidPushType.ANDROID_PUSH_TYPE_FCM:
                return "fcm";
            case AndroidPushType.ANDROID_PUSH_TYPE_GETUI:
                return "getui";
            case AndroidPushType.ANDROID_PUSH_TYPE_HONOR:
                return "honor";
            case AndroidPushType.PUSH_TYPE_UNIPUSH_V2:
                return "unipush";
            default:
                return "unknown";
        }
    }
}
