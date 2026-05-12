package cn.wildfirechat.push.admin.repository;

import cn.wildfirechat.push.admin.entity.PushConfig;
import cn.wildfirechat.push.admin.entity.PushConfigId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushConfigRepository extends JpaRepository<PushConfig, PushConfigId> {
    List<PushConfig> findByPlatform(String platform);
}
