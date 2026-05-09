package com.yyyouth.model.pojo.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户维度贡献快照 - user-15 用户主页
 * 主键即 user_id；累计列由 MQ 增量维护，level/streak/rank_percent 由 T+1 任务重算。
 */
@Data
@TableName("t_user_stats_overview")
public class UserStatsOverview {

    /** 用户 ID（主键，非自增） */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;

    @TableField("total_submit")
    private Integer totalSubmit;

    @TableField("total_comment")
    private Integer totalComment;

    @TableField("total_collect")
    private Integer totalCollect;

    @TableField("total_like")
    private Integer totalLike;

    @TableField("total_score")
    private Integer totalScore;

    @TableField("total_browse")
    private Integer totalBrowse;

    @TableField("total_contribution")
    private BigDecimal totalContribution;

    /** 等级 S/A+/A/B/C */
    @TableField("level_code")
    private String levelCode;

    /** 当前连续活跃天数 */
    @TableField("streak_days")
    private Integer streakDays;

    /** 历史最长连续活跃天数 */
    @TableField("max_streak_days")
    private Integer maxStreakDays;

    /** 全站贡献百分位 0-100 */
    @TableField("rank_percent")
    private BigDecimal rankPercent;

    @TableField("last_active_date")
    private LocalDate lastActiveDate;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
