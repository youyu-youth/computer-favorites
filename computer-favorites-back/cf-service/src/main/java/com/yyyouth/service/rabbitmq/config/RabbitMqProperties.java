package com.yyyouth.service.rabbitmq.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author yyyouth zg
 * @date 2026-05-06
 *
 * RabbitMQ 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "cf.rabbitmq")
public class RabbitMqProperties {

    private String exchange = "message.notify";

    private String dlxExchange = "exchange.dlx.notify";

    private Notify notify = new Notify();

    @Data
    public static class Notify {
        private String queue = "q.notify.message";
        private String routingKeyPrefix = "notify.";
        private String dlq = "q.notify.dlq";
        private int maxRetry = 3;
    }
}
