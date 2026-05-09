package com.yyyouth.service.rabbitmq.properties;

import com.yyyouth.common.constants.RabbitConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户统计业务 RabbitMQ 配置属性
 * prefix=cf.rabbitmq.user-stats
 */
@Data
@Component
@ConfigurationProperties(prefix = "cf.rabbitmq.user-stats")
public class UserStatsMqProperties {

    /**
     * 用户统计主交换机（Direct 类型，仅 1 个 routingKey 不需通配）
     */
    private String exchange = RabbitConstants.EXCHANGE_USER_STATS;

    /**
     * 用户统计死信交换机
     */
    private String dlxExchange = RabbitConstants.EXCHANGE_DLX_USER_STATS;

    /**
     * 用户统计主队列名（spec §6.1 要求 queue.user.stats.daily）
     */
    private String queue = RabbitConstants.QUEUE_USER_STATS;

    /**
     * 用户统计死信队列名
     */
    private String dlq = RabbitConstants.QUEUE_USER_STATS_DLQ;

    /**
     * 用户行为事件 routing key
     */
    private String routingKey = RabbitConstants.ROUTING_KEY_USER_STATS_ACTIVITY;

    /**
     * 死信 routing key
     */
    private String routingKeyDlq = RabbitConstants.ROUTING_KEY_USER_STATS_DLQ;

    /**
     * 最大重试次数
     */
    private int maxRetry = 3;
}
