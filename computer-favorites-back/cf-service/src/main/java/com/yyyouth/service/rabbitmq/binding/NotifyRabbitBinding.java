package com.yyyouth.service.rabbitmq.binding;

import com.yyyouth.service.rabbitmq.properties.NotifyMqProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 通知业务 RabbitMQ Exchange / Queue / Binding 声明
 * 拆自原 RabbitMqConfig，Bean 名保持不变以兼容已有消费者 SpEL 引用
 * （如 NotifyMessageConsumer 中的 #{notifyQueue.name}）
 */
@Configuration
@RequiredArgsConstructor
public class NotifyRabbitBinding {

    private final NotifyMqProperties properties;

    @Bean
    public TopicExchange notifyExchange() {
        return new TopicExchange(properties.getExchange(), true, false);
    }

    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(properties.getDlxExchange(), true, false);
    }

    @Bean
    public Queue notifyQueue() {
        return QueueBuilder.durable(properties.getQueue())
                .deadLetterExchange(properties.getDlxExchange())
                .deadLetterRoutingKey(properties.getRoutingKeyPrefix() + "dlq")
                .build();
    }

    @Bean
    public Queue notifyDlq() {
        return QueueBuilder.durable(properties.getDlq())
                .build();
    }

    @Bean
    public Binding notifyBinding() {
        return BindingBuilder.bind(notifyQueue())
                .to(notifyExchange())
                .with(properties.getRoutingKeyPrefix() + "#");
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(notifyDlq())
                .to(dlxExchange())
                .with(properties.getRoutingKeyPrefix() + "dlq");
    }
}
