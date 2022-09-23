## 接入推送流程
1. 申请厂商推送服务
2. 移动端配置
3. 配置和部署推送服务(push-server)
4. IM-Server 配置
5. 推送测试
6. 问题排查
7. 常见问题

### 申请厂商推送服务
目前支持小米、华为、vivo、oppo、魅族、苹果等推送，需要到各个厂商的开发者后台申请推送相关 key

### 移动端配置
#### Android 端配置
Android端，推送相关的代码，都在```push module``` 下面，入口是```PushService```，配置之后，如果能调用```ChatManager#setDeviceToken```，则表示配置成，下面是具体的配置

1. 修改```push/build.gradle```下推送相关配置信息，如下:
    ```
    // 默认配置的 appid 和 appkey 不可以直接使用
    manifestPlaceholders = [

            MI_APP_ID    : "2882303761517722456",
            MI_APP_KEY   : "5731772292456",

            HMS_APP_ID   : "100221325",

            MEIZU_APP_ID : "113616",
            MEIZU_APP_KEY: "fcd886f51c144b45b87a67a28e2934d1",

            VIVO_APP_ID  : "12918",
            VIVO_APP_KEY : "c42feb05-de6c-427d-af55-4f902d9e0a75",

            OPPO_APP_KEY  : "16c6afe503b24259928e082ef01a6bf2",
            OPPO_APP_SECRET : "16c6afe503b24259928e082ef01a6bf2"
    ]
    ```
2. 华为推送需要```chat/agconnect-services.json```文件，该文件是华为推送后台生成的
3. FCM推送需要替换```push/google-services.json```文件，该文件是 FCM 推送后台生成的
4. 如果一切正常，启动 App 之后，会打印一行日志，**如果没有打印该日志，则说明配置错误，请查看日志，或者在```PushService```打断点调试**

    ```Log.d(TAG, "setDeviceToken" + token + " " + pushType);// 这是打印日志的代码！！ ```
5. 如果不需要某些推送类型，可以将其从```push module```删除，保留也不影响
6. 如果需要使用个推，请看```getui```分支

#### iOS 端配置

### 配置和部署推送服务
1. 修改配置

   本推送服务有1个工程配置文件和7个推送配置文件，都在工程的```config```目录下，请根据实际情况配置服务的端口和各个推送服务配置，推送服务配置一定要和移动端对应上，别配置成不同的 app 去了。

   如果有无法支持的推送类型，请修改客户端去掉不支持的类型（注意这里的配置文件要保留）。

2. 配置证书

    苹果和谷歌推送需要证书，请把对应证书分别放到```apns```和```fcm```目录下，然后修改配置文件中的证书路径。

3. 编译
    ```
    mvn package
    ```

4. 运行
    编译成功之后，在```target```目录找到```push-xxxx.jar```，然后把jar包、```config```目录、```apns```和```fcm```目录放到一起，然后执行下面命令：
    ```
    nohup java -jar push-xxxx.jar 2>&1 &
    ```

### IM-Server 配置
修改IM服务的配置文件```wildfirechat.conf```，指向推送服务器的地址，修改完后需要重启
```
#*********************************************************************
# Push server configuration
#*********************************************************************
##安卓推送服务器地址
push.android.server.address http://localhost:8085/android/push
##苹果推送服务器地址
push.ios.server.address http://localhost:8085/ios/push
```

### 推送测试
1. 确保双方都在线时，能互发消息
2. Android端，为了保证用户能正常收到消息，需要进行一些相关设置，产品上线之后，也需要引导用户进行相关设置，不设置会收到不到推送，具体设置方式不同手机不一样，请参考具体的手机设置，也可以参考[这儿](https://docs.rongcloud.cn/im/push/android/message_notification/)：
    1. 允许后台运行
    2. 允许自启动
    3. 允许后台弹出界面
    4. 允许显示通知
3. 将其中一方杀进程，另外一方向其发送文本消息
4. 查看被杀进程一方，是否收到推送

### 问题排查
如果遇到问题请按照以下步骤排查：
1. 请确保上面所有步骤都正确完成之后，再开始问题排查
2. 确保程序是非启动状态，如果退回到桌面，应用还是激活的还会继续收消息，此时就不会走推送服务。应用在后台激活状态时应该走本地通知。
3. 确认客户端推送SDK是否正确的获取到token，是否调用了setDeviceToken，token和type是多少？
4. 确认消息是否是自定义消息，如果是自定义消息，push content是否带上有内容？自定义消息只有push content不为空才会推送。
5. 确认目标客户端是否7日之内登录过，超过7天是不推送的。
6. 确认目标客户是否设置了全局静音或会话静音。
7. 如果有pc和web端登陆，确认是否设定了pc在线时手机静音。
8. 查看```IM-Server```日志，看是否有推送相关日志输出
    ```
    LOG.info("Send push to {}, message from {}", deviceId, sender); // 这是打印日志的代码！！
    ```
9. 确认推送服务是否收到了推送信息，如果收到，token和type是否和步骤1一致，推送内容是否和2一致？
    ```
    // 对方是 Android
    LOG.info("Android push {}", new Gson().toJson(pushMessage)); // 这是打印日志的代码
    // 对方是 iOS
    LOG.info("iOS push {}", new Gson().toJson(pushMessage)); // 这是打印日志的代码
    ```
10. 如果推送内容正确到达推送服务，则后面的排查就跟IM服务完全无关了，是推送厂商推送服务的调试，需要客户自己按照推送厂商的官方文档进行调试。

### 常见问题
TODO


