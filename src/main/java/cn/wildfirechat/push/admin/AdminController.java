package cn.wildfirechat.push.admin;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.admin.entity.AdminUser;
import cn.wildfirechat.push.admin.entity.PushFile;
import cn.wildfirechat.push.admin.entity.PushRecord;
import cn.wildfirechat.push.admin.repository.AdminUserRepository;
import cn.wildfirechat.push.admin.repository.PushFileRepository;
import cn.wildfirechat.push.android.AndroidPushService;
import cn.wildfirechat.push.hm.HMPushService;
import cn.wildfirechat.push.ios.IOSPushService;
import org.springframework.data.domain.Page;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private static final Logger LOG = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private ConfigService configService;

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private AndroidPushService androidPushService;

    @Autowired
    private IOSPushService iosPushService;

    @Autowired
    private HMPushService hmPushService;

    @Autowired
    private PushFileRepository pushFileRepository;

    @Autowired
    private PushRecordService pushRecordService;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MS = 60 * 60 * 1000; // 1 hour
    private final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();

    private static class LoginAttempt {
        int count;
        long firstFailTime;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        String username = params.get("username");
        String password = params.get("password");

        if (username == null || username.isEmpty()) {
            result.put("code", 400);
            result.put("message", "请输入用户名");
            return result;
        }

        // Check lock status
        LoginAttempt attempt = loginAttempts.get(username);
        if (attempt != null && attempt.count >= MAX_LOGIN_ATTEMPTS) {
            long elapsed = System.currentTimeMillis() - attempt.firstFailTime;
            if (elapsed < LOCK_DURATION_MS) {
                long remainingMinutes = (LOCK_DURATION_MS - elapsed) / (60 * 1000);
                result.put("code", 423);
                result.put("message", "登录失败次数过多，请 " + remainingMinutes + " 分钟后再试");
                return result;
            } else {
                // Lock expired, clear record
                loginAttempts.remove(username);
            }
        }

        Optional<AdminUser> userOpt = adminUserRepository.findByUsername(username);
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            // Login success, clear failure record
            loginAttempts.remove(username);
            AdminUser user = userOpt.get();
            Algorithm algorithm = Algorithm.HMAC256(user.getSecretKey());
            String token = JWT.create()
                    .withIssuer("push-admin")
                    .withClaim("role", "admin")
                    .withClaim("username", username)
                    .withExpiresAt(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                    .sign(algorithm);
            result.put("code", 200);
            result.put("token", token);
        } else {
            // Login failed, record attempt
            if (attempt == null) {
                attempt = new LoginAttempt();
                attempt.count = 1;
                attempt.firstFailTime = System.currentTimeMillis();
                loginAttempts.put(username, attempt);
            } else {
                attempt.count++;
            }
            int remaining = MAX_LOGIN_ATTEMPTS - attempt.count;
            if (remaining <= 0) {
                result.put("code", 423);
                result.put("message", "登录失败次数过多，账户已锁定 1 小时");
            } else {
                result.put("code", 401);
                result.put("message", "用户名或密码错误，还剩 " + remaining + " 次机会");
            }
        }
        return result;
    }

    @GetMapping("/configs")
    public Map<String, Object> getAllConfigs() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", configService.getAllConfigs());
        return result;
    }

    @GetMapping("/config/{platform}")
    public Map<String, Object> getConfig(@PathVariable String platform) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> config = configService.getConfig(platform);
        result.put("code", 200);
        result.put("data", config);
        return result;
    }

    @PostMapping("/config/{platform}")
    public Map<String, Object> updateConfig(@PathVariable String platform, @RequestBody Map<String, String> config) {
        Map<String, Object> result = new HashMap<>();
        boolean success = configService.updateConfig(platform, config);
        if (success) {
            String validateError = configService.validateConfig(platform, config);
            result.put("code", 200);
            result.put("message", "保存成功");
            if (validateError != null) {
                result.put("warning", validateError);
            }
        } else {
            result.put("code", 500);
            result.put("message", "保存失败");
        }
        return result;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        Map<String, Object> result = new HashMap<>();
        Map<String, StatisticsService.PlatformStats> stats = statisticsService.getAllStats();
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, StatisticsService.PlatformStats> entry : stats.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("platform", entry.getKey());
            item.put("totalCount", entry.getValue().getTotalCount());
            item.put("successCount", entry.getValue().getSuccessCount());
            item.put("failCount", entry.getValue().getFailCount());
            list.add(item);
        }
        result.put("code", 200);
        result.put("data", list);
        return result;
    }

    @GetMapping("/stats/daily")
    public Map<String, Object> getDailyStats(
            @RequestParam(name = "startDate", required = false) String startDate,
            @RequestParam(name = "endDate", required = false) String endDate,
            @RequestParam(name = "platform", required = false) String platform) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<cn.wildfirechat.push.admin.entity.PushStats> list;
            if (startDate != null && endDate != null) {
                list = statisticsService.getDailyStats(startDate, endDate, platform);
            } else if (startDate != null) {
                list = statisticsService.getDailyStats(startDate);
            } else {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Calendar cal = java.util.Calendar.getInstance();
                String end = sdf.format(cal.getTime());
                cal.add(java.util.Calendar.DAY_OF_MONTH, -6);
                String start = sdf.format(cal.getTime());
                list = statisticsService.getDailyStats(start, end, platform);
            }

            List<Map<String, Object>> data = new ArrayList<>();
            for (cn.wildfirechat.push.admin.entity.PushStats ps : list) {
                Map<String, Object> item = new HashMap<>();
                item.put("platform", ps.getPlatform());
                item.put("statDate", ps.getStatDate());
                item.put("totalCount", ps.getTotalCount());
                item.put("successCount", ps.getSuccessCount());
                item.put("failCount", ps.getFailCount());
                data.add(item);
            }
            result.put("code", 200);
            result.put("data", data);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    @PostMapping("/stats/reset")
    public Map<String, Object> resetStats() {
        Map<String, Object> result = new HashMap<>();
        statisticsService.resetStats();
        result.put("code", 200);
        result.put("message", "统计已重置");
        return result;
    }

    @PostMapping("/password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            result.put("code", 400);
            result.put("message", "密码不能为空");
            return result;
        }

        String username = getCurrentUsername(request);
        Optional<AdminUser> userOpt = adminUserRepository.findByUsername(username);
        if (!userOpt.isPresent()) {
            result.put("code", 400);
            result.put("message", "用户不存在");
            return result;
        }

        AdminUser user = userOpt.get();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            result.put("code", 400);
            result.put("message", "原密码错误");
            return result;
        }

        if (newPassword.length() < 4) {
            result.put("code", 400);
            result.put("message", "新密码长度不能少于4位");
            return result;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        adminUserRepository.save(user);
        result.put("code", 200);
        result.put("message", "密码修改成功");
        return result;
    }

    @GetMapping("/platforms")
    public Map<String, Object> getPlatforms() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("data", configService.getPlatforms());
        return result;
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadFile(
            @RequestParam("platform") String platform,
            @RequestParam("field") String field,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (!"apns".equals(platform) && !"fcm".equals(platform)) {
            result.put("code", 400);
            result.put("message", "不支持该平台上传文件");
            return result;
        }

        Set<String> allowedFields = new HashSet<>();
        if ("apns".equals(platform)) {
            allowedFields.add("apns.auth_key_path");
        } else if ("fcm".equals(platform)) {
            allowedFields.add("fcm.credentialsPath");
        }
        if (!allowedFields.contains(field)) {
            result.put("code", 400);
            result.put("message", "不支持的字段");
            return result;
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            result.put("code", 400);
            result.put("message", "文件名不能为空");
            return result;
        }
        int lastDot = originalName.lastIndexOf('.');
        if (lastDot == -1 || lastDot == originalName.length() - 1) {
            result.put("code", 400);
            result.put("message", "文件缺少有效的扩展名");
            return result;
        }
        String ext = originalName.substring(lastDot).toLowerCase();
        Set<String> allowedExts = new HashSet<>(Arrays.asList(".p8", ".json"));
        if (!allowedExts.contains(ext)) {
            result.put("code", 400);
            result.put("message", "只允许上传 .p8, .json 文件");
            return result;
        }

        // 限制上传文件大小 2MB
        if (file.getSize() > 2 * 1024 * 1024) {
            result.put("code", 400);
            result.put("message", "文件大小不能超过 2MB");
            return result;
        }

        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String relativePath = platform + "/" + safeName;

        // 读取文件内容并保存到数据库（各节点共享）
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            result.put("code", 500);
            result.put("message", "读取文件失败");
            return result;
        }
        configService.saveFile(platform, field, safeName, fileBytes);
        Map<String, String> config = configService.getConfig(platform);
        if (config == null) {
            config = new HashMap<>();
        }
        config.put(field, relativePath);
        boolean saved = configService.updateConfig(platform, config);

        if (saved) {
            result.put("code", 200);
            result.put("message", "上传成功");
            result.put("path", relativePath);
        } else {
            result.put("code", 500);
            result.put("message", "配置文件更新失败");
        }
        return result;
    }

    @PostMapping("/test/push")
    public Map<String, Object> testPush(@RequestBody Map<String, String> body) {
        Map<String, Object> result = new HashMap<>();
        String platform = body.get("platform");
        String deviceToken = body.get("deviceToken");
        String pushContent = body.get("pushContent");
        String packageName = body.getOrDefault("packageName", "cn.wildfire.chat");
        if (packageName == null || packageName.trim().isEmpty()) {
            packageName = "cn.wildfire.chat";
        }
        String env = body.getOrDefault("env", "development");

        if (platform == null || platform.isEmpty() || deviceToken == null || deviceToken.isEmpty()) {
            result.put("code", 400);
            result.put("message", "平台名称和设备token不能为空");
            return result;
        }

        String configError = validatePushConfig(platform);
        if (configError != null) {
            result.put("code", 400);
            result.put("message", configError);
            return result;
        }

        PushMessage pushMessage = new PushMessage();
        pushMessage.sender = "admin";
        pushMessage.senderName = "测试";
        pushMessage.pushContent = pushContent != null ? pushContent : "这是一条测试推送消息";
        pushMessage.deviceToken = deviceToken;
        pushMessage.packageName = packageName;
        pushMessage.pushMessageType = 0;
        pushMessage.line = 0;
        pushMessage.convType = 0;
        pushMessage.cntType = 0;

        try {
            switch (platform) {
                case "xiaomi":
                    pushMessage.pushType = 1;
                    androidPushService.testPush(pushMessage);
                    break;
                case "hms":
                    pushMessage.pushType = 2;
                    androidPushService.testPush(pushMessage);
                    break;
                case "meizu":
                    pushMessage.pushType = 3;
                    androidPushService.testPush(pushMessage);
                    break;
                case "vivo":
                    pushMessage.pushType = 4;
                    androidPushService.testPush(pushMessage);
                    break;
                case "oppo":
                    pushMessage.pushType = 5;
                    androidPushService.testPush(pushMessage);
                    break;
                case "fcm":
                    pushMessage.pushType = 6;
                    androidPushService.testPush(pushMessage);
                    break;
                case "getui":
                    pushMessage.pushType = 7;
                    androidPushService.testPush(pushMessage);
                    break;
                case "honor":
                    pushMessage.pushType = 9;
                    androidPushService.testPush(pushMessage);
                    break;
                case "unipush":
                    pushMessage.pushType = 10;
                    androidPushService.testPush(pushMessage);
                    break;
                case "apns":
                    if ("distribution".equalsIgnoreCase(env)) {
                        pushMessage.pushType = 0;
                    } else {
                        pushMessage.pushType = 1;
                    }
                    iosPushService.testPush(pushMessage);
                    break;
                case "hm":
                    pushMessage.pushType = 0;
                    hmPushService.testPush(pushMessage);
                    break;
                default:
                    result.put("code", 400);
                    result.put("message", "不支持的平台: " + platform);
                    return result;
            }
            result.put("code", 200);
            result.put("message", "推送请求已提交，具体结果请查看服务端日志和推送统计");
        } catch (Exception e) {
            LOG.error("测试推送失败", e);
            result.put("code", 500);
            result.put("message", "推送失败: " + e.getMessage());
        }
        return result;
    }

    private String validatePushConfig(String platform) {
        Map<String, String> config = configService.getConfig(platform);
        if (config == null) {
            config = new HashMap<>();
        }
        switch (platform) {
            case "xiaomi":
                if (isEmpty(config.get("xiaomi.appSecret"))) {
                    return "小米推送尚未配置 appSecret，请先完成配置";
                }
                break;
            case "hms":
                if (isEmpty(config.get("hms.appId")) || "0".equals(config.get("hms.appId")) || isEmpty(config.get("hms.appSecret"))) {
                    return "华为 HMS 推送尚未配置 appId 或 appSecret，请先完成配置";
                }
                break;
            case "vivo":
                if (isEmpty(config.get("vivo.appId")) || "0".equals(config.get("vivo.appId")) || isEmpty(config.get("vivo.appKey")) || isEmpty(config.get("vivo.appSecret"))) {
                    return "vivo 推送尚未配置 appId、appKey 或 appSecret，请先完成配置";
                }
                break;
            case "oppo":
                if (isEmpty(config.get("oppo.appKey")) || isEmpty(config.get("oppo.appSecret"))) {
                    return "OPPO 推送尚未配置 appKey 或 appSecret，请先完成配置";
                }
                break;
            case "fcm":
                String credPath = config.get("fcm.credentialsPath");
                if (isEmpty(credPath)) {
                    return "FCM 推送尚未配置 credentialsPath（服务账号 JSON 文件），请先完成配置";
                }
                java.util.Optional<PushFile> fcmFile = pushFileRepository.findByPlatformAndField("fcm", "fcm.credentialsPath");
                if (!fcmFile.isPresent() || fcmFile.get().getContent() == null) {
                    return "FCM 凭证文件未上传，请重新上传";
                }
                break;
            case "getui":
                if (isEmpty(config.get("getui.appId")) || isEmpty(config.get("getui.appKey")) || isEmpty(config.get("getui.masterSecret"))) {
                    return "个推尚未配置 appId、appKey 或 masterSecret，请先完成配置";
                }
                break;
            case "honor":
                if (isEmpty(config.get("honor.appId")) || "0".equals(config.get("honor.appId")) || isEmpty(config.get("honor.appSecret"))) {
                    return "荣耀推送尚未配置 appId 或 appSecret，请先完成配置";
                }
                break;
            case "unipush":
                String url = config.get("unipush.url");
                if (isEmpty(url)) {
                    return "UniPush 尚未配置 url（云函数地址），请先完成配置";
                }
                if (!url.startsWith("http")) {
                    return "UniPush url 格式不正确，应以 http:// 或 https:// 开头";
                }
                break;
            case "apns":
                if (isEmpty(config.get("apns.auth_key_path")) || isEmpty(config.get("apns.key_id")) || isEmpty(config.get("apns.team_id"))) {
                    return "APNs 尚未配置 p8 密钥，请先上传 auth_key_path 并填写 key_id 和 team_id";
                }
                java.util.Optional<PushFile> apnsFile = pushFileRepository.findByPlatformAndField("apns", "apns.auth_key_path");
                if (!apnsFile.isPresent() || apnsFile.get().getContent() == null) {
                    return "APNs p8 密钥未上传，请重新上传";
                }
                break;
            case "hm":
                if (isEmpty(config.get("hm.iss")) || isEmpty(config.get("hm.kid")) || isEmpty(config.get("hm.privateKey"))) {
                    return "鸿蒙推送尚未配置 iss、kid 或 privateKey，请先完成配置";
                }
                break;
            default:
                return null;
        }
        return null;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    @GetMapping("/records")
    public Map<String, Object> getRecords(
            @RequestParam(name = "startTime", required = false) String startTime,
            @RequestParam(name = "endTime", required = false) String endTime,
            @RequestParam(name = "success", required = false) Boolean success,
            @RequestParam(name = "userId", required = false) String userId,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = null;
            Date end = null;
            if (startTime != null && !startTime.isEmpty()) {
                start = sdf.parse(startTime);
            }
            if (endTime != null && !endTime.isEmpty()) {
                end = sdf.parse(endTime);
                // 前端 datetime-local 只精确到分钟，补全为 59 秒，确保包含当前分钟内全部记录
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(end);
                cal.set(java.util.Calendar.SECOND, 59);
                cal.set(java.util.Calendar.MILLISECOND, 999);
                end = cal.getTime();
            }
            Page<PushRecord> pageResult = pushRecordService.getRecords(start, end, success, userId, page, size);
            result.put("code", 200);
            result.put("data", pageResult.getContent());
            result.put("total", pageResult.getTotalElements());
            result.put("page", page);
            result.put("size", size);
        } catch (Exception e) {
            LOG.error("查询推送记录失败", e);
            result.put("code", 500);
            result.put("message", "查询失败: " + e.getMessage());
        }
        return result;
    }

    private String getCurrentUsername(HttpServletRequest request) {
        String username = (String) request.getAttribute("admin_username");
        return username != null ? username : "admin";
    }
}
