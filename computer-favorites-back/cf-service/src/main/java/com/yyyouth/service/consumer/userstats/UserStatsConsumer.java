package com.yyyouth.service.consumer.userstats;

import com.rabbitmq.client.Channel;
import com.yyyouth.model.dto.userstats.UserActivityEvent;
import com.yyyouth.service.user.userstats.UserStatsAggregateService;
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
 * 用户行为事件消费者 - user-15 用户主页贡献统计。
 *
 * 监听队列：queue.user.stats.daily（绑定常量见 UserStatsRabbitBinding / RabbitConstants.QUEUE_USER_STATS）。
 * 处理失败抛出运行时异常 → Spring AMQP 重试至 max-attempts → DLQ。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatsConsumer {

    private final UserStatsAggregateService userStatsAggregateService;

    @RabbitListener(queues = "#{userStatsQueue.name}")
    public void handleUserActivityEvent(UserActivityEvent event, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.debug("[user-stats] 收到事件 deliveryTag={}, eventId={}, userId={}, type={}",
                deliveryTag, event != null ? event.getEventId() : null,
                event != null ? event.getUserId() : null,
                event != null ? event.getType() : null);
        try {
            userStatsAggregateService.apply(event);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[user-stats] 事件处理失败 deliveryTag={}, eventId={}, userId={}, type={}, err={}",
                    deliveryTag,
                    event != null ? event.getEventId() : null,
                    event != null ? event.getUserId() : null,
                    event != null ? event.getType() : null,
                    e.getMessage(), e);
            // 抛出运行时异常交由 Spring AMQP 重试 + DLQ 兜底
            throw new RuntimeException("用户行为事件聚合失败", e);
        }
    }
}
