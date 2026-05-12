package cn.wildfirechat.push.admin;

import cn.wildfirechat.push.admin.entity.PushStats;
import cn.wildfirechat.push.admin.repository.PushStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class StatisticsService {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private PushStatsRepository pushStatsRepository;

    // 当前日期的内存统计
    private final Map<String, PlatformStats> dailyStatsMap = new ConcurrentHashMap<>();
    private volatile String currentDate;
    private final ScheduledExecutorService syncExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ReentrantLock flushLock = new ReentrantLock();

    @PostConstruct
    public void init() {
        currentDate = LocalDate.now().format(DATE_FORMAT);
        // 从数据库加载当天已有统计，避免重启后覆盖历史数据
        List<PushStats> todayStats = pushStatsRepository.findByDate(currentDate);
        for (PushStats ps : todayStats) {
            PlatformStats stats = new PlatformStats();
            stats.totalCount.set(ps.getTotalCount());
            stats.successCount.set(ps.getSuccessCount());
            stats.failCount.set(ps.getFailCount());
            dailyStatsMap.put(ps.getPlatform(), stats);
        }
        syncExecutor.scheduleWithFixedDelay(this::flushToDatabase, 10, 10, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void destroy() {
        syncExecutor.shutdown();
        try {
            if (!syncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                syncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            syncExecutor.shutdownNow();
        }
        flushToDatabase();
    }

    public void recordPush(String platform) {
        checkDateChange();
        PlatformStats stats = dailyStatsMap.computeIfAbsent(platform, k -> new PlatformStats());
        stats.totalCount.incrementAndGet();
    }

    public void recordSuccess(String platform) {
        checkDateChange();
        PlatformStats stats = dailyStatsMap.computeIfAbsent(platform, k -> new PlatformStats());
        stats.successCount.incrementAndGet();
    }

    public void recordFail(String platform) {
        checkDateChange();
        PlatformStats stats = dailyStatsMap.computeIfAbsent(platform, k -> new PlatformStats());
        stats.failCount.incrementAndGet();
    }

    private void checkDateChange() {
        String today = LocalDate.now().format(DATE_FORMAT);
        if (!today.equals(currentDate)) {
            flushLock.lock();
            try {
                // 日期变化，先flush旧数据，然后重置
                flushWithDate(currentDate);
                dailyStatsMap.clear();
                currentDate = today;
            } finally {
                flushLock.unlock();
            }
        }
    }

    public Map<String, PlatformStats> getAllStats() {
        checkDateChange();
        return new HashMap<>(dailyStatsMap);
    }

    public List<PushStats> getDailyStats(String startDate, String endDate, String platform) {
        if (platform != null && !platform.isEmpty()) {
            return pushStatsRepository.findByPlatformAndDateRange(platform, startDate, endDate);
        }
        return pushStatsRepository.findByDateRange(startDate, endDate);
    }

    public List<PushStats> getDailyStats(String date) {
        return pushStatsRepository.findByDate(date);
    }

    public void resetStats() {
        flushLock.lock();
        try {
            dailyStatsMap.clear();
            pushStatsRepository.deleteAll();
        } finally {
            flushLock.unlock();
        }
    }

    private void flushToDatabase() {
        String date = currentDate;
        flushWithDate(date);
    }

    private void flushWithDate(String date) {
        if (dailyStatsMap.isEmpty()) return;

        Map<String, PlatformStats> snapshot;
        flushLock.lock();
        try {
            snapshot = new HashMap<>(dailyStatsMap);
        } finally {
            flushLock.unlock();
        }

        List<PushStats> toSave = new ArrayList<>();
        for (Map.Entry<String, PlatformStats> entry : snapshot.entrySet()) {
            try {
                PushStats ps = pushStatsRepository.findById(
                        new cn.wildfirechat.push.admin.entity.PushStatsId(entry.getKey(), date)
                ).orElse(new PushStats());
                ps.setPlatform(entry.getKey());
                ps.setStatDate(date);
                ps.setTotalCount(entry.getValue().getTotalCount());
                ps.setSuccessCount(entry.getValue().getSuccessCount());
                ps.setFailCount(entry.getValue().getFailCount());
                toSave.add(ps);
            } catch (Exception e) {
                LOG.error("Failed to prepare stats for {} on {}", entry.getKey(), date, e);
            }
        }
        if (!toSave.isEmpty()) {
            try {
                pushStatsRepository.saveAll(toSave);
            } catch (Exception e) {
                LOG.error("Failed to batch save stats for {}", date, e);
            }
        }
    }

    public static class PlatformStats {
        private final AtomicLong totalCount = new AtomicLong(0);
        private final AtomicLong successCount = new AtomicLong(0);
        private final AtomicLong failCount = new AtomicLong(0);

        public long getTotalCount() {
            return totalCount.get();
        }

        public long getSuccessCount() {
            return successCount.get();
        }

        public long getFailCount() {
            return failCount.get();
        }
    }
}
