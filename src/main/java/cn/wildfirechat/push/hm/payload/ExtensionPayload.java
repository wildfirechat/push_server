package cn.wildfirechat.push.hm.payload;

import cn.wildfirechat.push.hm.payload.internal.Notification;
import cn.wildfirechat.push.hm.payload.internal.Payload;

public class ExtensionPayload extends Payload {
    public Notification notification;
    public String extraData;
}
