package cn.wildfirechat.push.hm;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.hm.payload.AlertPayload;
import cn.wildfirechat.push.hm.payload.VoipPayload;
import cn.wildfirechat.push.hm.payload.internal.Payload;
import cn.wildfirechat.push.hm.payload.internal.Target;
import com.google.gson.Gson;

import java.util.ArrayList;

public class RequestBody {
    public Payload payload;
    public PushOptions pushOptions;
    public Target target;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static RequestBody buildAlertRequestBody(PushMessage pushMessage) {
        RequestBody requestBody = new RequestBody();
        requestBody.payload = AlertPayload.buildAlertPayload(pushMessage);
        Target target = new Target();
        target.token = new ArrayList<>();
        target.token.add(pushMessage.deviceToken);
        requestBody.target = target;
        requestBody.pushOptions = PushOptions.buildPushOptions(pushMessage);
        return requestBody;
    }

    public static RequestBody buildVoipRequestBody(PushMessage pushMessage) {
        RequestBody requestBody = new RequestBody();
        requestBody.payload = VoipPayload.buildVoipPayload(pushMessage);
        Target target = new Target();
        target.token = new ArrayList<>();
        target.token.add(pushMessage.deviceToken);
        requestBody.target = target;
        requestBody.pushOptions = PushOptions.buildPushOptions(pushMessage);
        return requestBody;
    }
}
