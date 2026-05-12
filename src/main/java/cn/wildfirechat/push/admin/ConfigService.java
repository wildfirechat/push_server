package cn.wildfirechat.push.admin;

import cn.wildfirechat.push.admin.entity.AdminUser;
import cn.wildfirechat.push.admin.entity.ConfigVersion;
import cn.wildfirechat.push.admin.entity.PushConfig;
import cn.wildfirechat.push.admin.entity.PushConfigId;
import cn.wildfirechat.push.admin.entity.PushFile;
import cn.wildfirechat.push.admin.repository.AdminUserRepository;
import cn.wildfirechat.push.admin.repository.ConfigVersionRepository;
import cn.wildfirechat.push.admin.repository.PushConfigRepository;
import cn.wildfirechat.push.admin.repository.PushFileRepository;
import cn.wildfirechat.push.android.fcm.FCMPush;
import cn.wildfirechat.push.hm.HMPushServiceImpl;
import cn.wildfirechat.push.ios.ApnsConfig;
import cn.wildfirechat.push.ios.ApnsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.auth.oauth2.GoogleCredentials;
import com.turo.pushy.apns.auth.ApnsSigningKey;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.MessageFormat;
import java.util.*;

@Service
public class ConfigService {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigService.class);

    private static final Set<String> PLATFORMS = new LinkedHashSet<>(Arrays.asList(
            "apns", "fcm", "getui", "hm", "hms", "honor", "oppo", "vivo", "xiaomi", "unipush"
    ));

    private static final Map<String, String> CONFIG_PREFIXES = new HashMap<>();
    static {
        CONFIG_PREFIXES.put("apns", "apns");
        CONFIG_PREFIXES.put("fcm", "fcm");
        CONFIG_PREFIXES.put("getui", "getui");
        CONFIG_PREFIXES.put("hm", "hm");
        CONFIG_PREFIXES.put("hms", "hms");
        CONFIG_PREFIXES.put("honor", "honor");
        CONFIG_PREFIXES.put("oppo", "oppo");
        CONFIG_PREFIXES.put("vivo", "vivo");
        CONFIG_PREFIXES.put("xiaomi", "xiaomi");
        CONFIG_PREFIXES.put("unipush", "unipush");
    }

    private static final Map<String, Map<String, String>> DEFAULT_CONFIG_FIELDS = new LinkedHashMap<>();
    static {
        Map<String, String> apnsDefaults = new LinkedHashMap<>();
        apnsDefaults.put("apns.auth_key_path", "");
        apnsDefaults.put("apns.key_id", "");
        apnsDefaults.put("apns.team_id", "");
        apnsDefaults.put("apns.alert", "default");
        apnsDefaults.put("apns.voipAlert", "ring.caf");
        apnsDefaults.put("apns.voipFeature", "false");
        DEFAULT_CONFIG_FIELDS.put("apns", apnsDefaults);

        Map<String, String> fcmDefaults = new LinkedHashMap<>();
        fcmDefaults.put("fcm.credentialsPath", "");
        fcmDefaults.put("fcm.databaseUrl", "");
        DEFAULT_CONFIG_FIELDS.put("fcm", fcmDefaults);

        Map<String, String> getuiDefaults = new LinkedHashMap<>();
        getuiDefaults.put("getui.appId", "");
        getuiDefaults.put("getui.appKey", "");
        getuiDefaults.put("getui.masterSecret", "");
        getuiDefaults.put("getui.domain", "https://restapi.getui.com/v2/");
        DEFAULT_CONFIG_FIELDS.put("getui", getuiDefaults);

        Map<String, String> hmDefaults = new LinkedHashMap<>();
        hmDefaults.put("hm.iss", "");
        hmDefaults.put("hm.kid", "");
        hmDefaults.put("hm.privateKey", "");
        hmDefaults.put("hm.projectId", "");
        hmDefaults.put("hm.supportVoipPush", "false");
        DEFAULT_CONFIG_FIELDS.put("hm", hmDefaults);

        Map<String, String> hmsDefaults = new LinkedHashMap<>();
        hmsDefaults.put("hms.appId", "");
        hmsDefaults.put("hms.appSecret", "");
        DEFAULT_CONFIG_FIELDS.put("hms", hmsDefaults);

        Map<String, String> honorDefaults = new LinkedHashMap<>();
        honorDefaults.put("honor.appId", "");
        honorDefaults.put("honor.appSecret", "");
        honorDefaults.put("honor.badgeClass", "");
        DEFAULT_CONFIG_FIELDS.put("honor", honorDefaults);

        Map<String, String> oppoDefaults = new LinkedHashMap<>();
        oppoDefaults.put("oppo.AppKey", "");
        oppoDefaults.put("oppo.AppSecret", "");
        DEFAULT_CONFIG_FIELDS.put("oppo", oppoDefaults);

        Map<String, String> vivoDefaults = new LinkedHashMap<>();
        vivoDefaults.put("vivo.appId", "");
        vivoDefaults.put("vivo.appKey", "");
        vivoDefaults.put("vivo.appSecret", "");
        DEFAULT_CONFIG_FIELDS.put("vivo", vivoDefaults);

        Map<String, String> xiaomiDefaults = new LinkedHashMap<>();
        xiaomiDefaults.put("xiaomi.appSecret", "");
        xiaomiDefaults.put("xiaomi.channelId", "");
        DEFAULT_CONFIG_FIELDS.put("xiaomi", xiaomiDefaults);

        Map<String, String> unipushDefaults = new LinkedHashMap<>();
        unipushDefaults.put("unipush.url", "");
        unipushDefaults.put("unipush.huaweiCategory", "");
        unipushDefaults.put("unipush.harmonyCategory", "");
        unipushDefaults.put("unipush.vivoCategory", "");
        DEFAULT_CONFIG_FIELDS.put("unipush", unipushDefaults);
    }

    @Autowired
    private PushConfigRepository pushConfigRepository;
    @Autowired
    private ConfigVersionRepository configVersionRepository;
    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private ApnsConfig apnsConfig;
    @Autowired
    private ApnsServer apnsServer;
    @Autowired
    private FCMPush fcmPush;
    @Autowired
    private HMPushServiceImpl hmPushService;
    @Autowired
    private PushFileRepository pushFileRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final Map<String, Object> configBeans = new HashMap<>();
    private volatile long localVersion = -1;

    @PostConstruct
    public void init() {
        configBeans.put("apns", apnsConfig);
        configBeans.put("fcm", fcmPush);
        configBeans.put("getui", null);
        configBeans.put("hm", null);
        configBeans.put("hms", null);
        configBeans.put("honor", null);
        configBeans.put("meizu", null);
        configBeans.put("oppo", null);
        configBeans.put("vivo", null);
        configBeans.put("xiaomi", null);
        configBeans.put("unipush", null);

        // 配置完全通过后台页面管理，不再从本地 properties 文件导入

        // 初始化本地版本号
        ConfigVersion cv = configVersionRepository.findById(1).orElse(null);
        localVersion = cv != null ? cv.getVersion() : 0;

        // 初始加载到内存
        refreshAllLocalBeans();
    }

    public synchronized void checkAndRefresh() {
        ConfigVersion cv = configVersionRepository.findById(1).orElse(null);
        if (cv == null) return;
        long dbVersion = cv.getVersion();
        if (dbVersion > localVersion) {
            LOG.info("Config version changed: {} -> {}, refreshing local beans...", localVersion, dbVersion);
            localVersion = dbVersion;
            refreshAllLocalBeans();
        }
    }

    private void refreshAllLocalBeans() {
        for (String platform : CONFIG_PREFIXES.keySet()) {
            ensureFilesOnDisk(platform);
            refreshBean(platform);
            refreshPushService(platform);
        }
    }

    public Map<String, Map<String, String>> getAllConfigs() {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        List<PushConfig> all = pushConfigRepository.findAll();
        for (PushConfig pc : all) {
            result.computeIfAbsent(pc.getPlatform(), k -> new LinkedHashMap<>())
                    .put(pc.getConfigKey(), pc.getConfigValue());
        }
        for (String platform : PLATFORMS) {
            Map<String, String> config = result.computeIfAbsent(platform, k -> new LinkedHashMap<>());
            fillDefaultFields(platform, config);
        }
        return result;
    }

    public Map<String, String> getConfig(String platform) {
        List<PushConfig> list = pushConfigRepository.findByPlatform(platform);
        Map<String, String> map = new LinkedHashMap<>();
        for (PushConfig pc : list) {
            map.put(pc.getConfigKey(), pc.getConfigValue());
        }
        fillDefaultFields(platform, map);
        return map;
    }

    private void fillDefaultFields(String platform, Map<String, String> config) {
        Map<String, String> fields = DEFAULT_CONFIG_FIELDS.get(platform);
        if (fields == null) return;
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (!config.containsKey(entry.getKey())) {
                config.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Transactional
    public boolean updateConfig(String platform, Map<String, String> config) {
        try {
            List<PushConfig> toSave = new ArrayList<>();
            for (Map.Entry<String, String> entry : config.entrySet()) {
                PushConfig pc = pushConfigRepository.findById(new PushConfigId(platform, entry.getKey()))
                        .orElse(new PushConfig());
                pc.setPlatform(platform);
                pc.setConfigKey(entry.getKey());
                pc.setConfigValue(entry.getValue());
                toSave.add(pc);
            }
            if (!toSave.isEmpty()) {
                pushConfigRepository.saveAll(toSave);
            }

            // 使用乐观锁更新版本号
            ConfigVersion cv = configVersionRepository.findById(1).orElse(new ConfigVersion());
            cv.setId(1);
            Long currentVersion = cv.getVersion();
            cv.setVersion(currentVersion != null ? currentVersion + 1 : 1);
            configVersionRepository.save(cv);

            // 立即刷新本地：使用传入的最新配置直接绑定，避免事务内查询延迟
            refreshBean(platform, config);
            refreshPushService(platform);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to update config for {}", platform, e);
            return false;
        }
    }

    private void refreshBean(String platform) {
        Object bean = configBeans.get(platform);
        String prefix = CONFIG_PREFIXES.get(platform);
        if (bean == null || prefix == null) return;
        try {
            Map<String, String> props = getConfig(platform);
            if (props.isEmpty()) return;
            doBindBean(bean, prefix, props);
        } catch (Exception e) {
            LOG.error("Failed to refresh config bean for {}", platform, e);
        }
    }

    private void refreshBean(String platform, Map<String, String> props) {
        Object bean = configBeans.get(platform);
        String prefix = CONFIG_PREFIXES.get(platform);
        if (bean == null || prefix == null) return;
        try {
            if (props.isEmpty()) return;
            doBindBean(bean, prefix, props);
        } catch (Exception e) {
            LOG.error("Failed to refresh config bean for {}", platform, e);
        }
    }

    private void doBindBean(Object bean, String prefix, Map<String, String> props) {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(props);
        Binder binder = new Binder(source);
        binder.bind(prefix, Bindable.ofInstance(bean));
        LOG.info("Refreshed config bean for {}", prefix);
    }

    private void refreshPushService(String platform) {
        try {
            switch (platform) {
                case "apns":
                    if (apnsServer != null) apnsServer.refresh();
                    break;
                case "fcm":
                    if (fcmPush != null) fcmPush.refresh();
                    break;
                case "hm":
                    if (hmPushService != null) hmPushService.refresh();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            LOG.error("Failed to refresh push service for {}", platform, e);
        }
    }

    public void saveFile(String platform, String field, String filename, byte[] content) {
        PushFile pf = pushFileRepository.findByPlatformAndField(platform, field)
                .orElse(new PushFile());
        pf.setPlatform(platform);
        pf.setField(field);
        pf.setFilename(filename);
        pf.setContent(content);
        pushFileRepository.save(pf);
        writeFileToDisk(platform, filename, content);
        LOG.info("Saved file to DB and disk: {}/{}", platform, filename);
    }

    public void ensureFilesOnDisk(String platform) {
        List<PushFile> files = pushFileRepository.findByPlatform(platform);
        for (PushFile file : files) {
            if (file.getContent() != null && file.getFilename() != null) {
                writeFileToDisk(file.getPlatform(), file.getFilename(), file.getContent());
            }
        }
    }

    private void writeFileToDisk(String platform, String filename, byte[] content) {
        String baseDir = new File("").getAbsolutePath();
        File dir = new File(baseDir + File.separator + platform);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, filename);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(content);
        } catch (IOException e) {
            LOG.error("Failed to write file to disk: {}", file.getAbsolutePath(), e);
        }
    }

    public String validateConfig(String platform, Map<String, String> config) {
        try {
            switch (platform) {
                case "apns":
                    return validateApnsConfig(config);
                case "fcm":
                    return validateFcmConfig(config);
                case "xiaomi":
                    return isEmpty(config.get("xiaomi.appSecret")) ? "小米推送 appSecret 不能为空" : null;
                case "hms":
                    return validateHmsConfig(config);
                case "vivo":
                    return (isEmpty(config.get("vivo.appId")) || "0".equals(config.get("vivo.appId")) || isEmpty(config.get("vivo.appKey")) || isEmpty(config.get("vivo.appSecret")))
                            ? "vivo 推送 appId、appKey 或 appSecret 不能为空" : null;
                case "oppo":
                    return (isEmpty(config.get("oppo.AppKey")) || isEmpty(config.get("oppo.AppSecret")))
                            ? "OPPO 推送 AppKey 或 AppSecret 不能为空" : null;
                case "getui":
                    return (isEmpty(config.get("getui.appId")) || isEmpty(config.get("getui.appKey")) || isEmpty(config.get("getui.masterSecret")))
                            ? "个推 appId、appKey 或 masterSecret 不能为空" : null;
                case "honor":
                    return validateHonorConfig(config);
                case "unipush":
                    String url = config.get("unipush.url");
                    if (isEmpty(url)) return "UniPush url 不能为空";
                    if (!url.startsWith("http")) return "UniPush url 格式不正确，应以 http:// 或 https:// 开头";
                    return null;
                case "hm":
                    return validateHmConfig(config);
                default:
                    return null;
            }
        } catch (Exception e) {
            LOG.error("Config validation error for {}", platform, e);
            return "验证异常: " + e.getMessage();
        }
    }

    private String validateApnsConfig(Map<String, String> config) {
        if (isEmpty(config.get("apns.auth_key_path")) || isEmpty(config.get("apns.key_id")) || isEmpty(config.get("apns.team_id"))) {
            return "APNs 尚未配置 p8 密钥，请先上传 auth_key_path 并填写 key_id 和 team_id";
        }
        String p8Path = config.get("apns.auth_key_path");
        if (!new File(p8Path).exists()) {
            return "APNs p8 密钥文件不存在";
        }
        try {
            ApnsSigningKey.loadFromPkcs8File(new File(p8Path), config.get("apns.team_id"), config.get("apns.key_id"));
        } catch (Exception e) {
            return "APNs p8 密钥文件无效";
        }
        return null;
    }

    private String validateFcmConfig(Map<String, String> config) {
        String credPath = config.get("fcm.credentialsPath");
        if (isEmpty(credPath)) {
            return "FCM 尚未配置 credentialsPath";
        }
        if (!new File(credPath).exists()) {
            return "FCM 凭证文件不存在";
        }
        try (FileInputStream is = new FileInputStream(credPath)) {
            GoogleCredentials.fromStream(is);
        } catch (Exception e) {
            return "FCM 凭证文件格式无效";
        }
        return null;
    }

    private String validateHmsConfig(Map<String, String> config) {
        String appId = config.get("hms.appId");
        String appSecret = config.get("hms.appSecret");
        if (isEmpty(appId) || "0".equals(appId) || isEmpty(appSecret)) {
            return "华为 HMS appId 或 appSecret 不能为空";
        }
        return validateOAuthToken("https://oauth-login.cloud.huawei.com/oauth2/v3/token", appId, appSecret, "华为 HMS");
    }

    private String validateHonorConfig(Map<String, String> config) {
        String appId = config.get("honor.appId");
        String appSecret = config.get("honor.appSecret");
        if (isEmpty(appId) || "0".equals(appId) || isEmpty(appSecret)) {
            return "荣耀推送 appId 或 appSecret 不能为空";
        }
        return validateOAuthToken("https://iam.developer.honor.com/auth/token", appId, appSecret, "荣耀推送");
    }

    private String validateOAuthToken(String tokenUrl, String appId, String appSecret, String platformName) {
        HttpURLConnection conn = null;
        try {
            String msgBody = MessageFormat.format(
                    "grant_type=client_credentials&client_secret={0}&client_id={1}",
                    URLEncoder.encode(appSecret, "UTF-8"), appId);
            URL url = new URL(tokenUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(msgBody.getBytes("UTF-8"));
            }
            int code = conn.getResponseCode();
            InputStream is = code < 400 ? conn.getInputStream() : conn.getErrorStream();
            String response = new String(org.apache.commons.io.IOUtils.toByteArray(is), "UTF-8");
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(response);
            if (obj.containsKey("access_token")) {
                return null;
            }
            String errorDesc = (String) obj.get("error_description");
            if (errorDesc == null) errorDesc = (String) obj.get("error");
            if (errorDesc == null) errorDesc = response;
            return platformName + " 凭证无效: " + errorDesc;
        } catch (Exception e) {
            return platformName + " 验证失败: " + e.getMessage();
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String validateHmConfig(Map<String, String> config) {
        String iss = config.get("hm.iss");
        String kid = config.get("hm.kid");
        String privateKey = config.get("hm.privateKey");
        if (isEmpty(iss) || isEmpty(kid) || isEmpty(privateKey)) {
            return "鸿蒙推送 iss、kid 或 privateKey 不能为空";
        }
        try {
            byte[] keyBytes = Base64.decodeBase64(privateKey.getBytes("UTF-8"));
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            return "鸿蒙推送 privateKey 格式无效，无法解析为 RSA 私钥";
        }
        return null;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public boolean updateAdminPassword(String newPassword) {
        try {
            List<AdminUser> users = adminUserRepository.findAll();
            if (!users.isEmpty()) {
                AdminUser user = users.get(0);
                user.setPassword(passwordEncoder.encode(newPassword));
                adminUserRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.error("Failed to update admin password", e);
            return false;
        }
    }

    public Set<String> getPlatforms() {
        return Collections.unmodifiableSet(PLATFORMS);
    }
}
