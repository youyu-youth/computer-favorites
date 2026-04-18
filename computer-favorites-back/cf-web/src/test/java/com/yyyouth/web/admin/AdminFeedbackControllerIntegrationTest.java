package com.yyyouth.web.admin;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyyouth.common.constants.HttpStatus;
import com.yyyouth.common.exception.BusinessException;
import com.yyyouth.model.vo.admin.AdminFeedbackDetailVO;
import com.yyyouth.model.vo.admin.AdminFeedbackHandleResultVO;
import com.yyyouth.model.vo.admin.AdminFeedbackListItemVO;
import com.yyyouth.model.vo.admin.AdminFeedbackPageVO;
import com.yyyouth.model.vo.admin.AdminFeedbackStatisticsVO;
import com.yyyouth.service.admin.feedback.AdminFeedbackService;
import com.yyyouth.service.audit.annotation.AuditLog;
import com.yyyouth.web.config.GlobalExceptionHandler;
import com.yyyouth.web.controller.admin.AdminFeedbackController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author yyyouth zg
 * @date 2026-04-18
 *
 * 管理端反馈处理接口集成测试
 */
@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AdminFeedbackControllerIntegrationTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private MockMvc mockMvc;

    @Mock
    private AdminFeedbackService adminFeedbackService;

    @InjectMocks
    private AdminFeedbackController adminFeedbackController;

    /**
     * 初始化 MockMvc
     */
    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(adminFeedbackController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    /**
     * 控制器方法应声明权限与审计注解
     *
     * @throws Exception 反射异常
     */
    @Test
    void shouldDeclarePermissionAndAuditAnnotations() throws Exception {
        Method listMethod = AdminFeedbackController.class.getMethod("list", com.yyyouth.model.dto.admin.AdminFeedbackQueryDTO.class);
        SaCheckPermission listPermission = listMethod.getAnnotation(SaCheckPermission.class);
        Assertions.assertNotNull(listPermission);
        Assertions.assertArrayEquals(new String[]{"admin:feedback:list"}, listPermission.value());

        Method detailMethod = AdminFeedbackController.class.getMethod("detail", Long.class);
        SaCheckPermission detailPermission = detailMethod.getAnnotation(SaCheckPermission.class);
        Assertions.assertNotNull(detailPermission);
        Assertions.assertArrayEquals(new String[]{"admin:feedback:detail"}, detailPermission.value());

        Method replyMethod = AdminFeedbackController.class.getMethod("reply", Long.class, com.yyyouth.model.dto.admin.AdminFeedbackReplyDTO.class);
        SaCheckPermission replyPermission = replyMethod.getAnnotation(SaCheckPermission.class);
        AuditLog replyAuditLog = replyMethod.getAnnotation(AuditLog.class);
        Assertions.assertNotNull(replyPermission);
        Assertions.assertArrayEquals(new String[]{"admin:feedback:reply"}, replyPermission.value());
        Assertions.assertNotNull(replyAuditLog);
        Assertions.assertEquals("reply", replyAuditLog.action());

        Method closeMethod = AdminFeedbackController.class.getMethod("close", Long.class);
        SaCheckPermission closePermission = closeMethod.getAnnotation(SaCheckPermission.class);
        AuditLog closeAuditLog = closeMethod.getAnnotation(AuditLog.class);
        Assertions.assertNotNull(closePermission);
        Assertions.assertArrayEquals(new String[]{"admin:feedback:close"}, closePermission.value());
        Assertions.assertNotNull(closeAuditLog);
        Assertions.assertEquals("close", closeAuditLog.action());
    }

    /**
     * 查询反馈列表成功时应返回分页数据
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldQueryFeedbackListSuccessfully() throws Exception {
        AdminFeedbackListItemVO itemVO = new AdminFeedbackListItemVO();
        itemVO.setId(9001L);
        itemVO.setUserName("张三");
        itemVO.setContent("这里是一条测试反馈内容");
        itemVO.setStatus(0);

        AdminFeedbackStatisticsVO statisticsVO = new AdminFeedbackStatisticsVO();
        statisticsVO.setTotal(1);
        statisticsVO.setPending(1);

        AdminFeedbackPageVO pageVO = new AdminFeedbackPageVO();
        pageVO.setRecords(List.of(itemVO));
        pageVO.setTotal(1L);
        pageVO.setPageNum(1);
        pageVO.setPageSize(6);
        pageVO.setTotalPages(1L);
        pageVO.setStatistics(statisticsVO);
        when(adminFeedbackService.queryFeedbackPage(any())).thenReturn(pageVO);

        mockMvc.perform(get("/api/admin/feedback/list")
                        .param("pageNum", "1")
                        .param("pageSize", "6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(9001))
                .andExpect(jsonPath("$.data.statistics.pending").value(1));
    }

    /**
     * 查询反馈详情成功时应返回详情数据
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldQueryFeedbackDetailSuccessfully() throws Exception {
        AdminFeedbackDetailVO detailVO = new AdminFeedbackDetailVO();
        detailVO.setId(9002L);
        detailVO.setUserName("李四");
        detailVO.setReply("问题已处理");
        when(adminFeedbackService.queryFeedbackDetail(9002L)).thenReturn(detailVO);

        mockMvc.perform(get("/api/admin/feedback/9002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.id").value(9002))
                .andExpect(jsonPath("$.data.reply").value("问题已处理"));
    }

    /**
     * 回复反馈参数非法时应返回参数校验错误
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnBadRequestWhenReplyPayloadInvalid() throws Exception {
        String requestBody = """
                {
                  "reply": "太短了"
                }
                """;

        mockMvc.perform(put("/api/admin/feedback/9003/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.BAD_REQUEST))
                .andExpect(jsonPath("$.msg").value("回复内容长度需在10到500个字符之间"));
    }

    /**
     * 关闭反馈成功时应返回处理结果
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldCloseFeedbackSuccessfully() throws Exception {
        AdminFeedbackHandleResultVO resultVO = new AdminFeedbackHandleResultVO();
        resultVO.setFeedbackId(9004L);
        resultVO.setStatus(2);
        resultVO.setUpdateTime(LocalDateTime.now());
        when(adminFeedbackService.closeFeedback(9004L)).thenReturn(resultVO);

        mockMvc.perform(put("/api/admin/feedback/9004/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(HttpStatus.SUCCESS))
                .andExpect(jsonPath("$.data.feedbackId").value(9004))
                .andExpect(jsonPath("$.data.status").value(2));
    }

    /**
     * 业务异常时应透传业务错误码与提示
     *
     * @throws Exception 执行异常
     */
    @Test
    void shouldReturnBusinessExceptionMessage() throws Exception {
        when(adminFeedbackService.queryFeedbackDetail(9999L))
                .thenThrow(new BusinessException(40401, "反馈记录不存在"));

        mockMvc.perform(get("/api/admin/feedback/9999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40401))
                .andExpect(jsonPath("$.msg").value("反馈记录不存在"));
    }
}
