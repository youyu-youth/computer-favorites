package com.yyyouth.service.rabbitmq.publisher;

import com.yyyouth.model.dto.userstats.UserActivityEvent;
import com.yyyouth.model.enums.UserActivityType;
import com.yyyouth.service.rabbitmq.properties.UserStatsMqProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * 用户行为事件发布器 - user-15 用户主页贡献统计
 *
 * 调用约束：
 *  1. 必须在业务表（t_user_collect / t_website_like / t_comment / t_website_score / t_website / t_browse_history）
 *     同步落库且事务提交（或即将提交）后调用，避免事件先到导致快照表与源表不一致；
 *  2. 自身不抛业务异常，MQ 发送失败仅记录日志，T+1 任务负责兜底重算；
 *  3. 事件 eventId 由本类生成 UUID，消费端用此做幂等去重；
 *  4. categoryId / tagIds / techIds 可为空，消费端按需更新 t_user_tag_affinity。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityPublisher {

    private final RabbitTemplate rabbitTemplate;

    private final UserStatsMqProperties properties;

    /**
     * 发送用户行为事件
     *
     * @param userId      行为发起用户ID
     * @param type        行为类型
     * @param targetId    业务目标ID（websiteId / commentId / submissionId 等，可空）
     * @param categoryId  分类ID（可空，仅 collect / like / browse / submit 时有意义）
     * @param tagIds      标签ID列表（可空）
     * @param techIds     技术栈ID列表（可空）
     */
    public void publish(Long userId, UserActivityType type, Long targetId,
                        Long categoryId, List<Long> tagIds, List<Long> techIds) {
        if (userId == null || userId <= 0 || type == null) {
            log.warn("[user-stats] 跳过事件发布：userId={}, type={}", userId, type);
            return;
        }

        UserActivityEvent event = UserActivityEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .userId(userId)
                .type(type.getCode())
                .targetId(targetId)
                .categoryId(categoryId)
                .tagIds(tagIds)
                .techIds(techIds)
                .ts(LocalDateTime.now())
                .build();

        try {
            CorrelationData correlationData = new CorrelationData(event.getEventId());
            rabbitTemplate.convertAndSend(
                    properties.getExchange(),
                    properties.getRoutingKey(),
                    event,
                    correlationData);
            log.debug("[user-stats] 事件已投递 eventId={}, userId={}, type={}, targetId={}",
                    event.getEventId(), userId, type.getCode(), targetId);
        } catch (Exception e) {
            log.error("[user-stats] 事件投递失败 userId={}, type={}, targetId={}, err={}",
                    userId, type.getCode(), targetId, e.getMessage(), e);
        }
    }

    /**
     * 简化重载：不携带维度信息（适用于无 category / tag 上下文的场景）
     */
    public void publish(Long userId, UserActivityType type, Long targetId) {
        publish(userId, type, targetId, null, null, null);
    }

    /**
     * 简化重载：仅携带 categoryId（适用于 like / collect / browse 等已知 website 分类的场景）
     */
    public void publish(Long userId, UserActivityType type, Long targetId, Long categoryId) {
        publish(userId, type, targetId, categoryId, null, null);
    }
}
