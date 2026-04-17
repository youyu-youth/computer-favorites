package com.yyyouth.service.user.notification;

import cn.dev33.satoken.stp.StpUtil;
import com.yyyouth.common.constants.NotificationErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.dto.user.UserMessageBatchReadDTO;
import com.yyyouth.model.dto.user.UserMessageQueryDTO;
import com.yyyouth.model.pojo.system.SystemMessage;
import com.yyyouth.model.vo.user.UserMessageBatchReadResultVO;
import com.yyyouth.model.vo.user.UserMessagePageVO;
import com.yyyouth.service.mapper.system.SystemMessageMapper;
import com.yyyouth.service.user.notification.impl.UserMessageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 用户消息服务单元测试
 */
@ExtendWith(MockitoExtension.class)
class UserMessageServiceImplTest {

    @Mock
    private SystemMessageMapper systemMessageMapper;

    @InjectMocks
    private UserMessageServiceImpl userMessageService;

    /**
     * 查询消息分页列表应返回列表和未读计数
     */
    @Test
    void shouldQueryMessagePageSuccessfully() {
        UserMessageQueryDTO queryDTO = new UserMessageQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(20);
        queryDTO.setIsRead(0);
        queryDTO.setType(4);

        SystemMessage message = new SystemMessage();
        message.setId(101L);
        message.setUserId(1001L);
        message.setTitle("审核通过通知");
        message.setContent("你的投稿已审核通过");
        message.setType(4);
        message.setRelatedId(2001L);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.of(2026, 4, 8, 11, 0, 0));

        when(systemMessageMapper.countUserMessages(1001L, 0, 4)).thenReturn(1L);
        when(systemMessageMapper.selectUserMessagePage(1001L, 0, 4, 0, 20)).thenReturn(List.of(message));
        when(systemMessageMapper.countUnreadMessages(1001L)).thenReturn(3L);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            UserMessagePageVO pageVO = userMessageService.queryMessagePage(queryDTO);

            assertThat(pageVO.getTotal()).isEqualTo(1L);
            assertThat(pageVO.getUnreadCount()).isEqualTo(3L);
            assertThat(pageVO.getList()).hasSize(1);
            assertThat(pageVO.getList().get(0).getTitle()).isEqualTo("审核通过通知");
            assertThat(pageVO.getList().get(0).getIsRead()).isEqualTo(0);
        }

        verify(systemMessageMapper).countUserMessages(1001L, 0, 4);
        verify(systemMessageMapper).selectUserMessagePage(1001L, 0, 4, 0, 20);
        verify(systemMessageMapper).countUnreadMessages(1001L);
    }

    /**
     * 查询举报反馈消息时应返回对应类型数据
     */
    @Test
    void shouldQueryReportFeedbackMessagePageSuccessfully() {
        UserMessageQueryDTO queryDTO = new UserMessageQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setIsRead(0);
        queryDTO.setType(5);

        SystemMessage message = new SystemMessage();
        message.setId(201L);
        message.setUserId(1001L);
        message.setTitle("举报处理结果通知");
        message.setContent("你提交的举报已审核通过，管理员已执行联动处置。");
        message.setType(5);
        message.setRelatedId(3001L);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.of(2026, 4, 16, 9, 30, 0));

        when(systemMessageMapper.countUserMessages(1001L, 0, 5)).thenReturn(1L);
        when(systemMessageMapper.selectUserMessagePage(1001L, 0, 5, 0, 10)).thenReturn(List.of(message));
        when(systemMessageMapper.countUnreadMessages(1001L)).thenReturn(2L);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            UserMessagePageVO pageVO = userMessageService.queryMessagePage(queryDTO);

            assertThat(pageVO.getTotal()).isEqualTo(1L);
            assertThat(pageVO.getUnreadCount()).isEqualTo(2L);
            assertThat(pageVO.getList()).hasSize(1);
            assertThat(pageVO.getList().get(0).getType()).isEqualTo(5);
            assertThat(pageVO.getList().get(0).getTitle()).isEqualTo("举报处理结果通知");
            assertThat(pageVO.getList().get(0).getContent()).contains("举报");
        }

        verify(systemMessageMapper).countUserMessages(1001L, 0, 5);
        verify(systemMessageMapper).selectUserMessagePage(1001L, 0, 5, 0, 10);
        verify(systemMessageMapper).countUnreadMessages(1001L);
    }

    /**
     * 标记单条消息已读应更新成功
     */
    @Test
    void shouldMarkMessageReadSuccessfully() {
        SystemMessage message = new SystemMessage();
        message.setId(101L);
        message.setUserId(1001L);
        message.setIsRead(0);

        when(systemMessageMapper.selectById(101L)).thenReturn(message);
        when(systemMessageMapper.updateById(any(SystemMessage.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            userMessageService.markMessageRead(101L);
        }

        verify(systemMessageMapper).updateById(org.mockito.ArgumentMatchers.<SystemMessage>argThat(entity ->
                entity.getId().equals(101L) && entity.getIsRead().equals(1)
        ));
    }

    /**
     * 标记他人消息已读应拒绝
     */
    @Test
    void shouldRejectWhenMarkOtherUserMessageRead() {
        SystemMessage message = new SystemMessage();
        message.setId(101L);
        message.setUserId(2002L);
        message.setIsRead(0);

        when(systemMessageMapper.selectById(101L)).thenReturn(message);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            assertThatThrownBy(() -> userMessageService.markMessageRead(101L))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(NotificationErrorCode.MESSAGE_OWNER_MISMATCH.getMessage());
        }
    }

    /**
     * 批量标记应返回成功与失败明细
     */
    @Test
    void shouldBatchReadMessagesWithPartialFailure() {
        UserMessageBatchReadDTO batchReadDTO = new UserMessageBatchReadDTO();
        batchReadDTO.setIds(List.of(101L, 102L, 103L));

        SystemMessage ownUnread = new SystemMessage();
        ownUnread.setId(101L);
        ownUnread.setUserId(1001L);
        ownUnread.setIsRead(0);

        SystemMessage otherUserMessage = new SystemMessage();
        otherUserMessage.setId(102L);
        otherUserMessage.setUserId(2002L);
        otherUserMessage.setIsRead(0);

        when(systemMessageMapper.selectBatchIds(List.of(101L, 102L, 103L))).thenReturn(List.of(ownUnread, otherUserMessage));
        when(systemMessageMapper.updateById(any(SystemMessage.class))).thenReturn(1);

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            UserMessageBatchReadResultVO resultVO = userMessageService.batchReadMessages(batchReadDTO);
            assertThat(resultVO.getSuccessCount()).isEqualTo(1);
            assertThat(resultVO.getFailedCount()).isEqualTo(2);
            assertThat(resultVO.getFailedIds()).containsExactly(102L, 103L);
        }

        verify(systemMessageMapper).selectBatchIds(eq(List.of(101L, 102L, 103L)));
    }

    /**
     * 批量消息参数为空应返回业务异常
     */
    @Test
    void shouldRejectWhenBatchIdsEmpty() {
        UserMessageBatchReadDTO batchReadDTO = new UserMessageBatchReadDTO();
        batchReadDTO.setIds(List.of());

        try (MockedStatic<StpUtil> stpUtilMock = org.mockito.Mockito.mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::checkLogin).thenAnswer(invocation -> null);
            stpUtilMock.when(StpUtil::getLoginIdAsLong).thenReturn(1001L);

            assertThatThrownBy(() -> userMessageService.batchReadMessages(batchReadDTO))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage(NotificationErrorCode.MESSAGE_BATCH_EMPTY.getMessage());
        }
    }
}
