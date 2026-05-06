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
}
