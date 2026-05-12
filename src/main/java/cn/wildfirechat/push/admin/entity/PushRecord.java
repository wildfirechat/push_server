package cn.wildfirechat.push.admin.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "push_record")
public class PushRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform", length = 50)
    private String platform;

    @Column(name = "user_id", length = 100)
    private String userId;

    @Column(name = "device_token", length = 500)
    private String deviceToken;

    @Column(name = "push_content", length = 2000)
    private String pushContent;

    @Column(name = "push_type", length = 50)
    private String pushType;

    @Column(name = "success")
    private boolean success;

    @Column(name = "error_msg", length = 2000)
    private String errorMsg;

    @Column(name = "push_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date pushTime;

    @PrePersist
    public void prePersist() {
        if (this.pushTime == null) {
            this.pushTime = new Date();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getPushContent() {
        return pushContent;
    }

    public void setPushContent(String pushContent) {
        this.pushContent = pushContent;
    }

    public String getPushType() {
        return pushType;
    }

    public void setPushType(String pushType) {
        this.pushType = pushType;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }
}
