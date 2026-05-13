package com.yyyouth.service.user.userstats.impl;

import com.yyyouth.common.constants.RedisConstant;
import com.yyyouth.model.dto.userstats.UserActivityEvent;
import com.yyyouth.model.enums.UserActivityType;
import com.yyyouth.service.mapper.user.UserStatsDailyMapper;
import com.yyyouth.service.mapper.user.UserStatsOverviewMapper;
import com.yyyouth.service.mapper.user.UserTagAffinityMapper;
import com.yyyouth.service.config.redis.RedisCache;
import com.yyyouth.service.user.userstats.UserStatsAggregateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户贡献统计聚合服务实现 - user-15 用户主页。
 *
 * 实现要点：
 *  1. 幂等：基于 Redis SETNX user:stats:event:{eventId} TTL 24h，去重时长足够覆盖 DLQ 兜底窗口；
 *  2. 单日上限：消费前查 t_user_stats_daily 当日 contribution，若 >= 50 则本事件 contribution 记 0 仅累加 count；
 *  3. 三表 upsert 在事务内串联，失败抛出由消费端 NACK 进 DLQ；
 *  4. 维度偏好（dim_type=1 分类）按权重 weight = unit * 单次行为权重 落库，tag/tech 暂留扩展点；
 *  5. 缓存失效：upsert 后按 pattern 删除该用户全部 dashboard key（SCAN + UNLINK，不阻塞）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatsAggregateServiceImpl implements UserStatsAggregateService {

    /** 单日贡献分上限 */
    private static final BigDecimal DAILY_CONTRIBUTION_CAP = new BigDecimal("50.0");

    /** 幂等去重 TTL（小时） */
    private static final long IDEMPOTENT_TTL_HOURS = 24L;

    /** 维度类型 - 分类 */
    private static final int DIM_CATEGORY = 1;

    /** 维度类型 - 标签 */
    private static final int DIM_TAG = 2;

    /** 维度类型 - 技术栈 */
    private static final int DIM_TECH_STACK = 3;

    private final UserStatsDailyMapper userStatsDailyMapper;

    private final UserStatsOverviewMapper userStatsOverviewMapper;

    private final UserTagAffinityMapper userTagAffinityMapper;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisCache redisCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean apply(UserActivityEvent event) {
        if (event == null || event.getUserId() == null || event.getUserId() <= 0) {
            log.warn("[user-stats] 事件参数非法，丢弃：{}", event);
            return false;
        }

        UserActivityType type = UserActivityType.fromCode(event.getType());
        if (type == null) {
            log.warn("[user-stats] 未知事件类型 type={}，丢弃 eventId={}", event.getType(), event.getEventId());
            return false;
        }

        if (!acquireIdempotentLock(event.getEventId())) {
            log.info("[user-stats] 幂等命中，跳过 eventId={}, userId={}, type={}",
                    event.getEventId(), event.getUserId(), event.getType());
            return false;
        }

        LocalDate statDate = resolveStatDate(event);
        BigDecimal contributionDelta = resolveContributionDelta(event.getUserId(), statDate, type);

        // 1. t_user_stats_daily 增量 upsert
        userStatsDailyMapper.upsertIncr(
                event.getUserId(),
                statDate,
                delta(type, UserActivityType.SUBMIT),
                delta(type, UserActivityType.COMMENT),
                delta(type, UserActivityType.COLLECT),
                delta(type, UserActivityType.LIKE),
                delta(type, UserActivityType.SCORE),
                delta(type, UserActivityType.BROWSE),
                contributionDelta);

        // 2. t_user_stats_overview 累计列累加
        userStatsOverviewMapper.upsertIncr(
                event.getUserId(),
                delta(type, UserActivityType.SUBMIT),
                delta(type, UserActivityType.COMMENT),
                delta(type, UserActivityType.COLLECT),
                delta(type, UserActivityType.LIKE),
                delta(type, UserActivityType.SCORE),
                delta(type, UserActivityType.BROWSE),
                contributionDelta,
                statDate);

        // 3. t_user_tag_affinity 维度偏好（仅当事件携带 categoryId 时累加分类维度）
        upsertTagAffinity(event, type);

        // 4. 失效该用户的 dashboard 缓存（SCAN + UNLINK）
        evictDashboardCache(event.getUserId());

        log.debug("[user-stats] 事件聚合完成 eventId={}, userId={}, type={}, statDate={}, contributionDelta={}",
                event.getEventId(), event.getUserId(), type.getCode(), statDate, contributionDelta);
        return true;
    }

    /**
     * 解析事件日期（按服务端时区取本地日）
     */
    private LocalDate resolveStatDate(UserActivityEvent event) {
        if (event.getTs() == null) {
            return LocalDate.now();
        }
        return event.getTs().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 计算本次事件的贡献分增量（裁剪到单日上限）
     */
    private BigDecimal resolveContributionDelta(Long userId, LocalDate statDate, UserActivityType type) {
        BigDecimal already = userStatsDailyMapper.selectContribution(userId, statDate);
        if (already == null) {
            already = BigDecimal.ZERO;
        }
        if (already.compareTo(DAILY_CONTRIBUTION_CAP) >= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal candidate = type.getWeight();
        BigDecimal remaining = DAILY_CONTRIBUTION_CAP.subtract(already);
        return candidate.compareTo(remaining) <= 0 ? candidate : remaining;
    }

    /**
     * 类型匹配返回 1，否则 0（用于增量 upsert 的列计数）
     */
    private int delta(UserActivityType actual, UserActivityType target) {
        return actual == target ? 1 : 0;
    }

    /**
     * upsert 维度偏好（M2 阶段仅处理 categoryId；tag/tech 维度待 M3 看板需要时补充）
     */
    private void upsertTagAffinity(UserActivityEvent event, UserActivityType type) {
        if (event.getCategoryId() == null || event.getCategoryId() <= 0) {
            return;
        }
        // 维度权重：submit=5, collect=3, like=1, browse=0.3, 其他类型不更新偏好
        BigDecimal weight = switch (type) {
            case SUBMIT -> new BigDecimal("5.0");
            case COLLECT -> new BigDecimal("3.0");
            case LIKE -> BigDecimal.ONE;
            case BROWSE -> new BigDecimal("0.3");
            default -> null;
        };
        if (weight == null) {
            return;
        }
        // dimName 在 M3 看板服务侧通过 categoryMapper join 补全；这里写空串避免 NOT NULL 约束失败
        userTagAffinityMapper.upsertWeight(
                event.getUserId(),
                DIM_CATEGORY,
                event.getCategoryId(),
                "",
                weight);
    }

    /**
     * 幂等去重：SETNX user:stats:event:{eventId} TTL 24h。eventId 为空时不去重（视为放行）。
     */
    private boolean acquireIdempotentLock(String eventId) {
        if (!StringUtils.hasText(eventId)) {
            return true;
        }
        String key = RedisConstant.USER_STATS_EVENT_IDEMPOTENT_PREFIX + eventId;
        Boolean ok = stringRedisTemplate.opsForValue()
                .setIfAbsent(key, "1", IDEMPOTENT_TTL_HOURS, TimeUnit.HOURS);
        return Boolean.TRUE.equals(ok);
    }

    /**
     * 失效该用户全部 dashboard 缓存 key（user:profile:dashboard:{userId}:*）。
     * 委托给 {@link RedisCache#evictByPattern(String)}（内部 SCAN + UNLINK）。
     */
    private void evictDashboardCache(Long userId) {
        redisCache.evictByPattern(RedisConstant.USER_PROFILE_DASHBOARD_PREFIX + userId + ":*");
    }
}
