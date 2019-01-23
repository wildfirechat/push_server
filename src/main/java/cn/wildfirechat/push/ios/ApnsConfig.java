package cn.wildfirechat.push.ios;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix="apns")
@PropertySource(value = "file:config/apns.properties")
public class ApnsConfig {
    String productCerPath;
    String productCerPwd;

    String developCerPath;
    String developCerPwd;

    String voipCerPath;
    String voipCerPwd;

    String alert;
    String voipAlert;

    public String getProductCerPath() {
        return productCerPath;
    }

    public void setProductCerPath(String productCerPath) {
        this.productCerPath = productCerPath;
    }

    public String getProductCerPwd() {
        return productCerPwd;
    }

    public void setProductCerPwd(String productCerPwd) {
        this.productCerPwd = productCerPwd;
    }

    public String getDevelopCerPath() {
        return developCerPath;
    }

    public void setDevelopCerPath(String developCerPath) {
        this.developCerPath = developCerPath;
    }

    public String getDevelopCerPwd() {
        return developCerPwd;
    }

    public void setDevelopCerPwd(String developCerPwd) {
        this.developCerPwd = developCerPwd;
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
}
