package cn.wildfirechat.push.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConfigRefreshTask {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigRefreshTask.class);

    @Autowired
    private ConfigService configService;

    @Scheduled(fixedRate = 30000)
    public void checkConfigUpdate() {
        try {
            configService.checkAndRefresh();
        } catch (Exception e) {
            LOG.error("Config refresh task error", e);
        }
    }
}
