package cn.wildfirechat.push.ios;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import com.notnoop.apns.*;
import com.notnoop.exceptions.ApnsDeliveryErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.notnoop.apns.DeliveryError.INVALID_TOKEN;

@Component
public class ApnsServer implements ApnsDelegate {
    private static final Logger LOG = LoggerFactory.getLogger(ApnsServer.class);
    private static ExecutorService mExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 5);
    @Override
    public void messageSent(ApnsNotification message, boolean resent) {
        LOG.info("APNS push sent:{}", message.getDeviceToken());
    }

    @Override
    public void messageSendFailed(ApnsNotification message, Throwable e) {
        LOG.info("APNS push failure:{}", e.getMessage());
        if(e instanceof ApnsDeliveryErrorException) {
            ApnsDeliveryErrorException apnsDeliveryErrorException = (ApnsDeliveryErrorException)e;
            LOG.info("APNS error code:{}", apnsDeliveryErrorException.getDeliveryError());
            if (apnsDeliveryErrorException.getDeliveryError() == INVALID_TOKEN) {
                if (message.getDeviceId() != null) {
                    LOG.error("Invalide token!!!");
                } else {
                    LOG.error("APNS ERROR without deviceId:{}", message);
                }
            }

        }
    }

    @Override
    public void connectionClosed(DeliveryError e, int messageIdentifier) {
        LOG.info("111");
    }

    @Override
    public void cacheLengthExceeded(int newCacheLength) {
        LOG.info("111");
    }

    @Override
    public void notificationsResent(int resendCount) {
        LOG.info("111");
    }

    ApnsService productSvc;
    ApnsService developSvc;
    ApnsService voipSvc;

    @Autowired
    private ApnsConfig mConfig;

    @PostConstruct
    private void init() {
        if (StringUtils.isEmpty(mConfig.alert)) {
            mConfig.alert = "default";
        }

        if (StringUtils.isEmpty(mConfig.voipAlert)) {
            mConfig.alert = "default";
        }

        productSvc = APNS.newService()
                .asBatched(3, 10)
                .withAppleDestination(true)
                .withCert(mConfig.productCerPath, mConfig.productCerPwd)
                .withDelegate(this)
                .build();

        developSvc = APNS.newService()
                .asBatched(3, 10)
                .withAppleDestination(false)
                .withCert(mConfig.developCerPath, mConfig.developCerPwd)
                .withDelegate(this)
                .build();

        voipSvc = APNS.newService()
                .withAppleDestination(true)
                .withCert(mConfig.voipCerPath, mConfig.voipCerPwd)
                .withDelegate(this)
                .build();

        productSvc.start();
        developSvc.start();
        voipSvc.start();
    }


    public void pushMessage(PushMessage pushMessage) {
        final long start = System.currentTimeMillis();
        mExecutor.submit(()-> {
            long now = System.currentTimeMillis();
            if (now - start > 5000) {
                LOG.error("等待太久，消息抛弃");
                return;
            }
            ApnsService service = developSvc;
            if (pushMessage.getPushType() == IOSPushType.IOS_PUSH_TYPE_DISTRIBUTION) {
                if (pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_NORMAL || StringUtils.isEmpty(pushMessage.getVoipDeviceToken())) {
                    service = productSvc;
                } else {
                    service = voipSvc;
                }
            }


            if (service == null) {
                LOG.error("Service not exist!!!!");
                return;
            }
            String sound = mConfig.alert;

            String pushContent = pushMessage.getPushContent();
            if (pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_INVITE) {
                pushContent = "通话邀请";
                sound = mConfig.voipAlert;
            } else if(pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_BYE) {
                pushContent = "通话结束";
                sound = null;
            }

            int badge = pushMessage.getUnReceivedMsg();
            if (badge <= 0) {
                badge = 1;
            }

            String title;
            String body;
            //todo 这里需要加上语言的处理，客户端会上报自己的语言，在DeviceInfo那个类中
//        if (pushMessage.language == "zh_CN") {
//
//        } else if(pushMessage.language == "US_EN") {
//
//        }
            if (pushMessage.convType == 1) {
                title = pushMessage.targetName;
                if (StringUtils.isEmpty(title)) {
                    title = "群聊";
                }

                if (StringUtils.isEmpty(pushMessage.senderName)) {
                    body = pushContent;
                } else {
                    body = pushMessage.senderName + ":" + pushContent;
                }

                if (pushMessage.mentionedType == 1) {
                    if (StringUtils.isEmpty(pushMessage.senderName)) {
                        body = "有人在群里@了你";
                    } else {
                        body = pushMessage.senderName + "在群里@了你";
                    }
                } else if(pushMessage.mentionedType == 2) {
                    if (StringUtils.isEmpty(pushMessage.senderName)) {
                        body = "有人在群里@了大家";
                    } else {
                        body = pushMessage.senderName + "在群里@了大家";
                    }
                }
            } else {
                if (StringUtils.isEmpty(pushMessage.senderName)) {
                    title = "消息";
                } else {
                    title = pushMessage.senderName;
                }
                body = pushContent;
            }

            final String payload = APNS.newPayload().alertBody(body).badge(badge).alertTitle(title).sound(sound).build();
            final ApnsNotification goodMsg = service.push(service == voipSvc ? pushMessage.getVoipDeviceToken() : pushMessage.getDeviceToken(), payload, null);
            LOG.info("Message id: " + goodMsg.getIdentifier());

//
//            //检查key到期日期
//            final Map<String, Date> inactiveDevices = service.getInactiveDevices();
//            for (final Map.Entry<String, Date> ent : inactiveDevices.entrySet()) {
//                LOG.info("Inactive " + ent.getKey() + " at date " + ent.getValue());
//            }
        });

    }
}
