package com.yyyouth.service.user.userstats.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yyyouth.model.enums.UserActivityType;
import com.yyyouth.model.pojo.user.UserStatsOverview;
import com.yyyouth.service.mapper.user.UserStatsAggregateMapper;
import com.yyyouth.service.mapper.user.UserStatsDailyMapper;
import com.yyyouth.service.mapper.user.UserStatsOverviewMapper;
import com.yyyouth.service.config.redis.RedisCache;
import com.yyyouth.service.user.userstats.UserStatsRecomputeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * T+1 重算实现（user-15 M4）。
 *
 * 关键策略：
 *  1. recomputeDaily：从源 6 表聚合昨日真值，按权重 + 50 分上限重算 contribution，覆写 daily 行（单事务幂等）。
 *  2. recomputeOverview：基于 daily 聚合累计列，重算 level/streak/rank_percent，UPDATE overview 行。
 *  3. recomputeTagAffinity：M4 暂仅打日志（spec 中标记为可后置）。
 *  4. 完成后批量 evict `user:profile:dashboard:{userId}:*` 缓存，触发回源最新数据。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatsRecomputeServiceImpl implements UserStatsRecomputeService {

    private static final BigDecimal DAILY_CONTRIBUTION_CAP = new BigDecimal("50.0");

    /** 等级阈值（基于 total_contribution，含等号边界更高级） */
    private static final BigDecimal LEVEL_S = new BigDecimal("5000");
    private static final BigDecimal LEVEL_A_PLUS = new BigDecimal("2000");
    private static final BigDecimal LEVEL_A = new BigDecimal("800");
    private static final BigDecimal LEVEL_B = new BigDecimal("200");

    /** rank_percent 缓存键前缀（看板侧使用同一前缀；这里仅为 evict） */
    private static final String DASHBOARD_PATTERN = "user:profile:dashboard:%d:*";

    private final UserStatsAggregateMapper aggregateMapper;
    private final UserStatsDailyMapper dailyMapper;
    private final UserStatsOverviewMapper overviewMapper;
    private final RedisCache redisCache;

    // ===================== T36-a recomputeDaily =====================

    @Override
    @Transactional
    public int recomputeDaily(LocalDate date) {
        Objects.requireNonNull(date, "date");
        log.info("[recompute-daily] start date={}", date);

        List<Long> activeIds = aggregateMapper.findActiveUserIds(date);
        log.info("[recompute-daily] activeUsers={}", activeIds.size());
        if (activeIds.isEmpty()) {
            return 0;
        }

        int affected = 0;
        for (Long userId : activeIds) {
            try {
                Map<String, Object> counts = aggregateMapper.aggregateDailyCounts(userId, date);
                int submit = readInt(counts, "submit_count");
                int comment = readInt(counts, "comment_count");
                int collect = readInt(counts, "collect_count");
                int like = readInt(counts, "like_count");
                int score = readInt(counts, "score_count");
                int browse = readInt(counts, "browse_count");

                BigDecimal contribution = computeContribution(submit, comment, collect, like, score, browse);

                // 先 DELETE 当日旧行（避免 ON DUPLICATE KEY 累加导致翻倍），再 INSERT 真值
                dailyMapper.delete(new UpdateWrapper<com.yyyouth.model.pojo.user.UserStatsDaily>()
                        .eq("user_id", userId)
                        .eq("stat_date", date));
                dailyMapper.upsertIncr(userId, date, submit, comment, collect, like, score, browse, contribution);
                affected++;
            } catch (Exception ex) {
                log.error("[recompute-daily] userId={} date={} fail", userId, date, ex);
            }
        }
        log.info("[recompute-daily] done date={} affected={}", date, affected);
        return affected;
    }

    private BigDecimal computeContribution(int submit, int comment, int collect, int like, int score, int browse) {
        BigDecimal sum = BigDecimal.ZERO
                .add(weight(UserActivityType.SUBMIT).multiply(BigDecimal.valueOf(submit)))
                .add(weight(UserActivityType.COMMENT).multiply(BigDecimal.valueOf(comment)))
                .add(weight(UserActivityType.COLLECT).multiply(BigDecimal.valueOf(collect)))
                .add(weight(UserActivityType.LIKE).multiply(BigDecimal.valueOf(like)))
                .add(weight(UserActivityType.SCORE).multiply(BigDecimal.valueOf(score)))
                .add(weight(UserActivityType.BROWSE).multiply(BigDecimal.valueOf(browse)));
        // 单日上限裁剪
        return sum.compareTo(DAILY_CONTRIBUTION_CAP) > 0 ? DAILY_CONTRIBUTION_CAP : sum;
    }

    private BigDecimal weight(UserActivityType type) {
        return type.getWeight();
    }

    // ===================== T36-b recomputeOverview =====================

    @Override
    @Transactional
    public int recomputeOverview() {
        log.info("[recompute-overview] start");

        // 全部需要刷新的用户：union(t_user_stats_overview, t_user_stats_daily 中存在的 user_id)
        Set<Long> userIds = new HashSet<>(aggregateMapper.findAllOverviewUserIds());

        // 一次性预拉取全表 contribution 用于排名（避免 N 次 SQL）
        List<Map<String, Object>> rankSnapshot = aggregateMapper.listAllContributionDesc();
        Map<Long, Long> rankIndex = buildRankIndex(rankSnapshot);
        long total = rankSnapshot.size();

        int affected = 0;
        for (Long userId : userIds) {
            try {
                if (recomputeOneOverview(userId, rankIndex, total)) {
                    affected++;
                }
            } catch (Exception ex) {
                log.error("[recompute-overview] userId={} fail", userId, ex);
            }
        }
        log.info("[recompute-overview] done affected={}", affected);
        return affected;
    }

    /**
     * @return true 表示成功更新一行
     */
    private boolean recomputeOneOverview(Long userId, Map<Long, Long> rankIndex, long total) {
        Map<String, Object> agg = aggregateMapper.aggregateOverviewFromDaily(userId);
        int totalSubmit = readInt(agg, "total_submit");
        int totalComment = readInt(agg, "total_comment");
        int totalCollect = readInt(agg, "total_collect");
        int totalLike = readInt(agg, "total_like");
        int totalScore = readInt(agg, "total_score");
        int totalBrowse = readInt(agg, "total_browse");
        BigDecimal totalContribution = readBig(agg, "total_contribution");
        LocalDate lastActive = readDate(agg, "last_active_date");

        String level = computeLevel(totalContribution);
        int streak = computeStreak(userId, lastActive);
        int maxStreak = computeMaxStreak(userId);
        BigDecimal rankPercent = computeRankPercent(rankIndex, total, userId);

        UserStatsOverview po = new UserStatsOverview();
        po.setUserId(userId);
        po.setTotalSubmit(totalSubmit);
        po.setTotalComment(totalComment);
        po.setTotalCollect(totalCollect);
        po.setTotalLike(totalLike);
        po.setTotalScore(totalScore);
        po.setTotalBrowse(totalBrowse);
        po.setTotalContribution(totalContribution);
        po.setLevelCode(level);
        po.setStreakDays(streak);
        po.setMaxStreakDays(maxStreak);
        po.setRankPercent(rankPercent);
        po.setLastActiveDate(lastActive);

        int rows = overviewMapper.updateById(po);
        if (rows == 0) {
            // overview 未必有行（用户从未触发 MQ 时为空），insert 一条
            rows = overviewMapper.insert(po);
        }
        // 失效该用户看板缓存
        try {
            redisCache.evictByPattern(String.format(DASHBOARD_PATTERN, userId));
        } catch (Exception ex) {
            log.warn("[recompute-overview] evict cache fail userId={} : {}", userId, ex.getMessage());
        }
        return rows > 0;
    }

    private String computeLevel(BigDecimal totalContribution) {
        if (totalContribution == null) return "C";
        if (totalContribution.compareTo(LEVEL_S) >= 0) return "S";
        if (totalContribution.compareTo(LEVEL_A_PLUS) >= 0) return "A+";
        if (totalContribution.compareTo(LEVEL_A) >= 0) return "A";
        if (totalContribution.compareTo(LEVEL_B) >= 0) return "B";
        return "C";
    }

    /**
     * 当前连续活跃天数：从今天回溯，直到出现非活跃日则中断；
     * 若用户今日未活跃但昨日活跃，则从昨日开始计；若昨日也无活跃则 streak=0。
     */
    private int computeStreak(Long userId, LocalDate lastActive) {
        if (lastActive == null) return 0;
        LocalDate today = LocalDate.now();
        // streak 起点：min(lastActive, today)
        LocalDate end = lastActive.isAfter(today) ? today : lastActive;
        // 仅当 lastActive >= today-1 才可能 streak >= 1（断档定义）
        if (end.isBefore(today.minusDays(1))) {
            return 0;
        }
        // 拉取最近 400 天内的活跃日，倒序
        LocalDate lower = end.minusDays(400);
        List<Map<String, Object>> rows = aggregateMapper.listActiveDaysDesc(userId, lower);
        if (rows.isEmpty()) return 0;
        int streak = 0;
        LocalDate cursor = end;
        for (Map<String, Object> row : rows) {
            LocalDate d = readDate(row, "stat_date");
            if (d == null) continue;
            if (d.equals(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            } else if (d.isBefore(cursor)) {
                break;
            }
        }
        return streak;
    }

    /**
     * 历史最长连续活跃天数：升序遍历所有活跃日，相邻日差 1 则累加，否则重置。
     */
    private int computeMaxStreak(Long userId) {
        List<LocalDate> days = aggregateMapper.listAllActiveDaysAsc(userId);
        if (days.isEmpty()) return 0;
        int max = 1, cur = 1;
        for (int i = 1; i < days.size(); i++) {
            LocalDate prev = days.get(i - 1);
            LocalDate now = days.get(i);
            if (prev.plusDays(1).equals(now)) {
                cur++;
                max = Math.max(max, cur);
            } else {
                cur = 1;
            }
        }
        return max;
    }

    /**
     * rank_percent = (高于自己的人数) / 总人数 * 100，越小越靠前；保留 2 位小数。
     * 参考 spec：rank_percent 表示"打败了多少百分比"，所以应是 (totalUsers - higherCount) / totalUsers * 100。
     * 这里采用"打败比例"语义（前端直接展示）。
     */
    private BigDecimal computeRankPercent(Map<Long, Long> rankIndex, long total, Long userId) {
        if (total == 0) return BigDecimal.ZERO;
        Long higher = rankIndex.getOrDefault(userId, total);
        // higher = 严格高于自己的用户数；defeated = total - higher - 1（排除自己），最低 0
        long defeated = Math.max(0, total - higher - 1);
        return BigDecimal.valueOf(defeated)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    private Map<Long, Long> buildRankIndex(List<Map<String, Object>> snapshot) {
        Map<Long, Long> idx = new java.util.HashMap<>(snapshot.size() * 2);
        BigDecimal prev = null;
        long sameCount = 0;
        long position = 0;
        for (int i = 0; i < snapshot.size(); i++) {
            Map<String, Object> row = snapshot.get(i);
            Long uid = readLong(row, "user_id");
            BigDecimal contrib = readBig(row, "total_contribution");
            if (i == 0 || (prev != null && contrib.compareTo(prev) != 0)) {
                position = i;
                sameCount = 0;
            } else {
                sameCount++;
            }
            // 同分用户共享同一 higher 数（dense ranking 的反向：同分=同 higher 数 = position）
            idx.put(uid, position);
            prev = contrib;
        }
        return idx;
    }

    // ===================== T36-c recomputeTagAffinity =====================

    @Override
    public int recomputeTagAffinity(int days) {
        // M4 阶段先打日志占位；M5 再补 t_user_tag_affinity 滑动窗口重算
        log.warn("[recompute-tag-affinity] not implemented yet, days={}", days);
        return 0;
    }

    // ===================== helpers =====================

    private static int readInt(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(v.toString());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static long readLong(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(v.toString());
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private static BigDecimal readBig(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal b) return b;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(v.toString());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private static LocalDate readDate(Map<String, Object> map, String key) {
        Object v = map == null ? null : map.get(key);
        if (v == null) return null;
        if (v instanceof LocalDate d) return d;
        if (v instanceof java.sql.Date sd) return sd.toLocalDate();
        if (v instanceof java.util.Date ud) {
            return ud.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        try {
            return LocalDate.parse(v.toString());
        } catch (Exception ex) {
            return null;
        }
    }
}
