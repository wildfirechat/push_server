package cn.wildfirechat.push.admin.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "push_stats")
@IdClass(PushStatsId.class)
public class PushStats {
    @Id
    @Column(name = "platform", length = 50)
    private String platform;

    @Id
    @Column(name = "stat_date", length = 10)
    private String statDate;

    @Column(name = "total_count", nullable = false)
    private Long totalCount = 0L;

    @Column(name = "success_count", nullable = false)
    private Long successCount = 0L;

    @Column(name = "fail_count", nullable = false)
    private Long failCount = 0L;

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

    public String getStatDate() {
        return statDate;
    }

    public void setStatDate(String statDate) {
        this.statDate = statDate;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getFailCount() {
        return failCount;
    }

    public void setFailCount(Long failCount) {
        this.failCount = failCount;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
