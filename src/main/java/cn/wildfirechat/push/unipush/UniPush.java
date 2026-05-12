package cn.wildfirechat.push.unipush;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.Utility;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// unipush 2.0
// 仅针对 uniapp 使用，先参考 uni-chat-uts 的 readme，配置推送
@Component
public class UniPush {
    private static final Logger LOG = LoggerFactory.getLogger(UniPush.class);
    private final Gson gson = new Gson();

    @Autowired
    private UniPushConfig mConfig;

    public void push(PushMessage pushMessage) throws IOException {
        UniPushBody body = UniPushBody.buildUniPushBody(pushMessage, mConfig);
        String response = httpPost(mConfig.getUrl(), gson.toJson(body), 10000, 10000);
        if (response == null) {
            throw new RuntimeException("UniPush failed: no response");
        }
        LOG.info("UniPush response: {}", response);
    }

    public String httpPost(String httpUrl, String data, int connectTimeout, int readTimeout) throws IOException {
        OutputStream outPut = null;
        HttpURLConnection urlConnection = null;
        InputStream in = null;

        try {
            URL url = new URL(httpUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            urlConnection.setConnectTimeout(connectTimeout);
            urlConnection.setReadTimeout(readTimeout);
            urlConnection.connect();

            outPut = urlConnection.getOutputStream();
            outPut.write(data.getBytes("UTF-8"));
            outPut.flush();

            int responseCode = urlConnection.getResponseCode();
            if (responseCode < 400) {
                in = urlConnection.getInputStream();
            } else {
                in = urlConnection.getErrorStream();
            }

            List<String> lines = IOUtils.readLines(in, urlConnection.getContentEncoding());
            StringBuilder strBuf = new StringBuilder();
            for (String line : lines) {
                strBuf.append(line);
            }
            String response = strBuf.toString();
            LOG.info(response);
            if (responseCode >= 400) {
                throw new RuntimeException("UniPush HTTP error: " + responseCode + ", body: " + response);
            }
            return response;
        } finally {
            IOUtils.closeQuietly(outPut);
            IOUtils.closeQuietly(in);
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private static class UniPushBody {
        String clientId;
        String title;
        String content;
        boolean forceNotification = true;
        Map<String, Object> payload;
        Map<String, String> category;

        static UniPushBody buildUniPushBody(PushMessage pushMessage, UniPushConfig config) {
            UniPushBody uniPushBody = new UniPushBody();
            uniPushBody.clientId = pushMessage.deviceToken;
            String[] arr = Utility.getPushTitleAndContent(pushMessage);
            uniPushBody.title = arr[0];
            uniPushBody.content = arr[1];
            Map<String, Object> payload = new HashMap<>();
            payload.put("pushMessage", pushMessage);
            uniPushBody.payload = payload;
            Map<String, String> category = new HashMap<>();
            category.put("harmony", config.getHarmonyCategory());
            category.put("huawei", config.getHuaweiCategory());
            category.put("vivo", config.getVivoCategory());
            uniPushBody.category = category;
            uniPushBody.forceNotification = true;

            return uniPushBody;
        }
    }
}
