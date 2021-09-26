package cn.wildfirechat.push.ios;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import com.turo.pushy.apns.*;
import com.turo.pushy.apns.metrics.micrometer.MicrometerApnsClientMetricsListener;
import com.turo.pushy.apns.util.ApnsPayloadBuilder;
import com.turo.pushy.apns.util.SimpleApnsPushNotification;
import com.turo.pushy.apns.util.concurrent.PushNotificationFuture;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.exit;

@Component
public class ApnsServer  {
    private static final Logger LOG = LoggerFactory.getLogger(ApnsServer.class);

    final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    final MicrometerApnsClientMetricsListener productMetricsListener =
            new MicrometerApnsClientMetricsListener(meterRegistry,
                    "notifications", "apns_product");
    final MicrometerApnsClientMetricsListener developMetricsListener =
            new MicrometerApnsClientMetricsListener(meterRegistry,
                    "notifications", "apns_develop");

    ApnsClient productSvc;
    ApnsClient developSvc;
    ApnsClient productVoipSvc;
    ApnsClient developVoipSvc;

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

        try {
            productSvc = new ApnsClientBuilder()
                    .setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST)
                    .setClientCredentials(new File(mConfig.cerPath), mConfig.cerPwd)
                    .setMetricsListener(productMetricsListener)
                    .build();

            developSvc = new ApnsClientBuilder()
                    .setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                    .setClientCredentials(new File(mConfig.cerPath), mConfig.cerPwd)
                    .setMetricsListener(developMetricsListener)
                    .build();

