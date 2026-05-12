package cn.wildfirechat.push.admin.repository;

import cn.wildfirechat.push.admin.entity.PushRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface PushRecordRepository extends JpaRepository<PushRecord, Long> {

    @Query("SELECT p FROM PushRecord p WHERE " +
           "(:startTime IS NULL OR p.pushTime >= :startTime) AND " +
           "(:endTime IS NULL OR p.pushTime <= :endTime) AND " +
           "(:success IS NULL OR p.success = :success) AND " +
           "(:userId IS NULL OR p.userId = :userId) " +
           "ORDER BY p.pushTime DESC")
    Page<PushRecord> findByConditions(
            @Param("startTime") Date startTime,
            @Param("endTime") Date endTime,
            @Param("success") Boolean success,
            @Param("userId") String userId,
            Pageable pageable);
}
