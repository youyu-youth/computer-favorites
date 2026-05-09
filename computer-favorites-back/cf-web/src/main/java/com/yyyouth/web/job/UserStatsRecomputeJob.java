package com.yyyouth.web.job;

import com.yyyouth.service.user.userstats.UserStatsRecomputeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户主页 T+1 重算定时任务（user-15 M4 / spec T35）。
 *
 * 调度策略：
 *  - cron：每日 02:00（凌晨低峰）
 *  - 多实例部署下用 Redisson 分布式锁互斥；watchdog 自动续期防 GC 暂停误释放
 *  - 锁 leaseTime 设 -1 启用 watchdog；waitTime 设 0 抢不到立即放弃避免雪崩
 *
 * 单次任务步骤：
 *  1. recomputeDaily(yesterday)
 *  2. recomputeOverview()（基于 daily 重算 level/streak/rank_percent）
 *  3. recomputeTagAffinity(90)（M4 仅占位）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatsRecomputeJob {

    private static final String LOCK_KEY = "lock:job:user-stats-recompute";
    private static final long WAIT_TIME_MS = 0L;

    private final UserStatsRecomputeService recomputeService;
    private final RedissonClient redissonClient;

    @Scheduled(cron = "${cf.job.user-stats-recompute.cron:0 0 2 * * ?}", zone = "Asia/Shanghai")
    public void run() {
        execute(LocalDate.now().minusDays(1));
    }

    /**
     * 抽出可由 Controller 手动触发的纯执行入口（便于灰度回填 / 单测）。
     */
    public boolean execute(LocalDate target) {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        boolean acquired = false;
        long start = System.currentTimeMillis();
        try {
            acquired = lock.tryLock(WAIT_TIME_MS, -1, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.warn("[user-stats-recompute] skip: lock held by another node");
                return false;
            }
            log.info("[user-stats-recompute] start target={}", target);
            int dailyAffected = recomputeService.recomputeDaily(target);
            int overviewAffected = recomputeService.recomputeOverview();
            int tagAffected = recomputeService.recomputeTagAffinity(90);
            log.info("[user-stats-recompute] done target={} daily={} overview={} tag={} cost={}ms",
                    target, dailyAffected, overviewAffected, tagAffected,
                    Duration.ofMillis(System.currentTimeMillis() - start).toMillis());
            return true;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.error("[user-stats-recompute] interrupted", ie);
            return false;
        } catch (Exception ex) {
            log.error("[user-stats-recompute] fail target={}", target, ex);
            return false;
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                try {
                    lock.unlock();
                } catch (Exception ex) {
                    log.warn("[user-stats-recompute] unlock fail", ex);
                }
            }
        }
    }
}
