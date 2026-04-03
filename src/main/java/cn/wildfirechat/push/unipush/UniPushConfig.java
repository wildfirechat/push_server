package cn.wildfirechat.push.unipush;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "unipush")
@PropertySource(value = "file:config/unipush.properties")
public class UniPushConfig {
    private String url;
    private String huaweiCategory;
    private String harmonyCategory;
    private String vivoCategory;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHuaweiCategory() {
        return huaweiCategory;
    }

    public void setHuaweiCategory(String huaweiCategory) {
        this.huaweiCategory = huaweiCategory;
    }

    public String getHarmonyCategory() {
        return harmonyCategory;
    }

    public void setHarmonyCategory(String harmonyCategory) {
        this.harmonyCategory = harmonyCategory;
    }

    public String getVivoCategory() {
        return vivoCategory;
    }

    public void setVivoCategory(String vivoCategory) {
        this.vivoCategory = vivoCategory;
    }
}
