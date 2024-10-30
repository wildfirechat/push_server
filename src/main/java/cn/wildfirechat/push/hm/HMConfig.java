package cn.wildfirechat.push.hm;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "hm")
@PropertySource(value = "file:config/hm.properties")
public class HMConfig {
    private String privateKey;
    private String iss;
    private String kid;
    private String projectId;
    private boolean supportVoipPush = false;

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getIss() {
        return iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public boolean isSupportVoipPush() {
        return supportVoipPush;
    }

    public void setSupportVoipPush(boolean supportVoipPush) {
        this.supportVoipPush = supportVoipPush;
    }
}
