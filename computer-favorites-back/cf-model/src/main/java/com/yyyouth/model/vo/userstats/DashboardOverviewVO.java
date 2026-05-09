package com.yyyouth.model.vo.userstats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户主页贡献概览 VO（user-15）
 * 对应 Redis key：user:profile:dashboard:{uid}:overview
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 累计投稿数 */
    private Integer totalSubmit;

    /** 累计评论数 */
    private Integer totalComment;

    /** 累计收藏数 */
    private Integer totalCollect;

    /** 累计点赞数 */
    private Integer totalLike;

    /** 累计评分数 */
    private Integer totalScore;

    /** 累计浏览数 */
    private Integer totalBrowse;

    /** 累计贡献分（带 1 位小数） */
    private BigDecimal totalContribution;

    /** 等级编码：S / A / B / C */
    private String levelCode;

    /** 当前连续活跃天数 */
    private Integer streakDays;

    /** 历史最长连续活跃天数 */
    private Integer maxStreakDays;

    /** 排行百分位（0-100，越大越靠前） */
    private BigDecimal rankPercent;

    /** 最近活跃日期 */
    private LocalDate lastActiveDate;
}
