package com.yyyouth.service.rabbitmq.publisher;

import com.yyyouth.model.dto.userstats.UserActivityEvent;
import com.yyyouth.model.enums.UserActivityType;
import com.yyyouth.service.rabbitmq.properties.UserStatsMqProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * @author yyyouth zg
 * @date 2026-05-08
 *
 * UserActivityPublisher 单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserActivityPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private UserStatsMqProperties properties;

    private UserActivityPublisher publisher;

    @BeforeEach
    void setUp() {
        properties = new UserStatsMqProperties();
        publisher = new UserActivityPublisher(rabbitTemplate, properties);
    }

    @Test
    void shouldPublishCollectEventWithCategoryId() {
        publisher.publish(1001L, UserActivityType.COLLECT, 2001L, 88L);

        ArgumentCaptor<UserActivityEvent> eventCaptor = ArgumentCaptor.forClass(UserActivityEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq("exchange.user.stats"),
                eq("user.stats.activity"),
                eventCaptor.capture(),
                any(CorrelationData.class));

        UserActivityEvent captured = eventCaptor.getValue();
        assertThat(captured.getUserId()).isEqualTo(1001L);
        assertThat(captured.getType()).isEqualTo("collect");
        assertThat(captured.getTargetId()).isEqualTo(2001L);
        assertThat(captured.getCategoryId()).isEqualTo(88L);
        assertThat(captured.getEventId()).isNotBlank();
        assertThat(captured.getTs()).isNotNull();
    }

    @Test
    void shouldPublishLikeEventWithoutDimensionInfo() {
        publisher.publish(1001L, UserActivityType.LIKE, 3001L);

        ArgumentCaptor<UserActivityEvent> eventCaptor = ArgumentCaptor.forClass(UserActivityEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq("exchange.user.stats"),
                eq("user.stats.activity"),
                eventCaptor.capture(),
                any(CorrelationData.class));

        UserActivityEvent captured = eventCaptor.getValue();
        assertThat(captured.getType()).isEqualTo("like");
        assertThat(captured.getCategoryId()).isNull();
        assertThat(captured.getTagIds()).isNull();
        assertThat(captured.getTechIds()).isNull();
    }

    @Test
    void shouldPublishSubmitEventWithFullDimensionInfo() {
        publisher.publish(1001L, UserActivityType.SUBMIT, 5001L, 7L,
                List.of(11L, 12L), List.of(21L));

        ArgumentCaptor<UserActivityEvent> eventCaptor = ArgumentCaptor.forClass(UserActivityEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq("exchange.user.stats"),
                eq("user.stats.activity"),
                eventCaptor.capture(),
                any(CorrelationData.class));

        UserActivityEvent captured = eventCaptor.getValue();
        assertThat(captured.getType()).isEqualTo("submit");
        assertThat(captured.getCategoryId()).isEqualTo(7L);
        assertThat(captured.getTagIds()).containsExactly(11L, 12L);
        assertThat(captured.getTechIds()).containsExactly(21L);
    }

    @Test
    void shouldSkipPublishWhenUserIdInvalid() {
        publisher.publish(null, UserActivityType.COLLECT, 1L);
        publisher.publish(0L, UserActivityType.COLLECT, 1L);

        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class),
                any(UserActivityEvent.class), any(CorrelationData.class));
    }

    @Test
    void shouldSkipPublishWhenTypeNull() {
        publisher.publish(1001L, null, 1L);

        verify(rabbitTemplate, never()).convertAndSend(any(String.class), any(String.class),
                any(UserActivityEvent.class), any(CorrelationData.class));
    }

    @Test
    void shouldSwallowExchangeFailureWithoutThrowing() {
        org.mockito.Mockito.doThrow(new RuntimeException("RabbitMQ 连接失败"))
                .when(rabbitTemplate)
                .convertAndSend(any(String.class), any(String.class),
                        any(UserActivityEvent.class), any(CorrelationData.class));

        // 不应抛异常 → 业务事务不被波及
        publisher.publish(1001L, UserActivityType.SCORE, 1L);
    }
}
