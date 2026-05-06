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
 * 通知消息消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotifyMessageConsumer {

    private final SystemMessageMapper systemMessageMapper;

    @RabbitListener(queues = "#{notifyQueue.name}")
    public void handleNotifyEvent(NotifyEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.debug("收到通知事件，deliveryTag={}, userId={}, type={}", deliveryTag, event.getUserId(), event.getType());
        try {
            SystemMessage systemMessage = SystemMessage.builder()
                    .userId(event.getUserId())
                    .title(event.getTitle())
                    .content(event.getContent())
                    .type(event.getType())
                    .relatedId(event.getRelatedId())
                    .isRead(0)
                    .createTime(event.getCreateTime() != null ? event.getCreateTime() : LocalDateTime.now())
                    .build();
            systemMessageMapper.insert(systemMessage);
            channel.basicAck(deliveryTag, false);
            log.debug("通知入库成功，deliveryTag={}, messageId={}", deliveryTag, systemMessage.getId());
        } catch (Exception e) {
            log.error("通知入库失败，deliveryTag={}, userId={}, type={}", deliveryTag, event.getUserId(), event.getType(), e);
            throw new RuntimeException("通知入库失败", e);
        }
    }
}
