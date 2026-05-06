package com.yyyouth.service.rabbitmq.constant;

/**
 * @author yyyouth zg
 * @date 2026-05-06
 *
 * RabbitMQ 通知常量
 */
public final class RabbitMqConstants {

    private RabbitMqConstants() {
    }

    /** 通知 Routing Key 前缀 */
    public static final String ROUTING_KEY_PREFIX = "notify.";

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
