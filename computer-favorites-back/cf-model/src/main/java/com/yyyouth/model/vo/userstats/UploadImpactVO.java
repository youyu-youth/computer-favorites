package com.yyyouth.model.vo.userstats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author yyyouth zg
 * @date 2026-05-09
 *
 * 上传网站影响力 VO（user-15 扩展）
 *
 * 口径：聚合 <strong>当前用户已审核通过的全部上传网站</strong>，统计「<strong>他人</strong>对这些网站」的
 * 浏览/点赞/收藏/评论/评分行为；与 {@link DashboardOverviewVO} 的「自身行为统计」严格区分。
 *
 * 范围（range）：
 *  - {@code 7d / 30d / 90d}：返回 daily 时间序列 + 当期/上期 环比 delta；
 *  - {@code all}：仅返回累计数（rangeCounts=totals），points 为空，delta 全部为 null。
 *
 * 对应 Redis key：{@code user:profile:dashboard:{uid}:upload-impact:{range}}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadImpactVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 范围：7d / 30d / 90d / all */
    private String range;

    /** 已审核通过、未删除的上传网站数（基数提示，前端展示「基于 N 个上传网站」） */
    private Integer websiteCount;

    /** 全时累计（来自 t_website 计数器列 SUM，与 range 无关） */
    private MetricGroupVO totals;

    /** 当前 range 内事件数（all 时 = totals 同值，前端据此渲染柱图最大柱） */
    private MetricGroupVO rangeCounts;

    /** 本期 vs 上一同长期间 环比百分比；range=all 时全部为 null */
    private DeltaGroupVO delta;

    /** 7d/30d/90d 时为按日序列（升序、缺失日补 0）；all 时为空 list */
    private List<DailyPointVO> points;

    /**
     * 五指标计数组（单位：次）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricGroupVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 浏览次数（来自 t_browse_history） */
        private Long browse;

        /** 点赞次数（来自 t_website_like） */
        private Long like;

        /** 收藏次数（来自 t_user_collect） */
        private Long collect;

        /** 评论次数（来自 t_comment，已过滤 deleted=0 AND status=1） */
        private Long comment;

        /** 评分次数（来自 t_website_score） */
        private Long score;
    }

    /**
     * 五指标环比 delta 组（单位：百分比，保留 1 位小数 HALF_UP）
     *
     * 计算规则：
     *  - prev=0 && curr=0  → 0
     *  - prev=0 && curr>0  → 100
     *  - 其他               → (curr - prev) * 100 / prev
     *  - range=all 时       → 全部字段为 null
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeltaGroupVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /** 浏览环比百分比 */
        private BigDecimal browse;

        /** 点赞环比百分比 */
        private BigDecimal like;

        /** 收藏环比百分比 */
        private BigDecimal collect;

        /** 评论环比百分比 */
        private BigDecimal comment;

        /** 评分环比百分比 */
        private BigDecimal score;
    }

    /**
     * 日序列单点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPointVO implements Serializable {

        private static final long serialVersionUID = 1L;

        /** ISO 日期 yyyy-MM-dd */
        private String date;

        /** 当日浏览次数 */
        private Long browse;

        /** 当日点赞次数 */
        private Long like;

        /** 当日收藏次数 */
        private Long collect;

        /** 当日评论次数 */
        private Long comment;

        /** 当日评分次数 */
        private Long score;
    }
}
