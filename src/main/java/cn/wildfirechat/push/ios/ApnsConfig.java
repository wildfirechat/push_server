package cn.wildfirechat.push.ios;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix="apns")
@PropertySource(value = "file:config/apns.properties")
public class ApnsConfig {
    String cerPath;
    String cerPwd;

    String voipCerPath;
    String voipCerPwd;

    String alert;
    String voipAlert;

    boolean voipFeature;

    public String getCerPath() {
        return cerPath;
    }

    public void setCerPath(String cerPath) {
        this.cerPath = cerPath;
    }

    public String getCerPwd() {
        return cerPwd;
    }

    public void setCerPwd(String cerPwd) {
        this.cerPwd = cerPwd;
    }

    public String getVoipCerPath() {
        return voipCerPath;
    }

    public void setVoipCerPath(String voipCerPath) {
        this.voipCerPath = voipCerPath;
    }

    public String getVoipCerPwd() {
        return voipCerPwd;
    }

    public void setVoipCerPwd(String voipCerPwd) {
        this.voipCerPwd = voipCerPwd;
    }

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
}
