package cn.wildfirechat.push.android.fcm;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.PushMessageType;
import cn.wildfirechat.push.Utility;
import cn.wildfirechat.push.admin.entity.PushFile;
import cn.wildfirechat.push.admin.repository.PushFileRepository;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;

@Component
public class FCMPush {
    private static final Logger LOG = LoggerFactory.getLogger(FCMPush.class);
    @Autowired
    private FCMConfig mConfig;

    @Autowired
    private PushFileRepository pushFileRepository;

    @PostConstruct
    private void init() throws Exception {
        buildFirebaseApp();
    }

    public synchronized void refresh() {
        try {
            FirebaseApp app = FirebaseApp.getInstance();
            app.delete();
        } catch (IllegalStateException e) {
            // FirebaseApp not initialized yet, ignore
        } catch (Exception e) {
            LOG.error("FirebaseApp delete failed", e);
        }
        buildFirebaseApp();
    }

    private synchronized void buildFirebaseApp() {
        String credentialsPath = mConfig.getCredentialsPath();
        if (credentialsPath == null || credentialsPath.trim().isEmpty()) {
            LOG.warn("FCM credentialsPath is not configured");
            return;
        }
        java.util.Optional<PushFile> fileOpt = pushFileRepository.findByPlatformAndField("fcm", "fcm.credentialsPath");
        if (!fileOpt.isPresent() || fileOpt.get().getContent() == null) {
            LOG.warn("FCM credentials content not found in database");
            return;
        }
        try (ByteArrayInputStream refreshToken = new ByteArrayInputStream(fileOpt.get().getContent())) {
            FirebaseOptions.Builder optionsBuilder = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(refreshToken));
            // 如果配置了 databaseUrl，则设置
            String databaseUrl = mConfig.getDatabaseUrl();
            if (databaseUrl != null && !databaseUrl.trim().isEmpty()) {
                optionsBuilder.setDatabaseUrl(databaseUrl);
            }
            FirebaseApp.initializeApp(optionsBuilder.build());
            LOG.info("FCMPush initialized successfully");
        } catch (Exception e) {
            LOG.error("FCMPush init failed", e);
        }
    }


    public void push(PushMessage pushMessage) throws Exception {
        String[] arr = Utility.getPushTitleAndContent(pushMessage);
        String title = arr[0];
        String body = arr[1];

        Notification.Builder builder = Notification.builder().setTitle(title).setBody(body);
        Message message = Message.builder()
                .setNotification(builder.build())
                .setToken(pushMessage.deviceToken)
                .build();

        String response = FirebaseMessaging.getInstance().send(message);
        LOG.info("Successfully sent message: {}", response);
    }
}
