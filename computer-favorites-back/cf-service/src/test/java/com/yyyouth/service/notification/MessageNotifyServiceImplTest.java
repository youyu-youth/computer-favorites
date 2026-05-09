package com.yyyouth.service.notification;

import com.yyyouth.model.dto.notification.NotifyEvent;
import com.yyyouth.model.enums.UserMessageType;
import com.yyyouth.service.notification.impl.MessageNotifyServiceImpl;
import com.yyyouth.service.rabbitmq.properties.NotifyMqProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author yyyouth zg
 * @date 2026-05-06
 *
 * 通知发送服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class MessageNotifyServiceImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private NotifyMqProperties properties;

    private MessageNotifyServiceImpl messageNotifyService;

    @BeforeEach
    void setUp() {
        properties = new NotifyMqProperties();
        properties.setExchange("message.notify");
        properties.setDlxExchange("exchange.dlx.notify");
        properties.setQueue("q.notify.message");
        properties.setRoutingKeyPrefix("notify.");
        properties.setDlq("q.notify.dlq");
        properties.setMaxRetry(3);
        messageNotifyService = new MessageNotifyServiceImpl(rabbitTemplate, properties);
    }

    @Test
    void shouldSendAuditNotificationWithCorrectRoutingKey() {
        NotifyEvent event = NotifyEvent.builder()
                .userId(1001L)
                .title("网站投稿审核结果通知")
                .content("你投稿的网站《Test》已审核通过")
                .type(UserMessageType.AUDIT_RESULT.getCode())
                .relatedId(2001L)
                .build();

        messageNotifyService.send(event);

        ArgumentCaptor<NotifyEvent> eventCaptor = ArgumentCaptor.forClass(NotifyEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq("message.notify"),
                eq("notify.audit"),
                eventCaptor.capture(),
                any(CorrelationData.class)
        );
        NotifyEvent captured = eventCaptor.getValue();
        assertThat(captured.getUserId()).isEqualTo(1001L);
        assertThat(captured.getType()).isEqualTo(4);
    }

    @Test
    void shouldSendCollectNotificationWithCorrectRoutingKey() {
        NotifyEvent event = NotifyEvent.builder()
                .userId(2001L)
                .title("收藏提醒")
                .content("用户 test 收藏了你的网站")
                .type(UserMessageType.FAVORITE_REMINDER.getCode())
                .relatedId(3001L)
                .build();

        messageNotifyService.send(event);

        verify(rabbitTemplate).convertAndSend(
                eq("message.notify"),
                eq("notify.collect"),
                any(NotifyEvent.class),
                any(CorrelationData.class)
        );
    }

    @Test
    void shouldSendReportFeedbackNotificationWithCorrectRoutingKey() {
        NotifyEvent event = NotifyEvent.builder()
                .userId(1001L)
                .title("举报处理结果通知")
                .content("举报已审核通过")
                .type(UserMessageType.REPORT_FEEDBACK.getCode())
                .relatedId(4001L)
                .build();

        messageNotifyService.send(event);

        verify(rabbitTemplate).convertAndSend(
                eq("message.notify"),
                eq("notify.report"),
                any(NotifyEvent.class),
                any(CorrelationData.class)
        );
    }

    @Test
    void shouldSendCommentReplyNotificationWithCorrectRoutingKey() {
        NotifyEvent event = NotifyEvent.builder()
                .userId(1001L)
                .title("收到评论回复")
                .content("好文章")
                .type(UserMessageType.COMMENT_REPLY.getCode())
                .relatedId(5001L)
                .build();

        messageNotifyService.send(event);

        verify(rabbitTemplate).convertAndSend(
                eq("message.notify"),
                eq("notify.comment"),
                any(NotifyEvent.class),
                any(CorrelationData.class)
        );
    }

    @Test
    void shouldSendBatchOfEvents() {
        NotifyEvent event1 = NotifyEvent.builder()
                .userId(1001L).title("T1").content("C1")
                .type(1).build();
        NotifyEvent event2 = NotifyEvent.builder()
                .userId(2001L).title("T2").content("C2")
                .type(2).build();

        messageNotifyService.sendBatch(java.util.List.of(event1, event2));

        verify(rabbitTemplate, times(2)).convertAndSend(
                any(String.class), any(String.class), any(NotifyEvent.class), any(CorrelationData.class)
        );
    }
}