            if (mConfig.voipFeature) {
                productVoipSvc = new ApnsClientBuilder()
                        .setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST)
                        .setClientCredentials(new File(mConfig.voipCerPath), mConfig.voipCerPwd)
                        .setMetricsListener(productMetricsListener)
                        .build();
                developVoipSvc = new ApnsClientBuilder()
                        .setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)
                        .setClientCredentials(new File(mConfig.voipCerPath), mConfig.voipCerPwd)
                        .setMetricsListener(developMetricsListener)
                        .build();
            }

        } catch (IOException e) {
            e.printStackTrace();
            exit(-1);
        }
    }

    public long getMessageId(PushMessage pushMessage) {
        try {
            JSONObject jsonObject = (JSONObject)(new JSONParser().parse(pushMessage.pushData));
            if(jsonObject.get("messageUid") instanceof Long) {
                return (Long)jsonObject.get("messageUid");
            } else if(jsonObject.get("messageUid") instanceof Integer) {
                return (Integer)jsonObject.get("messageUid");
            } else if(jsonObject.get("messageUid") instanceof Double) {
                double uid = (Double)jsonObject.get("messageUid");
                return (long)uid;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void pushMessage(PushMessage pushMessage) {
            ApnsClient service;
            String sound = mConfig.alert;

        String collapseId = null;
        if(pushMessage.messageId > 0) {
            collapseId = pushMessage.messageId + "";
        }

            String pushContent = pushMessage.getPushContent();
            boolean hiddenDetail = pushMessage.isHiddenDetail;
            if (pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_INVITE) {
                pushContent = "通话邀请";
                sound = mConfig.voipAlert;
                hiddenDetail = false;
            } else if(pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_BYE) {
                pushContent = "通话结束";
                sound = null;
                hiddenDetail = false;
            } else  if(pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_ANSWER) {
                pushContent = "已被其他端接听";
                sound = null;
                hiddenDetail = false;
            } else if(pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_RECALLED) {
                pushContent = "消息已被撤回";
                sound = null;
                hiddenDetail = false;
                long recalledId = getMessageId(pushMessage);
                if(recalledId > 0) {
                    collapseId = recalledId + "";
                }
                pushMessage.pushData = null;
            } else if(pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_DELETED) {
                pushContent = "消息已被删除";
                sound = null;
                hiddenDetail = false;
                long deletedId = getMessageId(pushMessage);
                if(deletedId > 0) {
                    collapseId = deletedId + "";
                }
                pushMessage.pushData = null;
            } else if(pushMessage.pushMessageType != PushMessageType.PUSH_MESSAGE_TYPE_NORMAL) {
                LOG.error("not support push message type:{}", pushMessage.pushMessageType);
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

                if (hiddenDetail) {
                    body = "你收到一条新消息"; //Todo 需要判断当前语言
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

                if (hiddenDetail) {
                    body = "你收到一条新消息"; //Todo 需要判断当前语言
                } else {
                    body = pushContent;
                }
            }

            final ApnsPayloadBuilder payloadBuilder = new ApnsPayloadBuilder();
            payloadBuilder.setAlertBody(body);
            payloadBuilder.setAlertTitle(title);
            payloadBuilder.setBadgeNumber(badge);
            payloadBuilder.setSound(sound);

            Calendar c = Calendar.getInstance();
            ApnsPushNotification pushNotification;

            if (!mConfig.voipFeature || pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_NORMAL || StringUtils.isEmpty(pushMessage.getVoipDeviceToken())) {
                if (pushMessage.getPushType() == IOSPushType.IOS_PUSH_TYPE_DISTRIBUTION) {
                    service = productSvc;
                } else {
                    service = developSvc;
                }
                if((pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_NORMAL || pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_RECALLED || pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_DELETED) || StringUtils.isEmpty(pushMessage.getVoipDeviceToken())) {
                    c.add(Calendar.MINUTE, 10); //普通推送
                    String payload = payloadBuilder.buildWithDefaultMaximumLength();
                    pushNotification = new SimpleApnsPushNotification(pushMessage.deviceToken, pushMessage.packageName, payload, c.getTime(), DeliveryPriority.CONSERVE_POWER, PushType.ALERT, collapseId);
                } else {
                    c.add(Calendar.MINUTE, 1); //voip通知，使用普通推送
                    payloadBuilder.setContentAvailable(true);
                    payloadBuilder.addCustomProperty("voip", true);
                    payloadBuilder.addCustomProperty("voip_type", pushMessage.pushMessageType);
                    payloadBuilder.addCustomProperty("voip_data", pushMessage.pushData);
                    String payload = payloadBuilder.buildWithDefaultMaximumLength();
                    pushNotification = new SimpleApnsPushNotification(pushMessage.deviceToken, pushMessage.packageName, payload, c.getTime(), DeliveryPriority.IMMEDIATE, PushType.BACKGROUND, collapseId);
                }
            } else {
                if (pushMessage.getPushType() == IOSPushType.IOS_PUSH_TYPE_DISTRIBUTION) {
                    service = productVoipSvc;
                } else {
                    service = developVoipSvc;
                }
                c.add(Calendar.MINUTE, 1);
                String payload = payloadBuilder.buildWithDefaultMaximumLength();
                pushNotification = new SimpleApnsPushNotification(pushMessage.voipDeviceToken, pushMessage.packageName + ".voip", payload, c.getTime(), DeliveryPriority.IMMEDIATE, PushType.VOIP, collapseId);
            }

        SimpleApnsPushNotification simpleApnsPushNotification = (SimpleApnsPushNotification)pushNotification;
            LOG.info("CollapseId:{}", simpleApnsPushNotification.getCollapseId());

            if (service == null) {
                LOG.error("Service not exist!!!!");
                return;
            }

            final PushNotificationFuture<ApnsPushNotification, PushNotificationResponse<ApnsPushNotification>>
                    sendNotificationFuture = service.sendNotification(pushNotification);
            sendNotificationFuture.addListener(new GenericFutureListener<Future<? super PushNotificationResponse<ApnsPushNotification>>>() {
                @Override
                public void operationComplete(Future<? super PushNotificationResponse<ApnsPushNotification>> future) throws Exception {
                    // When using a listener, callers should check for a failure to send a
                    // notification by checking whether the future itself was successful
                    // since an exception will not be thrown.
                    if (future.isSuccess()) {
                        final PushNotificationResponse<ApnsPushNotification> pushNotificationResponse =
                                sendNotificationFuture.getNow();
                        if(!pushNotificationResponse.isAccepted()) {
                            LOG.error("apns push failure: {}", pushNotificationResponse.getRejectionReason());
                        } else {
                            LOG.info("push success: {}", pushNotificationResponse.getApnsId().toString());
                            LOG.info("token invalidate timestamp: {}", pushNotificationResponse.getTokenInvalidationTimestamp());
                        }
                    } else {
                        // Something went wrong when trying to send the notification to the
                        // APNs gateway. We can find the exception that caused the failure
                        // by getting future.cause().
                        future.cause().printStackTrace();
                        LOG.error("apns push failure: {}", future.cause().getLocalizedMessage());
                    }
                }
            });
    }
}
