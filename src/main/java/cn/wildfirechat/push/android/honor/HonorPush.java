package cn.wildfirechat.push.android.honor;


import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.android.honor.internal.RequestBody;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

@Component
public class HonorPush {
    private static final Logger LOG = LoggerFactory.getLogger(HonorPush.class);
    private static final String tokenUrl = "https://iam.developer.honor.com/auth/token"; //获取认证Token的URL
    private static final String apiUrl = "https://push-api.cloud.honor.com/api/v1/%s/sendMessage"; //应用级消息下发API
    private String accessToken;//下发通知消息的认证Token
    private long tokenExpiredTime = 0;  //accessToken的过期时间，初始化为0

    @Autowired
    private HonorConfig mConfig;

    /**
     * 检查token是否有效
     *
     * @return true if token is valid and not expired
     */
    private boolean isTokenValid() {
        if (StringUtils.isEmpty(accessToken)) {
            LOG.debug("Honor token is empty");
            return false;
        }

        long currentTime = System.currentTimeMillis();
        if (tokenExpiredTime <= currentTime) {
            LOG.debug("Honor token expired. Current: {}, Expired: {}", currentTime, tokenExpiredTime);
            return false;
        }

        long remainingTime = (tokenExpiredTime - currentTime) / 1000;
        LOG.debug("Honor token is valid, remaining {} seconds", remainingTime);
        return true;
    }

    //获取下发通知消息的认证Token
    private void refreshToken() throws IOException {
        LOG.info("Honor refresh token");
        String msgBody = MessageFormat.format(
            "grant_type=client_credentials&client_secret={0}&client_id={1}",
            URLEncoder.encode(mConfig.getAppSecret(), "UTF-8"), mConfig.getAppId());
        String response = httpPost(tokenUrl, "", msgBody, 5000, 5000);
        JSONObject obj = JSONObject.parseObject(response);

        if (obj.containsKey("access_token")) {
            accessToken = obj.getString("access_token");
            // 设置过期时间，提前5分钟刷新
            long expiresIn = obj.getLong("expires_in") * 1000; // 转换为毫秒
            tokenExpiredTime = System.currentTimeMillis() + expiresIn - 5 * 60 * 1000;
            LOG.info("Honor token refreshed successfully, expires in {} seconds", obj.getLong("expires_in"));
        } else {
            LOG.error("Failed to get access_token from response: {}", response);
            throw new IOException("Failed to get access_token from Honor auth response");
        }
    }

    //发送Push消息
    public void push(PushMessage pushMessage) {
        // 检查token是否有效，无效则刷新
        if (!isTokenValid()) {
            try {
                refreshToken();
            } catch (IOException e) {
                LOG.error("Failed to refresh Honor token", e);
                return; // token刷新失败，直接返回
            }
        }
        /*PushManager.requestToken为客户端申请token的方法，可以调用多次以防止申请token失败*/
        /*PushToken不支持手动编写，需使用客户端的onToken方法获取*/
//        JSONArray deviceTokens = new JSONArray();//目标设备Token
//        deviceTokens.add(pushMessage.getDeviceToken());
//
//        JSONObject param = new JSONObject();
//        param.put("appPkgName", pushMessage.packageName);//定义需要打开的appPkgName
//        JSONObject action = new JSONObject();
//        action.put("type", 3);//类型3为打开APP，其他行为请参考接口文档设置
//        action.put("param", param);//消息点击动作参数
//
//
//        JSONObject msg = new JSONObject();
//        // 透传消息
//        msg.put("type", 3);//3: 通知栏消息，异步透传消息请根据接口文档设置
//        msg.put("action", action);//消息点击动作 add by liguangyu
//
//        String token = pushMessage.getDeviceToken();
//        pushMessage.deviceToken = null;
////        msg.put("body", new Gson().toJson(pushMessage));//通知栏消息body内容
//
//        JSONObject body = new JSONObject();//仅通知栏消息需要设置标题和内容，透传消息key和value为用户自定义
//        body.put("title", pushMessage.senderName);//消息标题
//        body.put("content", pushMessage.pushContent);//消息内容体
//        //body.put("info", new Gson().toJson(pushMessage));//消息内容体
//        msg.put("body", body);//通知栏消息body内容示例代码
//        LOG.info("liguangyu test body: {} pushMessage{}",body,new Gson().toJson(pushMessage) );
//
//        // 华为消息分类
//        msg.put("importance", "NORMAL");
//        msg.put("category", "IM");
//
//        JSONObject hps = new JSONObject();//华为PUSH消息总结构体
//        hps.put("msg", msg);
//
//        JSONObject payload = new JSONObject();
//        payload.put("hps", hps);
//
//        LOG.info("send push to Honor {}", payload);

        try {
//            String postBody = MessageFormat.format(
//                "access_token={0}&nsp_svc={1}&nsp_ts={2}&device_token_list={3}&payload={4}",
//                URLEncoder.encode(accessToken,"UTF-8"),
//                URLEncoder.encode("openpush.message.api.send","UTF-8"),
//                URLEncoder.encode(String.valueOf(System.currentTimeMillis() / 1000),"UTF-8"),
//                URLEncoder.encode(deviceTokens.toString(),"UTF-8"),
//                URLEncoder.encode(payload.toString(),"UTF-8"));
            RequestBody alertPayload = RequestBody.buildRequestBody(pushMessage, mConfig.getAppId());
            LOG.info("Push message {}", alertPayload);
            //String postUrl = apiUrl + "?nsp_ctx=" + URLEncoder.encode("{\"ver\":\"1\", \"appId\":\"" + mConfig.getAppId() + "\"}", "UTF-8");
            String postUrl = String.format(apiUrl, mConfig.getAppId());
            String response = httpPost(postUrl, accessToken, alertPayload.toString(), 8000, 8000);
            LOG.info("Push to {} response {}", pushMessage.getDeviceToken(), response);
        } catch (IOException e) {
            e.printStackTrace();
            LOG.info("Push to {} with exception", pushMessage.getDeviceToken(), e);
        }
    }

    public String httpPost(String httpUrl, String jwt, String data, int connectTimeout, int readTimeout) throws IOException {
        OutputStream outPut = null;
        HttpURLConnection urlConnection = null;
        InputStream in = null;

        try {
            URL url = new URL(httpUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            if (StringUtils.isNotEmpty(jwt)) {
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Authorization", "Bearer " + jwt);
                urlConnection.setRequestProperty("timestamp", "" + System.currentTimeMillis());
                //urlConnection.setRequestProperty("push-type", pushType + "");
            } else {
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            }

            urlConnection.setConnectTimeout(connectTimeout);
            urlConnection.setReadTimeout(readTimeout);
            urlConnection.connect();

            // POST data
            outPut = urlConnection.getOutputStream();
            outPut.write(data.getBytes("UTF-8"));
            outPut.flush();

            // read response
            if (urlConnection.getResponseCode() < 400) {
                in = urlConnection.getInputStream();
            } else {
                in = urlConnection.getErrorStream();
            }

            List<String> lines = IOUtils.readLines(in, urlConnection.getContentEncoding());
            StringBuffer strBuf = new StringBuffer();
            for (String line : lines) {
                strBuf.append(line);
            }
            LOG.info(strBuf.toString());
            return strBuf.toString();
        } finally {
            IOUtils.closeQuietly(outPut);
            IOUtils.closeQuietly(in);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
