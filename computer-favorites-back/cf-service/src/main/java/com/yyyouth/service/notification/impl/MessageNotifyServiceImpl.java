package com.yyyouth.service.notification.impl;

import com.yyyouth.model.dto.notification.NotifyEvent;
import com.yyyouth.service.notification.MessageNotifyService;
import com.yyyouth.service.rabbitmq.config.RabbitMqProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * @author yyyouth zg
 * @date 2026-05-06
 *
 * 通知发送服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageNotifyServiceImpl implements MessageNotifyService {

    private final RabbitTemplate rabbitTemplate;

    private final RabbitMqProperties properties;

    @Override
    public void send(NotifyEvent event) {
        String exchange = properties.getExchange();
        String routingKey = buildRoutingKey(event.getType());
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend(exchange, routingKey, event, correlationData);
        log.debug("通知事件已投递，correlationId={}, userId={}, type={}",
                correlationData.getId(), event.getUserId(), event.getType());
    }

    @Override
    public void sendBatch(List<NotifyEvent> events) {
        for (NotifyEvent event : events) {
            send(event);
        }
    }

    private String buildRoutingKey(Integer type) {
        String prefix = properties.getNotify().getRoutingKeyPrefix();
        return switch (type) {
            case 1 -> prefix + "system";
            case 2 -> prefix + "comment";
            case 3 -> prefix + "collect";
            case 4 -> prefix + "audit";
            case 5 -> prefix + "report";
            default -> prefix + "system";
        };
    }
}
