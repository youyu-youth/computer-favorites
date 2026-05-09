package com.yyyouth.service.rabbitmq.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 通知业务 RabbitMQ 配置属性
 * prefix=cf.rabbitmq.notify
 */
@Data
@Component
@ConfigurationProperties(prefix = "cf.rabbitmq.notify")
public class NotifyMqProperties {

    /**
     * 通知主交换机（Topic 类型，按 routingKeyPrefix.* 通配）
     */
    private String exchange = "message.notify";

    /**
     * 通知死信交换机
     */
    private String dlxExchange = "exchange.dlx.notify";

    /**
     * 通知主队列名
     */
    private String queue = "q.notify.message";

    /**
     * 通知死信队列名
     */
    private String dlq = "q.notify.dlq";

    /**
     * routing key 前缀，业务事件追加后缀（system/comment/collect/audit/report）
     */
    private String routingKeyPrefix = "notify.";

    /**
     * 最大重试次数
     */
    private int maxRetry = 3;
}
