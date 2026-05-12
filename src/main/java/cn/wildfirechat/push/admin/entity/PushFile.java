package cn.wildfirechat.push.admin.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "push_file")
public class PushFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "platform", length = 50, nullable = false)
    private String platform;

    @Column(name = "field", length = 100, nullable = false)
    private String field;

    @Column(name = "filename", length = 200)
    private String filename;

    @Lob
    @Column(name = "content", nullable = false)
    private byte[] content;

    @Column(name = "upload_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadTime;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.uploadTime = new Date();
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

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }
}
