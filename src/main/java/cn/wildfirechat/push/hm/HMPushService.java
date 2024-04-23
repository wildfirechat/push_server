package cn.wildfirechat.push.hm;

import cn.wildfirechat.push.PushMessage;

public interface HMPushService {
    Object push(PushMessage pushMessage);
}
