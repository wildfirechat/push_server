package cn.wildfirechat.push;

import cn.wildfirechat.push.android.AndroidPushService;
import cn.wildfirechat.push.ios.IOSPushService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PushController {

    @Autowired
    private AndroidPushService mAndroidPushService;

    @Autowired
    private IOSPushService mIOSPushService;

    @PostMapping(value = "/android/push", produces = "application/json;charset=UTF-8"   )
    public Object androidPush(@RequestBody PushMessage pushMessage) {
        return mAndroidPushService.push(pushMessage);
    }

    @PostMapping(value = "/ios/push", produces = "application/json;charset=UTF-8"   )
    public Object iOSPush(@RequestBody PushMessage pushMessage) {
        return mIOSPushService.push(pushMessage);
    }
}
