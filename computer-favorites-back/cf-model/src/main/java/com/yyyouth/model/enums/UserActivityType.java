package com.yyyouth.model.enums;

import lombok.Getter;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户行为事件类型 - user-15 用户主页贡献统计
 * 配套 t_user_stats_daily 列名 + 贡献分权重；单日上限 50 分由消费端校验。
 */
@Getter
public enum UserActivityType {

    /** 投稿审核通过 */
    SUBMIT("submit", "submit_count", new java.math.BigDecimal("5.0")),

    /** 评论 */
    COMMENT("comment", "comment_count", new java.math.BigDecimal("2.0")),

    /** 收藏网站 */
    COLLECT("collect", "collect_count", new java.math.BigDecimal("1.0")),

    /** 网站点赞 */
    LIKE("like", "like_count", new java.math.BigDecimal("0.5")),

    /** 网站评分 */
    SCORE("score", "score_count", new java.math.BigDecimal("1.0")),

    /** 浏览历史（30min 去重通过后） */
    BROWSE("browse", "browse_count", new java.math.BigDecimal("0.1"));

    /** 类型代码（事件载荷字段） */
    private final String code;

    /** 对应 t_user_stats_daily / t_user_stats_overview 计数列名 */
    private final String dailyColumn;

    /** 单次行为贡献分权重 */
    private final java.math.BigDecimal weight;

    UserActivityType(String code, String dailyColumn, java.math.BigDecimal weight) {
        this.code = code;
        this.dailyColumn = dailyColumn;
        this.weight = weight;
    }

    public static UserActivityType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (UserActivityType t : values()) {
            if (t.code.equalsIgnoreCase(code)) {
                return t;
            }
        }
        return null;
    }
}
