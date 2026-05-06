package com.yyyouth.service.rabbitmq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConversionException;

/**
 * @author yyyouth zg
 * @date 2026-05-06
 *
 * RabbitMQ Exchange/Queue/Binding 声明及消息转换配置
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

    /**
     * JSON 消息转换器，使用 Spring 容器中的 ObjectMapper（含 JavaTimeModule），
     * 配置受信包以支持 NotifyEvent 等 DTO 的类型推断
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.yyyouth.model");
        converter.setClassMapper(typeMapper);
        return converter;
    }

    /**
     * 监听器容器工厂，配置 JSON 转换器和自定义错误处理策略。
     * Spring AMQP 默认的 DefaultExceptionStrategy 不把 Spring Messaging 的
     * MessageConversionException 视为致命错误，导致旧格式消息（如 Java 序列化）
     * 无限重试。此处扩展策略，让转换失败的消息直接进入 DLQ 丢弃。
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setErrorHandler(new ConditionalRejectingErrorHandler(
                new ConditionalRejectingErrorHandler.DefaultExceptionStrategy() {
                    @Override
                    public boolean isFatal(Throwable t) {
                        if (t instanceof ListenerExecutionFailedException) {
                            Throwable cause = t.getCause();
                            if (cause instanceof MessageConversionException) {
                                return true;
                            }
                        }
                        return super.isFatal(t);
                    }
                }
        ));
        return factory;
    }
}
