package cn.wildfirechat.push.ios;

import cn.wildfirechat.push.PushMessage;

public interface IOSPushService {
    Object push(PushMessage pushMessage);
    void testPush(PushMessage pushMessage) throws Exception;
}
