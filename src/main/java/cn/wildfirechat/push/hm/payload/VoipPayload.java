package cn.wildfirechat.push.hm.payload;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.hm.payload.internal.Payload;
import cn.wildfirechat.push.hm.payload.internal.Target;
import com.google.gson.Gson;

import java.util.ArrayList;


public class VoipPayload extends Payload {
    public String extraData;

    public static VoipPayload buildVoipPayload(PushMessage pushMessage) {
        VoipPayload voipPayload = new VoipPayload();

        // 由于一般应用无法申请 voip 权限，故暂未实现
        // https://developer.huawei.com/consumer/cn/doc/harmonyos-guides/push-apply-right#section7291115452410
        voipPayload.extraData = "TODO";
        return voipPayload;
    }
}


