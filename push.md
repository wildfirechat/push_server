## 接入推送流程
1. 申请厂商推送服务
2. 移动端配置
3. 配置和部署推送服务(push-server)
4. IM-Server 配置
5. 推送测试
6. 调试推送服务
7. 问题排查

### 一，申请厂商推送服务
目前支持小米、华为、vivo、oppo、魅族、苹果等推送，需要到各个厂商的开发者后台申请推送相关 key

### 二，移动端配置
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
4. 如果一切正常，启动 App 之后，会打印下面这一行日志，**如果没有打印该日志，则说明配置错误，请查看日志，或者在```PushService```打断点调试**：

      ```
      Log.d(TAG, "setDeviceToken" + token + " " + pushType);// 这是打印日志的代码！！
      ```
5. 如果不需要某些推送类型，可以将其从```push module```删除，保留也不影响
6. 如果需要使用个推，请看```getui```分支

#### iOS 端配置
请参考[ios-chat](https://github.com/wildfirechat/ios-chat)项目```appdelegate.m```文件中的关于推送注册的部分。当调用到```setDeviceToken```方法传入推送token即为客户端接入成功。

### 三，配置和部署推送服务
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

### 四，IM-Server 配置
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

### 五，推送测试
1. 确保双方都在线时，能互发消息
2. Android端，为了保证用户能正常收到消息，需要进行一些相关设置，产品上线之后，也需要引导用户进行相关设置，不设置会收到不到推送，具体设置方式不同手机不一样，请参考具体的手机设置，也可以参考[这儿](https://docs.rongcloud.cn/im/push/android/message_notification/)：
    1. 允许后台运行
    2. 允许自启动
    3. 允许后台弹出界面
    4. 允许显示通知
3. 将其中一方杀进程，另外一方向其发送文本消息
4. 查看被杀进程一方，是否收到推送

### 六，调试推送服务
推送厂商SDK可能随时更新，接口也有可能由变更，推送服务和客户端推送SDK可能需要更新和调整。也有可能推送厂商开发后台配置错误，导致无法推送成功。出现这种情况后，请按照对应厂商的最新说明，调试推送服务。推送服务收到IM服务推送请求后，调用厂商SDK进行推送，这部分工作与野火IM无关了，请仔细阅读推送厂商的文档或者与联系推送厂商的技术支持。

### 七，问题排查
如果遇到问题请按照以下步骤排查：
1. 请确保上面所有步骤都正确完成之后，再开始问题排查
2. 确保程序是非启动状态，如果退回到桌面，应用还是激活的还会继续收消息，此时就不会走推送服务。应用在后台激活状态时应该走本地通知。
3. 确认客户端推送SDK是否正确的获取到token，是否调用了```setDeviceToken```，```token```和```type```是多少？
4. 上一步成功之后，```IM-Server```数据库的```t_user_session```表的```_token```和```_push_type```字段会被填上上一步设置的值。
5. 确认消息是否是自定义消息，如果是自定义消息，```push content```或者```push data```至少一个不为空才会推送。另外消息的[PersistFlag](https://docs.wildfirechat.cn/base_knowledge/message_content.html#消息类型)必须是存储或者存储计数属性的才会推送。
6. 确认目标客户端是否7日之内登录过，超过7天是不推送的。
7. 确认目标客户是否设置了全局静音或会话静音。
8. 如果有pc和web端登陆，确认是否设定了pc在线时手机静音。
9. 查看```IM-Server```日志，看是否有推送相关日志输出
    ```
    LOG.info("Send push to {}, message from {}", deviceId, sender); // 这是打印日志的代码！！
    ```
10. 确认推送服务是否收到了推送信息，如果收到，token和type是否和步骤1一致，推送内容是否和2一致？
     ```
     // 目标用户是 Android
     LOG.info("Android push {}", new Gson().toJson(pushMessage)); // 这是打印日志的代码
     // 目标用户是 iOS
     LOG.info("iOS push {}", new Gson().toJson(pushMessage)); // 这是打印日志的代码
     ```
11. 推送服务收到IM请求的推送信息，调用厂商SDK进行推送，检查代码确认是透传方式还是通知栏方式。
12. 如果推送服务使用的是通知栏方式，后面的工作就全是推送厂商的工作了，请按照推送厂商的官方文档进行调试。
13. 如果推送服务使用的是透传方式，请确认客户端对应推送SDK是否收到透传消息，如果没有收到透传消息，则问题出在推送通道上，请按照推送厂商的官方文档进行调试。
14. 如果推送服务使用的是透传方式，确认对应推送SDK收到了透传消息，请检查应用激活后是否初始化IM SDK并调用connect方法，及连接状态是否连接成功，是否收到新消息，是否弹出本地通知。

### 技术支持
按照文档一般情况下都能成功处理推送功能。实际上推送的功能并不复杂，只是涉及到太多的环节，每个环节又是由不同的研发或者公司来负责。请一定要理解整个推送的过程，知道推送过程中每一环节的功能，每个环节由谁来负责或者检查，只有真正的理解了推送的完整流程，才能找到对应的人来处理，才有可能高效地处理推送问题。

当确认是野火负责的环节时，可以来给野火提issue或者论坛发帖问。提问时请写清楚下面几个要求：
1. 请写出推送的完整流程。
2. 请写出推送的每个环节是那一方来负责，比如android端注册推送并调用setDeviceToken由你们android研发来负责。
3. 你认为那个环节出了问题，并给出证据。因为我们没有服务和客户端的任何信息，所以请务必给出全面详细的信息。
4. 上面问题排查中每一步的结果。

**只有了解推送的流程和每个环节的功能才能高效地沟通，所以只有写清楚上面四条信息我们才能够提供技术支持。**

### 附录
#### IM-Server 调用推送服务 HTTP 请求说明
使用POST方式，内容为JSON格式，参数如下

| 参数 | 类型 | 必需 | 描述 |
| ------ | ------ | --- | ------ |
| sender | string | 是 | 发送者ID |
| senderName | string | 是 | 发送者姓名 |
| convType | int | 是 | 会话类型 |
| target | string | 是 | 接收用户ID |
| targetName | string | 是 | 接收用户名称 |
| line | int | 否 | 会话线路，缺省为0 |
| serverTime | long | 是 | 消息时间 |
| pushMessageType | int | 是 | 0 普通消息；1 voip消息。在支持透传的系统上，voip消息用透传 |
| pushType | int | 是 | 推送类型，android推送分为小米/华为/魅族等。ios分别为开发和发布。 |
| pushContent | string | 是 | 消息推送内容 |
| pushData | string | 否 | 消息推送数据 |
| unReceivedMsg | int | 是 | 服务器端没有接收下来的消息数（只计算计数消息） |
| mentionedType | int | 否 | 消息提醒类型，0，没提醒；1，提醒了当前用户；2，提醒了所有人 |
| packageName | string | 否 | 应用包名 |
| deviceToken | int | 否 | 设备token |
| isHiddenDetail | bool | 否 | 是否要隐藏推送详情 |
| language | string | 否 | 接收者的手机语言 |

