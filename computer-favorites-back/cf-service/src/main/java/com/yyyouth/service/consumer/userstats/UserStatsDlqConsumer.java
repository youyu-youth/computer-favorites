package com.yyyouth.service.consumer.userstats;

import com.rabbitmq.client.Channel;
import com.yyyouth.model.dto.userstats.UserActivityEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户行为事件死信消费者 - user-15 用户主页贡献统计。
 *
 * 行为：
 *  - 仅落 ERROR 日志 + ACK，不再重投递；
 *  - 所有未被聚合的事件由 T+1 全量重算任务（M4 实施）兜底覆写。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatsDlqConsumer {

    @RabbitListener(queues = "#{userStatsDlq.name}")
    public void handleDeadLetter(UserActivityEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.error("[user-stats][DLQ] 死信事件 deliveryTag={}, eventId={}, userId={}, type={}, targetId={}, ts={}",
                deliveryTag,
                event != null ? event.getEventId() : null,
                event != null ? event.getUserId() : null,
                event != null ? event.getType() : null,
                event != null ? event.getTargetId() : null,
                event != null ? event.getTs() : null);
        // 不抛异常、直接 ACK，避免循环消费；T+1 重算任务负责数据修正
        channel.basicAck(deliveryTag, false);
    }
}
