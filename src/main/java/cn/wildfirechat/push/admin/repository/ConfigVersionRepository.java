package cn.wildfirechat.push.admin.repository;

import cn.wildfirechat.push.admin.entity.ConfigVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigVersionRepository extends JpaRepository<ConfigVersion, Integer> {
}
