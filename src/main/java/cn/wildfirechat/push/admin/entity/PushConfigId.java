package cn.wildfirechat.push.admin.entity;

import java.io.Serializable;
import java.util.Objects;

public class PushConfigId implements Serializable {
    private String platform;
    private String configKey;

    public PushConfigId() {}

    public PushConfigId(String platform, String configKey) {
        this.platform = platform;
        this.configKey = configKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PushConfigId)) return false;
        PushConfigId that = (PushConfigId) o;
        return Objects.equals(platform, that.platform) && Objects.equals(configKey, that.configKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, configKey);
    }
}
