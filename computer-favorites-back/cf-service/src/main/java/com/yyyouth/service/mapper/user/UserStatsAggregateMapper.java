package com.yyyouth.service.mapper.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户贡献 T+1 重算专用只读 Mapper（user-15 M4）。
 *
 * 数据源覆盖：t_website（投稿审核通过）、t_comment、t_user_collect、t_website_like、
 * t_website_score、t_browse_history（30min 去重后可视为日活粒度）、t_user_stats_daily（用作 overview 累计）。
 *
 * 注意：所有跨用户的 SQL 都不要 LIMIT，调用方自行控制批量。
 */
@Mapper
public interface UserStatsAggregateMapper {

    /**
     * 找出指定日期内有活动的用户 ID（union 六张源表，DISTINCT 去重）。
     */
    @Select("SELECT DISTINCT user_id FROM ("
            + "  SELECT submitter_id AS user_id FROM t_website "
            + "    WHERE submitter_id IS NOT NULL AND audit_status = 1 AND deleted = 0 "
            + "      AND DATE(create_time) = #{date} "
            + "  UNION ALL "
            + "  SELECT user_id FROM t_comment WHERE deleted = 0 AND status = 1 "
            + "      AND DATE(create_time) = #{date} "
            + "  UNION ALL "
            + "  SELECT user_id FROM t_user_collect WHERE DATE(create_time) = #{date} "
            + "  UNION ALL "
            + "  SELECT user_id FROM t_website_like WHERE DATE(create_time) = #{date} "
            + "  UNION ALL "
            + "  SELECT user_id FROM t_website_score WHERE DATE(create_time) = #{date} "
            + "  UNION ALL "
            + "  SELECT user_id FROM t_browse_history WHERE DATE(create_time) = #{date} "
            + ") t WHERE user_id IS NOT NULL")
    List<Long> findActiveUserIds(@Param("date") LocalDate date);

    /**
     * 单用户单日的 6 个计数（去重后的真值），返回 Map：
     * submit_count / comment_count / collect_count / like_count / score_count / browse_count
     * 调用方根据这些值再按权重 + 单日 50 上限计算 contribution。
     */
    @Select("SELECT "
            + "  (SELECT COUNT(*) FROM t_website "
            + "    WHERE submitter_id = #{userId} AND audit_status = 1 AND deleted = 0 "
            + "      AND DATE(create_time) = #{date}) AS submit_count, "
            + "  (SELECT COUNT(*) FROM t_comment "
            + "    WHERE user_id = #{userId} AND deleted = 0 AND status = 1 "
            + "      AND DATE(create_time) = #{date}) AS comment_count, "
            + "  (SELECT COUNT(*) FROM t_user_collect "
            + "    WHERE user_id = #{userId} AND DATE(create_time) = #{date}) AS collect_count, "
            + "  (SELECT COUNT(*) FROM t_website_like "
            + "    WHERE user_id = #{userId} AND DATE(create_time) = #{date}) AS like_count, "
            + "  (SELECT COUNT(*) FROM t_website_score "
            + "    WHERE user_id = #{userId} AND DATE(create_time) = #{date}) AS score_count, "
            + "  (SELECT COUNT(*) FROM t_browse_history "
            + "    WHERE user_id = #{userId} AND DATE(create_time) = #{date}) AS browse_count")
    Map<String, Object> aggregateDailyCounts(@Param("userId") Long userId,
                                             @Param("date") LocalDate date);

    /**
     * 列出 t_user_stats_overview 中所有用户 ID（含已被 MQ 写入但 daily 可能缺失的用户）。
     */
    @Select("SELECT user_id FROM t_user_stats_overview")
    List<Long> findAllOverviewUserIds();

    /**
     * 单用户从 daily 表汇总后的累计列（避免读 overview 自身造成读写循环）。
     */
    @Select("SELECT "
            + "  IFNULL(SUM(submit_count), 0)   AS total_submit, "
            + "  IFNULL(SUM(comment_count), 0)  AS total_comment, "
            + "  IFNULL(SUM(collect_count), 0)  AS total_collect, "
            + "  IFNULL(SUM(like_count), 0)     AS total_like, "
            + "  IFNULL(SUM(score_count), 0)    AS total_score, "
            + "  IFNULL(SUM(browse_count), 0)   AS total_browse, "
            + "  IFNULL(SUM(contribution), 0)   AS total_contribution, "
            + "  MAX(stat_date)                 AS last_active_date "
            + "FROM t_user_stats_daily WHERE user_id = #{userId}")
    Map<String, Object> aggregateOverviewFromDaily(@Param("userId") Long userId);

    /**
     * 倒序拉取一段日期内的（stat_date, contribution）行，用于本地计算 streak。
     * 仅返回 contribution > 0 的天，调用方比较日期连续性。
     */
    @Select("SELECT stat_date, contribution FROM t_user_stats_daily "
            + "WHERE user_id = #{userId} AND contribution > 0 "
            + "  AND stat_date >= #{lowerBound} "
            + "ORDER BY stat_date DESC")
    List<Map<String, Object>> listActiveDaysDesc(@Param("userId") Long userId,
                                                 @Param("lowerBound") LocalDate lowerBound);

    /**
     * 历史最长 streak：从用户首次活跃日开始全量扫，避免 lowerBound 截断。
     * 返回升序，调用方逐行扫描计算最大连续段。
     */
    @Select("SELECT stat_date FROM t_user_stats_daily "
            + "WHERE user_id = #{userId} AND contribution > 0 "
            + "ORDER BY stat_date ASC")
    List<LocalDate> listAllActiveDaysAsc(@Param("userId") Long userId);

    /**
     * 全表用户的 total_contribution 从大到小拉取（用于排名百分位）。
     * 数据量随用户基数线性增长；M4 阶段假设量级 < 10w，可一次拉完。
     * 上线前若超此规模需改为分桶 + 二分。
     */
    @Select("SELECT user_id, total_contribution FROM t_user_stats_overview "
            + "ORDER BY total_contribution DESC")
    List<Map<String, Object>> listAllContributionDesc();

    /**
     * 严格大于 currentScore 的用户数量（用于 rank_percent 计算）。
     */
    @Select("SELECT COUNT(*) FROM t_user_stats_overview "
            + "WHERE total_contribution > #{score}")
    long countContributionGreaterThan(@Param("score") BigDecimal score);

    /**
     * 总用户数（rank_percent 分母）。
     */
    @Select("SELECT COUNT(*) FROM t_user_stats_overview")
    long countAllUsers();
}
