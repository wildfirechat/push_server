package cn.wildfirechat.push.admin.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "config_version")
public class ConfigVersion {
    @Id
    private Integer id = 1;

    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Version
    @Column(name = "opt_lock")
    private Integer optLock;

    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @PreUpdate
    @PrePersist
    public void preUpdate() {
        this.updatedAt = new Date();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Integer getOptLock() {
        return optLock;
    }

    public void setOptLock(Integer optLock) {
        this.optLock = optLock;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
