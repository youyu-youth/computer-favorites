package com.yyyouth.service.rabbitmq.config;

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
 * @date 2026-05-06
 *
 * RabbitMQ Exchange/Queue/Binding 声明
 */
@Configuration
@RequiredArgsConstructor
public class RabbitMqConfig {

    private final RabbitMqProperties properties;

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
        return QueueBuilder.durable(properties.getNotify().getQueue())
                .deadLetterExchange(properties.getDlxExchange())
                .deadLetterRoutingKey(properties.getNotify().getRoutingKeyPrefix() + "dlq")
                .build();
    }

    @Bean
    public Queue notifyDlq() {
        return QueueBuilder.durable(properties.getNotify().getDlq())
                .build();
    }

    @Bean
    public Binding notifyBinding() {
        return BindingBuilder.bind(notifyQueue())
                .to(notifyExchange())
                .with(properties.getNotify().getRoutingKeyPrefix() + "#");
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder.bind(notifyDlq())
                .to(dlxExchange())
                .with(properties.getNotify().getRoutingKeyPrefix() + "dlq");
    }
}
