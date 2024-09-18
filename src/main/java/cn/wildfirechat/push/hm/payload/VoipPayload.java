package cn.wildfirechat.push.hm.payload;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.hm.payload.internal.Payload;
import cn.wildfirechat.push.hm.payload.internal.Target;
import com.google.gson.Gson;

import java.util.ArrayList;


public class VoipPayload {
    Payload payload;
    Target target;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static VoipPayload buildAlertPayload(PushMessage pushMessage) {
        Target target = new Target();
        target.token = new ArrayList<>();
        target.token.add(pushMessage.deviceToken);

        VoipPayload voipPayload = new VoipPayload();
        voipPayload.payload = new Payload();
        voipPayload.payload.extraData = "TODO";
        voipPayload.target = target;

        return voipPayload;
    }
}


