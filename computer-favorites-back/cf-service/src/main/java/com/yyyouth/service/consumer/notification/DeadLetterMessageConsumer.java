package com.yyyouth.service.consumer.notification;

import com.rabbitmq.client.Channel;
import com.yyyouth.model.dto.notification.NotifyEvent;
import com.yyyouth.model.pojo.system.SystemMessage;
import com.yyyouth.service.mapper.system.SystemMessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * @author yyyouth zg
 * @date 2026-05-06
 *
 * 死信消费者，降级为同步写入
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterMessageConsumer {

    private final SystemMessageMapper systemMessageMapper;

    @RabbitListener(queues = "#{notifyDlq.name}")
    public void handleDeadLetter(NotifyEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.warn("收到死信通知，deliveryTag={}, userId={}, type={}, 降级同步写入",
                deliveryTag, event.getUserId(), event.getType());
        try {
            SystemMessage systemMessage = SystemMessage.builder()
                    .userId(event.getUserId())
                    .title("[重试失败]" + event.getTitle())
                    .content(event.getContent())
                    .type(event.getType())
                    .relatedId(event.getRelatedId())
                    .isRead(0)
                    .createTime(event.getCreateTime() != null ? event.getCreateTime() : LocalDateTime.now())
                    .build();
            systemMessageMapper.insert(systemMessage);
            channel.basicAck(deliveryTag, false);
            log.info("死信降级写入成功，deliveryTag={}, messageId={}", deliveryTag, systemMessage.getId());
        } catch (Exception e) {
            log.error("死信降级写入失败，deliveryTag={}, userId={}, type={}，消息将被丢弃",
                    deliveryTag, event.getUserId(), event.getType(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
