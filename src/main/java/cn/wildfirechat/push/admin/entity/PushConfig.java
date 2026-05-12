package cn.wildfirechat.push.admin.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "push_config")
@IdClass(PushConfigId.class)
public class PushConfig {
    @Id
    @Column(name = "platform", length = 50)
    private String platform;

    @Id
    @Column(name = "config_key", length = 100)
    private String configKey;

    @Column(name = "config_value", length = 2000)
    private String configValue;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PreUpdate
    @PrePersist
    public void preUpdate() {
        this.updatedAt = new Date();
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
