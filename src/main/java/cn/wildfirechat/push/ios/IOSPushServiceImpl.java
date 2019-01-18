package cn.wildfirechat.push.ios;

import cn.wildfirechat.push.PushMessage;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IOSPushServiceImpl implements IOSPushService {
    private static final Logger LOG = LoggerFactory.getLogger(IOSPushServiceImpl.class);
    @Autowired
    public ApnsServer apnsServer;

    @Override
    public Object push(PushMessage pushMessage) {
        LOG.info("iOS push {}", new Gson().toJson(pushMessage));
        apnsServer.pushMessage(pushMessage);
        return "OK";
    }
}
