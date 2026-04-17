package com.yyyouth.web.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.constants.NotificationErrorCode;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.vo.user.UserMessageBatchReadResultVO;
import com.yyyouth.model.vo.user.UserMessageItemVO;
import com.yyyouth.model.vo.user.UserMessagePageVO;
import com.yyyouth.service.user.notification.UserMessageService;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.user.UserMessageController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-04-08
 *
 * 用户消息接口集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserMessageControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private MockMvc mockMvc;

    @Mock
    private UserMessageService userMessageService;

    @InjectMocks
    private UserMessageController userMessageController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userMessageController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 查询消息分页列表应返回分页结果
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldQueryMessagePageSuccessfully() throws Exception {
        UserMessageItemVO itemVO = new UserMessageItemVO();
        itemVO.setId(101L);
        itemVO.setTitle("审核结果通知");
        itemVO.setContent("你投稿的网站已审核通过");
        itemVO.setType(4);
        itemVO.setRelatedId(2001L);
        itemVO.setIsRead(0);
        itemVO.setCreateTime(LocalDateTime.of(2026, 4, 8, 10, 0, 0));

        UserMessagePageVO pageVO = new UserMessagePageVO();
        pageVO.setList(List.of(itemVO));
        pageVO.setTotal(1L);
        pageVO.setPageNum(1);
        pageVO.setPageSize(20);
        pageVO.setUnreadCount(3L);

        when(userMessageService.queryMessagePage(any())).thenReturn(pageVO);

        mockMvc.perform(get("/api/user/messages")
                        .param("pageNum", "1")
                        .param("pageSize", "20")
                        .param("isRead", "0")
                        .param("type", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.unreadCount").value(3))
                .andExpect(jsonPath("$.data.list[0].id").value(101))
                .andExpect(jsonPath("$.data.list[0].title").value("审核结果通知"))
                .andExpect(jsonPath("$.data.list[0].isRead").value(0));

        verify(userMessageService).queryMessagePage(any());
    }

    /**
     * 查询举报反馈消息分页应返回对应类型结果
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldQueryReportFeedbackMessagePageSuccessfully() throws Exception {
        UserMessageItemVO itemVO = new UserMessageItemVO();
        itemVO.setId(201L);
        itemVO.setTitle("举报处理结果通知");
        itemVO.setContent("你提交的举报已审核完成，当前未采纳。");
        itemVO.setType(5);
        itemVO.setRelatedId(3001L);
        itemVO.setIsRead(0);
        itemVO.setCreateTime(LocalDateTime.of(2026, 4, 16, 11, 0, 0));

        UserMessagePageVO pageVO = new UserMessagePageVO();
        pageVO.setList(List.of(itemVO));
        pageVO.setTotal(1L);
        pageVO.setPageNum(1);
        pageVO.setPageSize(10);
        pageVO.setUnreadCount(1L);

        when(userMessageService.queryMessagePage(any())).thenReturn(pageVO);

        mockMvc.perform(get("/api/user/messages")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("isRead", "0")
                        .param("type", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.unreadCount").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(201))
                .andExpect(jsonPath("$.data.list[0].type").value(5))
                .andExpect(jsonPath("$.data.list[0].title").value("举报处理结果通知"));

        verify(userMessageService).queryMessagePage(any());
    }

    /**
     * 标记单条消息已读应返回成功
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldMarkMessageReadSuccessfully() throws Exception {
        doNothing().when(userMessageService).markMessageRead(eq(101L));

        mockMvc.perform(put("/api/user/messages/101/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.msg").value("标记已读成功"));

        verify(userMessageService).markMessageRead(101L);
    }

    /**
     * 批量标记消息已读应返回批量处理结果
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldBatchReadMessagesSuccessfully() throws Exception {
        UserMessageBatchReadResultVO resultVO = new UserMessageBatchReadResultVO();
        resultVO.setSuccessCount(2);
        resultVO.setFailedCount(1);
        resultVO.setFailedIds(List.of(103L));

        when(userMessageService.batchReadMessages(any())).thenReturn(resultVO);

        String requestBody = "{\"ids\":[101,102,103]}";
        mockMvc.perform(put("/api/user/messages/read/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.failedCount").value(1))
                .andExpect(jsonPath("$.data.failedIds[0]").value(103));

        verify(userMessageService).batchReadMessages(any());
    }

    /**
     * 批量消息 ID 为空时应返回参数错误
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnBadRequestWhenBatchIdsEmpty() throws Exception {
        String requestBody = "{\"ids\":[]}";

        mockMvc.perform(put("/api/user/messages/read/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath("$.msg").value("消息ID列表不能为空"));
    }

    /**
     * 消息不存在时应返回业务错误码
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnBusinessErrorWhenMessageNotFound() throws Exception {
        doThrow(new BusinessException(
                NotificationErrorCode.MESSAGE_NOT_FOUND.getCode(),
                NotificationErrorCode.MESSAGE_NOT_FOUND.getMessage()))
                .when(userMessageService)
                .markMessageRead(eq(999L));

        mockMvc.perform(put("/api/user/messages/999/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(NotificationErrorCode.MESSAGE_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.msg").value(NotificationErrorCode.MESSAGE_NOT_FOUND.getMessage()));
    }
}
