package com.yyyouth.service.rabbitmq.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
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
 * RabbitMQ 通用基础设施配置：MessageConverter + ListenerContainerFactory。
 * 业务相关的 Exchange / Queue / Binding 已拆出到各业务 Binding 类：
 *   - {@link com.yyyouth.service.rabbitmq.binding.NotifyRabbitBinding}
 *   - {@link com.yyyouth.service.rabbitmq.binding.UserStatsRabbitBinding}
 */
@Configuration
public class RabbitMqConfig {

    /**
     * JSON 消息转换器，使用 Spring 容器中的 ObjectMapper（含 JavaTimeModule），
     * 配置受信包以支持 NotifyEvent / UserActivityEvent 等 DTO 的类型推断。
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
