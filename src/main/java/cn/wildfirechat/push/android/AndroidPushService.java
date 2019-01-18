package cn.wildfirechat.push.android;

import cn.wildfirechat.push.PushMessage;

public interface AndroidPushService {
    Object push(PushMessage pushMessage);
}
