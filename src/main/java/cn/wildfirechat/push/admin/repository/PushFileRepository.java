package cn.wildfirechat.push.admin.repository;

import cn.wildfirechat.push.admin.entity.PushFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushFileRepository extends JpaRepository<PushFile, Long> {
    Optional<PushFile> findByPlatformAndField(String platform, String field);

    List<PushFile> findByPlatform(String platform);
}
