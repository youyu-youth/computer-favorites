package com.yyyouth.service.user.userstats;

import java.time.LocalDate;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户主页 T+1 重算服务（user-15 M4）。
 *
 * 与 MQ 增量链路解耦：MQ 处理实时增量，T+1 全量校准（防止漏消费 / 异常 / 后端权重变更后的漂移）。
 *
 * 调度入口：cf-web 的 UserStatsRecomputeJob（@Scheduled cron 每日 02:00）。
 */
public interface UserStatsRecomputeService {

    /**
     * 重算指定日期的所有活跃用户 daily 行（基于源表 union 聚合，覆写 t_user_stats_daily）。
     *
     * @param date 通常为 LocalDate.now().minusDays(1)
     * @return 受影响用户数
     */
    int recomputeDaily(LocalDate date);

    /**
     * 重算所有用户 overview：
     *  - 累计列 = SUM(daily 各列)
     *  - level_code（S / A+ / A / B / C，阈值见 spec §3.2）
     *  - streak_days / max_streak_days
     *  - rank_percent（全表百分位）
     *  - last_active_date = MAX(daily.stat_date)
     *
     * 直接 UPDATE 现有 overview 行；若 daily 完全为空但 overview 存在，将累计列归零并 streak/level 重置。
     *
     * @return 受影响用户数
     */
    int recomputeOverview();

    /**
     * 重算用户标签 / 分类 / 技术栈偏好（滑动窗口）。
     *
     * @param days 滑动窗口天数（spec 推荐 90）
     * @return 受影响用户数
     */
    int recomputeTagAffinity(int days);
}
