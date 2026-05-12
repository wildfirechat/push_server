package cn.wildfirechat.push.admin.repository;

import cn.wildfirechat.push.admin.entity.PushStats;
import cn.wildfirechat.push.admin.entity.PushStatsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushStatsRepository extends JpaRepository<PushStats, PushStatsId> {

    @Query("SELECT ps FROM PushStats ps WHERE ps.statDate BETWEEN :startDate AND :endDate ORDER BY ps.statDate DESC, ps.platform")
    List<PushStats> findByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query("SELECT ps FROM PushStats ps WHERE ps.statDate = :date ORDER BY ps.platform")
    List<PushStats> findByDate(@Param("date") String date);

    @Query("SELECT ps FROM PushStats ps WHERE ps.platform = :platform AND ps.statDate BETWEEN :startDate AND :endDate ORDER BY ps.statDate DESC")
    List<PushStats> findByPlatformAndDateRange(@Param("platform") String platform, @Param("startDate") String startDate, @Param("endDate") String endDate);

    @Query("SELECT SUM(ps.totalCount), SUM(ps.successCount), SUM(ps.failCount) FROM PushStats ps WHERE ps.statDate BETWEEN :startDate AND :endDate")
    List<Object[]> sumByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
