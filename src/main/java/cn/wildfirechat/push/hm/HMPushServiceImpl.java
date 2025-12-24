package cn.wildfirechat.push.hm;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import cn.wildfirechat.push.Utility;
import cn.wildfirechat.push.hm.payload.AlertPayload;
import cn.wildfirechat.push.hm.payload.VoipPayload;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class HMPushServiceImpl implements HMPushService {
    private static final String AUD = "https://oauth-login.cloud.huawei.com/oauth2/v3/token";
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");
    private static final Logger LOG = LoggerFactory.getLogger(HMPushServiceImpl.class);
    private ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors() * 100,
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    @Autowired
    HMConfig config;

    private String pushUrl;

    @PostConstruct
    void setupPushUrl() {
        this.pushUrl = String.format("https://push-api.cloud.huawei.com/v3/%s/messages:send", config.getProjectId());
    }

    private String createJwt() throws NoSuchAlgorithmException, InvalidKeySpecException {
        RSAPrivateKey prk = (RSAPrivateKey) getPrivateKey(config.getPrivateKey());
        Algorithm algorithm = Algorithm.RSA256(null, prk);
        long iat = System.currentTimeMillis() / 1000;
        long exp = iat + 3600;
        JWTCreator.Builder builder =
            JWT.create()
                .withIssuer(config.getIss())
                .withKeyId(config.getKid())
                .withAudience(AUD)
                .withClaim("iat", iat)
                .withClaim("exp", exp);
        String jwt = builder.sign(algorithm);
        return jwt;
    }

    private PrivateKey getPrivateKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodeBase64(key));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

    private byte[] decodeBase64(String key) {
        return Base64.decodeBase64(key.getBytes(DEFAULT_CHARSET));
    }


    @Override
    public Object push(PushMessage pushMessage) {
        LOG.info("HM push {}", new Gson().toJson(pushMessage));
        if(Utility.filterPush(pushMessage)) {
            LOG.info("canceled");
            return "Canceled";
        }

        final long start = System.currentTimeMillis();
        executorService.execute(() -> {
            long now = System.currentTimeMillis();
            if (now - start > 15000) {
                LOG.error("等待太久，消息抛弃");
                return;
            }
            try {
                String jwt = createJwt();
                if (pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_RECALLED || pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_DELETED) {
                    //Todo not implement
                    //撤回或者删除消息，需要更新远程通知，暂未实现
                    return;
                }

                if (config.isSupportVoipPush() && (pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_INVITE || pushMessage.pushMessageType == PushMessageType.PUSH_MESSAGE_TYPE_VOIP_BYE)) {
                    RequestBody voipRequestBody = RequestBody.buildVoipRequestBody(pushMessage);
                    String response = httpPost(this.pushUrl, jwt, 10, voipRequestBody.toString(), 10000, 10000);
                    LOG.info("Push voip message to {} response {}", pushMessage.getDeviceToken(), response);
                } else {
                    RequestBody alertRequestBody = RequestBody.buildAlertRequestBody(pushMessage);
                    String response = httpPost(this.pushUrl, jwt, 0, alertRequestBody.toString(), 10000, 10000);
                    LOG.info("Push alert message to {} response {}", pushMessage.getDeviceToken(), response);
                }

            } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
                e.printStackTrace();
            }
        });

        return "OK";
    }

    private String httpPost(String httpUrl, String jwt, int pushType, String data, int connectTimeout, int readTimeout) throws IOException {
        OutputStream outPut = null;
        HttpURLConnection urlConnection = null;
        InputStream in = null;

        try {
            URL url = new URL(httpUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "Bearer " + jwt);
            urlConnection.setRequestProperty("push-type", pushType + "");
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
//            LOG.info(strBuf.toString());
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
