package cn.wildfirechat.push.android.getui;


import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import cn.wildfirechat.push.Utility;
import cn.wildfirechat.push.android.xiaomi.XiaomiConfig;
import com.getui.push.v2.sdk.ApiHelper;
import com.getui.push.v2.sdk.GtApiConfiguration;
import com.getui.push.v2.sdk.api.PushApi;
import com.getui.push.v2.sdk.common.ApiResult;
import com.getui.push.v2.sdk.dto.req.Audience;
import com.getui.push.v2.sdk.dto.req.message.PushChannel;
import com.getui.push.v2.sdk.dto.req.message.PushDTO;
import com.getui.push.v2.sdk.dto.req.message.android.AndroidDTO;
import com.getui.push.v2.sdk.dto.req.message.android.GTNotification;
import com.getui.push.v2.sdk.dto.req.message.android.ThirdNotification;
import com.getui.push.v2.sdk.dto.req.message.android.Ups;
import com.getui.push.v2.sdk.dto.req.message.ios.Alert;
import com.getui.push.v2.sdk.dto.req.message.ios.Aps;
import com.getui.push.v2.sdk.dto.req.message.ios.IosDTO;
import com.google.gson.Gson;
import com.meizu.push.sdk.server.IFlymePush;
import com.xiaomi.xmpush.server.Constants;
import com.xiaomi.xmpush.server.Message;
import com.xiaomi.xmpush.server.Result;
import com.xiaomi.xmpush.server.Sender;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

import static com.xiaomi.xmpush.server.Message.NOTIFY_TYPE_ALL;

@Component
public class GetuiPush {
    private static final Logger LOG = LoggerFactory.getLogger(GetuiPush.class);
    @Autowired
    private GetuiConfig mConfig;

    private PushApi pushApi;

    @PostConstruct
    public void init() {
        try{
            // 设置httpClient最大连接数，当并发较大时建议调大此参数。或者启动参数加上 -Dhttp.maxConnections=200
            System.setProperty("http.maxConnections", "200");
            GtApiConfiguration apiConfiguration = new GtApiConfiguration();
            //填写应用配置
            if(!StringUtils.isEmpty(mConfig.getAppId())) {
                apiConfiguration.setAppId(mConfig.getAppId());
                apiConfiguration.setAppKey(mConfig.getAppKey());
                apiConfiguration.setMasterSecret(mConfig.getMasterSecret());
                // 接口调用前缀，请查看文档: 接口调用规范 -> 接口前缀, 可不填写appId
                apiConfiguration.setDomain("https://restapi.getui.com/v2/");
    //        apiConfiguration.setDomain(mConfig.getDomain());
                // 实例化ApiHelper对象，用于创建接口对象
                ApiHelper apiHelper = ApiHelper.build(apiConfiguration);
                // 创建对象，建议复用。目前有PushApi、StatisticApi、UserApi
                this.pushApi = apiHelper.creatApi(PushApi.class);
            }
        } catch (Exception e) {
            LOG.error("GetuiPush init failed");
            e.printStackTrace();
        }
    }

    public void push(PushMessage pushMessage, boolean isAndroid) {
        String[] arr = Utility.getPushTitleAndContent(pushMessage);
        String title = arr[0];
        String body = arr[1];

        //根据cid进行单推
        PushDTO<Audience> pushDTO = new PushDTO<Audience>();
        // 设置推送参数
        pushDTO.setRequestId(System.currentTimeMillis() + "");
        /**** 设置个推通道参数 *****/
        com.getui.push.v2.sdk.dto.req.message.PushMessage pm = new com.getui.push.v2.sdk.dto.req.message.PushMessage();
        pushDTO.setPushMessage(pm);
        GTNotification notification = new GTNotification();
        pm.setNotification(notification);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setClickType("startapp");
//        notification.setUrl("https://www.getui.com");
        /**** 设置个推通道参数，更多参数请查看文档或对象源码 *****/

        /**** 设置厂商相关参数 ****/
        PushChannel pushChannel = new PushChannel();
        pushDTO.setPushChannel(pushChannel);
        /*配置安卓厂商参数*/
        AndroidDTO androidDTO = new AndroidDTO();
        pushChannel.setAndroid(androidDTO);
        Ups ups = new Ups();
        androidDTO.setUps(ups);
        ThirdNotification thirdNotification = new ThirdNotification();
        ups.setNotification(thirdNotification);
        thirdNotification.setTitle(title);
        thirdNotification.setBody(body);
        thirdNotification.setClickType("startapp");
//        thirdNotification.setUrl("https://www.getui.com");
        // 两条消息的notify_id相同，新的消息会覆盖老的消息，取值范围：0-2147483647
        // thirdNotification.setNotifyId("11177");
        /*配置安卓厂商参数结束，更多参数请查看文档或对象源码*/

        /*设置ios厂商参数*/
        IosDTO iosDTO = new IosDTO();
        pushChannel.setIos(iosDTO);
        // 相同的collapseId会覆盖之前的消息
        iosDTO.setApnsCollapseId("" + pushMessage.messageId);
        Aps aps = new Aps();
        iosDTO.setAps(aps);
        Alert alert = new Alert();
        aps.setAlert(alert);
        alert.setTitle(title);
        alert.setBody(body);
        /*设置ios厂商参数结束，更多参数请查看文档或对象源码*/

        /*设置接收人信息*/
        Audience audience = new Audience();
        pushDTO.setAudience(audience);
        audience.addCid(pushMessage.getDeviceToken());
        /*设置接收人信息结束*/
        /**** 设置厂商相关参数，更多参数请查看文档或对象源码 ****/

        // 进行cid单推
        ApiResult<Map<String, Map<String, String>>> apiResult = pushApi.pushToSingleByCid(pushDTO);
        if (apiResult.isSuccess()) {
            // success
            System.out.println(apiResult.getData());
        } else {
            // failed
            System.out.println("code:" + apiResult.getCode() + ", msg: " + apiResult.getMsg());
        }
    }
}
