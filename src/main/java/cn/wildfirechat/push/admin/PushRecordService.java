package cn.wildfirechat.push.admin;

import cn.wildfirechat.push.PushMessage;
import cn.wildfirechat.push.admin.entity.PushRecord;
import cn.wildfirechat.push.admin.repository.PushRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PushRecordService {
    private static final Logger LOG = LoggerFactory.getLogger(PushRecordService.class);

    @Autowired
    private PushRecordRepository pushRecordRepository;

    public void saveRecord(PushMessage pushMessage, String platform, boolean success, String errorMsg) {
        try {
            PushRecord record = new PushRecord();
            record.setPlatform(platform);
            record.setUserId(pushMessage.userId);
            record.setDeviceToken(pushMessage.getDeviceToken());
            String content = pushMessage.getPushContent();
            if (content != null && content.length() > 2000) {
                content = content.substring(0, 1997) + "...";
            }
            record.setPushContent(content);
            record.setPushType(String.valueOf(pushMessage.getPushType()));
            record.setSuccess(success);
            if (errorMsg != null && errorMsg.length() > 2000) {
                errorMsg = errorMsg.substring(0, 1997) + "...";
            }
            record.setErrorMsg(errorMsg);
            record.setPushTime(new Date());
            pushRecordRepository.save(record);
        } catch (Exception e) {
            LOG.error("Failed to save push record", e);
        }
    }

    public Page<PushRecord> getRecords(Date startTime, Date endTime, Boolean success, String userId, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), Math.max(1, Math.min(size, 100)));
        return pushRecordRepository.findByConditions(startTime, endTime, success, userId, pageable);
    }
}
