package com.yyyouth.service.rabbitmq.binding;

import com.yyyouth.service.rabbitmq.properties.UserStatsMqProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户统计业务 RabbitMQ Exchange / Queue / Binding 声明
 * 用户主页贡献快照表（t_user_stats_daily / t_user_stats_overview / t_user_tag_affinity）
 * 通过此 Direct 交换机异步增量更新；T+1 定时任务做兜底重算。
 */
@Configuration
@RequiredArgsConstructor
public class UserStatsRabbitBinding {

    private final UserStatsMqProperties properties;

    @Bean
    public DirectExchange userStatsExchange() {
        return new DirectExchange(properties.getExchange(), true, false);
    }

    @Bean
    public DirectExchange userStatsDlxExchange() {
        return new DirectExchange(properties.getDlxExchange(), true, false);
    }

    @Bean
    public Queue userStatsQueue() {
        return QueueBuilder.durable(properties.getQueue())
                .deadLetterExchange(properties.getDlxExchange())
                .deadLetterRoutingKey(properties.getRoutingKeyDlq())
                .build();
    }

    @Bean
    public Queue userStatsDlq() {
        return QueueBuilder.durable(properties.getDlq())
                .build();
    }

    @Bean
    public Binding userStatsBinding() {
        return BindingBuilder.bind(userStatsQueue())
                .to(userStatsExchange())
                .with(properties.getRoutingKey());
    }

    @Bean
    public Binding userStatsDlqBinding() {
        return BindingBuilder.bind(userStatsDlq())
                .to(userStatsDlxExchange())
                .with(properties.getRoutingKeyDlq());
    }
}
