package com.yyyouth.common.constants;

/**
 * @author yyyouth zg
 * @date 2026-05-06
 *
 * rabbitmq 通知常量
 */
public final class RabbitConstants {

    private RabbitConstants() {
    }

    public static final String EXCHANGE_NAME = "exchange.sssp";

    public static final String QUEUE_NAME_POST = "queue.sssp.post";

    public static final String ROUTING_KEY_POST = "routing.sssp.post";

    /** 审核结果通知 */
    public static final String ROUTING_KEY_AUDIT = "notify.audit";

    /** 收藏提醒 */
    public static final String ROUTING_KEY_COLLECT = "notify.collect";

    /** 举报反馈 */
    public static final String ROUTING_KEY_REPORT = "notify.report";

    /** 评论回复 */
    public static final String ROUTING_KEY_COMMENT = "notify.comment";

    /** 系统通知 */
    public static final String ROUTING_KEY_SYSTEM = "notify.system";

    // ==================== 用户统计 - user-15 用户主页 ====================

    /** 用户统计主交换机（Direct） */
    public static final String EXCHANGE_USER_STATS = "exchange.user.stats";

    /** 用户统计死信交换机 */
    public static final String EXCHANGE_DLX_USER_STATS = "exchange.dlx.user.stats";

    /** 用户统计主队列 */
    public static final String QUEUE_USER_STATS = "queue.user.stats.daily";

    /** 用户统计死信队列 */
    public static final String QUEUE_USER_STATS_DLQ = "queue.user.stats.daily.dlq";

    /** 用户统计 - 行为事件 routing key */
    public static final String ROUTING_KEY_USER_STATS_ACTIVITY = "user.stats.activity";

    /** 用户统计 - 死信 routing key */
    public static final String ROUTING_KEY_USER_STATS_DLQ = "user.stats.activity.dlq";
}
