package cn.wildfirechat.push.ios;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="apns")
public class ApnsConfig {
    String authKeyPath;
    String keyId;
    String teamId;

    String alert;
    String voipAlert;

    boolean voipFeature;

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    public String getVoipAlert() {
        return voipAlert;
    }

    public void setVoipAlert(String voipAlert) {
        this.voipAlert = voipAlert;
    }

    public boolean isVoipFeature() {
        return voipFeature;
    }

    public void setVoipFeature(boolean voipFeature) {
        this.voipFeature = voipFeature;
    }

    public String getAuthKeyPath() {
        return authKeyPath;
    }

    public void setAuthKeyPath(String authKeyPath) {
        this.authKeyPath = authKeyPath;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }
}
