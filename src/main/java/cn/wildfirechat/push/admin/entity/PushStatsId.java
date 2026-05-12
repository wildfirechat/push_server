package cn.wildfirechat.push.admin.entity;

import java.io.Serializable;
import java.util.Objects;

public class PushStatsId implements Serializable {
    private String platform;
    private String statDate;

    public PushStatsId() {}

    public PushStatsId(String platform, String statDate) {
        this.platform = platform;
        this.statDate = statDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PushStatsId)) return false;
        PushStatsId that = (PushStatsId) o;
        return Objects.equals(platform, that.platform) && Objects.equals(statDate, that.statDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, statDate);
    }
}
